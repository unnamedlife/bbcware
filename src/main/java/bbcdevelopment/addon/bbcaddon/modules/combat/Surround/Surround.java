package bbcdevelopment.addon.bbcaddon.modules.combat.Surround;

import bbcdevelopment.addon.bbcaddon.BBCAddon;
import bbcdevelopment.addon.bbcaddon.event.BlockUpdateEvent;
import bbcdevelopment.addon.bbcaddon.modules.BBCModule;
import bbcdevelopment.addon.bbcaddon.utils.math.TimerUtils;
import bbcdevelopment.addon.bbcaddon.utils.world.BlockPosX;
import bbcdevelopment.addon.bbcaddon.utils.world.Place;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayerInteractionManagerAccessor;
import meteordevelopment.meteorclient.mixin.WorldRendererAccessor;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Surround extends BBCModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Place.Swap> swap = sgGeneral.add(new EnumSetting.Builder<Place.Swap>().name("swap").defaultValue(Place.Swap.Silent).build());
    private final Setting<Integer> BPI = sgGeneral.add(new IntSetting.Builder().name("block-per-interval").defaultValue(3).range(1, 5).sliderRange(1, 5).build());
    private final Setting<Integer> intervalDelay = sgGeneral.add(new IntSetting.Builder().name("interval-delay").defaultValue(1).range(0, 3).sliderRange(0, 3).build());
    private final Setting<Boolean> packet = sgGeneral.add(new BoolSetting.Builder().name("packet").defaultValue(false).build());
    private final Setting<Boolean> antiBreak = sgGeneral.add(new BoolSetting.Builder().name("anti-break").defaultValue(false).build());
    private final Setting<Boolean> antiCrystal = sgGeneral.add(new BoolSetting.Builder().name("anti-crystal").defaultValue(true).build());
    private final Setting<Boolean> oldPlace = sgGeneral.add(new BoolSetting.Builder().name("1.12").defaultValue(false).build());
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder().name("rotate").defaultValue(false).build());
    private final Setting<Integer> collisionPassed = sgGeneral.add(new IntSetting.Builder().name("collision-passed").defaultValue(1500).range(1250, 5000).sliderRange(1250, 5000).build());
    private final Setting<Boolean> fastReplace = sgGeneral.add(new BoolSetting.Builder().name("fast-replace").defaultValue(true).build());
    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder().name("auto-disable").defaultValue(true).build());

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder().name("render").defaultValue(true).build());
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("shape-mode").defaultValue(ShapeMode.Both).visible(render::get).build());
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder().name("side-color").defaultValue(new SettingColor(255, 0, 170, 10)).visible(render::get).build());
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("line-color").defaultValue(new SettingColor(255, 0, 170, 90)).visible(render::get).build());


    private List<BlockPosX> poses = new ArrayList<>();
    private final List<BlockPosX> queue = new ArrayList<>();

    private final TimerUtils timer = new TimerUtils();
    private final TimerUtils oneTick = new TimerUtils();

    private int interval;

    public Surround() {
        super(BBCAddon.Combat, "Surround+", "Surrounds you in blocks to prevent massive crystal damage.");
    }

    @Override
    public void onActivate() {
        queue.clear();

        interval = 0;
        timer.reset();
        oneTick.reset();
    }

    public List<BlockPosX> getPositions(PlayerEntity player) {
        List<BlockPosX> positions = new ArrayList<>();
        List<Entity> getEntityBoxes;

        for (BlockPosX bp : Util.getSphere(player.getBlockPos(), 3, 1)) {
            if (!bp.replaceable()) continue;

            getEntityBoxes = mc.world.getOtherEntities(null, bp.box(), entity -> entity == player);
            if (!getEntityBoxes.isEmpty()) continue;
            if (timer.passedMillis(collisionPassed.get().longValue()) && !mc.world.canPlace(Blocks.OBSIDIAN.getDefaultState(), bp, ShapeContext.absent())) continue;

            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP || direction == Direction.DOWN) continue;

                getEntityBoxes = mc.world.getOtherEntities(null, bp.offset(direction).box(), entity -> entity == player);
                if (!getEntityBoxes.isEmpty()) positions.add(bp);
            }
        }

        return positions;
    }

    @EventHandler
    public void onUpdate(BlockUpdateEvent event) {
        if (!antiCrystal.get()) return;
        if (!getPositions(mc.player).contains(event.pos)) return;

        List<Entity> entities = mc.world.getOtherEntities(null, new BlockPosX(event.pos).box(), entity -> entity instanceof EndCrystalEntity);
        if (entities.isEmpty()) return;

        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(entities.get(0), mc.player.isSneaking()));
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (autoDisable.get() && ((mc.options.jumpKey.isPressed() || mc.player.input.jumping) || mc.player.prevY < mc.player.getPos().getY())) {
            toggle();
            return;
        }

        if (interval > 0) interval--;
        if (interval > 0) return;

        Map<Integer, BlockBreakingInfo> blocks = ((WorldRendererAccessor) mc.worldRenderer).getBlockBreakingInfos();
        BlockPos ownBreakingPos = ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).getCurrentBreakingBlockPos();
        ArrayList<BlockPos> boobies = Util.getSurroundBlocks(mc.player);

        blocks.values().forEach(info -> {
            BlockPosX pos = new BlockPosX(info.getPos());

            if (antiBreak.get() && !pos.equals(ownBreakingPos) && info.getStage() >= 0) {
                if (boobies.contains(pos)) queue.addAll(getBlocksAround(pos));
            }
            if (fastReplace.get() && !pos.equals(ownBreakingPos) && info.getStage() >= 8) {
                if (boobies.contains(pos)) mc.world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        });


        poses = getPositions(mc.player);
        poses.addAll(queue);

        FindItemResult block = InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem(itemStack.getItem()).getBlastResistance() >= 600);
        if (poses.isEmpty() || !block.found()) {
            if (Util.isSurrounded(mc.player)) timer.reset();
            return;
        }

        for (int i = 0; i <= BPI.get(); i++) {
            if (poses.size() > i) {
                BlockPosX bp = new BlockPosX(poses.get(i));

                if (oldPlace.get()) {
                    Place.place$Old(bp, block, swap.get(), packet.get(), rotate.get());
                } else Place.place$New(bp, block, swap.get(), packet.get(), rotate.get());
                queue.remove(poses.get(i));
            }
        }
        interval = intervalDelay.get();
    }

    private List<BlockPosX> getBlocksAround(BlockPosX blockPos) {
        List<BlockPosX> blocks = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            BlockPosX bp = blockPos.offset(direction);

            if (direction == Direction.UP || direction == Direction.DOWN) continue;
            if (bp.hasEntity()) continue;
            if (!bp.air()) continue;
            if (queue.contains(bp)) continue;

            blocks.add(bp);
        }

        return blocks;
    }

    @EventHandler
    public void onRender(Render3DEvent event) {
        if (poses.isEmpty()) return;

        poses.forEach(bp -> event.renderer.box(bp.box(), sideColor.get(), lineColor.get(), shapeMode.get(), 0));
    }
}
