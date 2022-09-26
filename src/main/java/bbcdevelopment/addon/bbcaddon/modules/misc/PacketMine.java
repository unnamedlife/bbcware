package bbcdevelopment.addon.bbcaddon.modules.misc;

import bbcdevelopment.addon.bbcaddon.BBCAddon;
import bbcdevelopment.addon.bbcaddon.modules.BBCModule;
import bbcdevelopment.addon.bbcaddon.utils.world.BlockPosX;
import bbcdevelopment.addon.bbcaddon.utils.world.PacketUtils;
import bbcdevelopment.addon.bbcaddon.utils.world.Place;
import bbcdevelopment.addon.bbcaddon.utils.world.Task;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PacketMine extends BBCModule {
    public SettingGroup sgGeneral = settings.getDefaultGroup();
    public SettingGroup sgRender = settings.createGroup("Render");

    public Setting<Place.Swap> swap = sgGeneral.add(new EnumSetting.Builder<Place.Swap>().name("swap").defaultValue(Place.Swap.Normal).build());
    public Setting<Mode> queue = sgGeneral.add(new EnumSetting.Builder<Mode>().name("queue").defaultValue(Mode.Unlimited).build());
    public Setting<Integer> size = sgGeneral.add(new IntSetting.Builder().name("size").defaultValue(1).range(1, 25).visible(() -> queue.get() == Mode.Limited).build());

    public Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder().name("swing").defaultValue(false).build());
    public Setting<Boolean> render = sgRender.add(new BoolSetting.Builder().name("render").defaultValue(true).build());
    public Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("shape-mode").defaultValue(ShapeMode.Both).visible(render::get).build());
    public Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder().name("side-color").defaultValue(new SettingColor(255, 0, 170, 10)).visible(render::get).build());
    public Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("line-color").defaultValue(new SettingColor(255, 0, 170, 90)).visible(render::get).build());
    public Setting<SettingColor> readySide = sgRender.add(new ColorSetting.Builder().name("ready-side-color").defaultValue(new SettingColor(255, 0, 170, 10)).visible(render::get).build());
    public Setting<SettingColor> readyLine = sgRender.add(new ColorSetting.Builder().name("ready-line-color").defaultValue(new SettingColor(255, 0, 170, 90)).visible(render::get).build());

    private final List<Queue> queues = new ArrayList<>();

    private final Task task = new Task();
    private final PacketUtils packets = new PacketUtils();

    public PacketMine() {
        super(BBCAddon.Misc, "Mining+", "Automatically breaks blocks via packets");
    }

    @Override
    public void onActivate() {
        queues.clear();

        task.reset();
        packets.reset();
    }

    @EventHandler
    public void onStartBreaking(StartBreakingBlockEvent event) {
        BlockPosX bp = new BlockPosX(event.blockPos);
        if (bp.unbreakable()) {
            event.cancel();
            return;
        }

        if (!has(key(bp))) {
            if (queue.get() == Mode.Limited && queues.size() >= size.get()) {
                event.cancel();
                return;
            }

            queues.add(new Queue(bp));
        }

        event.cancel();
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (queues.isEmpty()) return;

        // Remove from queue
        if (mc.crosshairTarget instanceof BlockHitResult hitResult) {
            BlockPosX bp = new BlockPosX(hitResult.getBlockPos());

            if (canRemove(bp)) {
                if (has(key(bp))) {
                    Queue queue = get(key(bp));

                    if (queue.progress() > 0.0) {
                        task.reset();
                        packets.reset();
                    }
                }

                queues.remove(get(key(bp)));
            }
        }

        // Removing bugged poses
        for (Queue queue : queues) {
            if (queue.bp().distance() > 4.6) {
                if (queue.progress() > 0.0) {
                    task.reset();
                    packets.reset();
                }

                queues.remove(queue);
            }
        }
        if (queues.isEmpty()) return;

        BlockPosX bp = queues.get(0).bp();
        packets.mine(bp, task, "Finish");
        queues.get(0).progress(packets.getProgress());

        if (packets.isReady()) {
            FindItemResult tool = InvUtils.findFastestTool(bp.state());

            switch (swap.get()) {
                case Normal, Silent -> {
                    if (tool.found() && !tool.isMainHand()) {
                        InvUtils.swap(tool.slot(), true);
                    }
                }
                case Move -> move(tool, () -> {
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, bp, Direction.UP));
                    doSwing(swing.get());
                });
            }
            if (swap.get() != Place.Swap.Move) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, bp, Direction.UP));
                doSwing(swing.get());
            }
            if (swap.get() == Place.Swap.Silent) InvUtils.swapBack();

            task.reset();
            packets.reset();

            queues.remove(0);
        }
    }

    private boolean canRemove(BlockPosX bp) {
        if (!(mc.player.getMainHandStack().getItem() instanceof ToolItem)) return false;

        return mc.options.useKey.isPressed() && has(key(bp));
    }

    private void doSwing(boolean swing) {
        if (swing) {
            mc.player.swingHand(Hand.MAIN_HAND);
        } else mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    public void move(FindItemResult itemResult, Runnable runnable) {
        if (itemResult.isOffhand()) {
            runnable.run();
            return;
        }

        move(mc.player.getInventory().selectedSlot, itemResult.slot());
        runnable.run();
        move(mc.player.getInventory().selectedSlot, itemResult.slot());
    }

    private void move(int from, int to) {
        ScreenHandler handler = mc.player.currentScreenHandler;

        Int2ObjectArrayMap<ItemStack> stack = new Int2ObjectArrayMap<>();
        stack.put(to, handler.getSlot(to).getStack());

        mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(handler.syncId, handler.getRevision(), PlayerInventory.MAIN_SIZE + from, to, SlotActionType.SWAP, handler.getCursorStack().copy(), stack));
    }

    public boolean has(String key) {
        return get(key) != null;
    }

    public Queue get(String key) {
        for (Queue queue : queues) {
            if (Objects.equals(queue.key(), key)) return queue;
        }

        return null;
    }

    public String key(BlockPosX bp) {
        return bp.x() + ", " + bp.y() + ", " + bp.z();
    }

    @EventHandler
    public void onRender(Render3DEvent event) {
        if (!render.get() || queues.isEmpty()) return;

        for (Queue queue : queues) {
            BlockPosX bp = queue.bp();

            Color side;
            Color line;

            if (queue.progress() < 0.94) {
                side = sideColor.get();
                line = lineColor.get();
            } else {
                side = readySide.get();
                line = readyLine.get();
            }


            event.renderer.box(bp.box(), side, line, shapeMode.get(), 0);
        }
    }

    public enum Mode {
        Unlimited, Limited
    }

    public static class Queue {
        private BlockPosX bp;
        private double progress;

        private String key;

        public Queue(BlockPosX bp) {
            this.bp = bp;

            this.progress = 0;
            this.key = bp.x() + ", " + bp.y() + ", " + bp.z();
        }

        public BlockPosX bp() {
            return this.bp;
        }

        public double progress() {
            return this.progress;
        }

        public String key() {
            return this.key;
        }

        public void progress(double progress) {
            this.progress = progress;
        }
    }
}
