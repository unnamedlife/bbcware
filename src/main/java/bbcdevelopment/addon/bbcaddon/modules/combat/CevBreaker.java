package bbcdevelopment.addon.bbcaddon.modules.combat;

import bbcdevelopment.addon.bbcaddon.BBCAddon;
import bbcdevelopment.addon.bbcaddon.modules.BBCModule;
import bbcdevelopment.addon.bbcaddon.utils.entity.TargetHelper;
import bbcdevelopment.addon.bbcaddon.utils.security.Initialization;
import bbcdevelopment.addon.bbcaddon.utils.world.BlockHelper;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayerInteractionManagerAccessor;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class CevBreaker extends BBCModule {
    public CevBreaker(){
        super(BBCAddon.Combat, "cev-breaker", "");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Double> breakRange = sgGeneral.add(new DoubleSetting.Builder().name("break-range").defaultValue(5).range(0, 10).build());
    private final Setting<Boolean> extraPositions = sgGeneral.add(new BoolSetting.Builder().name("extra-positions").defaultValue(true).build());
    private final Setting<Double> progressValue = sgGeneral.add(new DoubleSetting.Builder().name("progress").defaultValue(0.95).range(0, 1).build());
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder().name("rotate").defaultValue(false).build());

    private final Setting<Boolean> swing = sgRender.add(new BoolSetting.Builder().name("swing").description("Whether to swing hand client-side.").defaultValue(true).build());
    private final Setting<RenderMode> render = sgRender.add(new EnumSetting.Builder<RenderMode>().name("render").description("Renders the block where it is placing a anchor.").defaultValue(RenderMode.Smooth).build());
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).visible(() -> render.get() != RenderMode.Off).build());
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color for positions to be placed.").defaultValue(new SettingColor(15, 255, 211,75)).visible(() -> render.get() != RenderMode.Off).build());
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color for positions to be placed.").defaultValue(new SettingColor(15, 255, 211)).visible(() -> render.get() != RenderMode.Off).build());

    private BlockPos renderPos;
    private double progress;

    @Override
    public void onActivate() {

    }

    @EventHandler
    private void onTickPost(TickEvent.Post event){
        if (mc == null || mc.player == null || mc.world == null || mc.interactionManager == null) return;

        PlayerEntity target = getTarget();
        if (target == null) return;

        FindItemResult pickAxe = InvUtils.findInHotbar(Items.NETHERITE_PICKAXE, Items.DIAMOND_PICKAXE);
        FindItemResult endCrystal = InvUtils.findInHotbar(Items.END_CRYSTAL);
        if (!pickAxe.found()) {
            warning("No pickaxe on hotbar --> Toggle");
            toggle();
            return;
        }

        if (mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL && !endCrystal.found()){
            warning("No end crystal on hotbar --> Toggle");
            toggle();
            return;
        }

        BlockPos validPos = getValidPos(target);
        if (validPos == null) return;
        renderPos = validPos;

        if (BlockHelper.isAir(validPos)){
            Entity entity = getEntity(validPos.add(0, 1, 0));
            if (entity != null){
                mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                if (swing.get()) mc.player.swingHand(Hand.MAIN_HAND);
                else mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
            else BlockUtils.place(validPos, InvUtils.findInHotbar(Items.OBSIDIAN), rotate.get(), 0);
        }else {
            if (progress >= progressValue.get()){
                Hand hand = getHand(endCrystal);
                mc.interactionManager.interactBlock(mc.player, hand, new BlockHitResult(BlockHelper.closestVec3d(validPos), Direction.UP, validPos, true));
                if (swing.get()) mc.player.swingHand(Hand.MAIN_HAND);
                else mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                if (hand == Hand.MAIN_HAND) InvUtils.swapBack();
            }
            InvUtils.swap(pickAxe.slot(), true);
            mc.interactionManager.updateBlockBreakingProgress(validPos, Direction.UP);
            if (swing.get()) mc.player.swingHand(Hand.MAIN_HAND);
            else mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            progress = ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).getBreakingProgress();
            InvUtils.swapBack();
        }
    }

    @EventHandler
    private void on3dRender(Render3DEvent event){
        if (renderPos == null || render.get() == RenderMode.Off) return;

        switch (render.get()){
            case Smooth -> {
                double min = progress / 2;
                Vec3d vec3d = new Vec3d(renderPos.getX() + 0.5, renderPos.getY() + 0.5, renderPos.getZ() + 0.5);
                Box box = new Box(vec3d.x - min, vec3d.y - min, vec3d.z - min, vec3d.x + min, vec3d.y + min, vec3d.z + min);
                event.renderer.box(box, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
            }
            case Box -> {
                if (BlockHelper.isAir(renderPos)) return;
                event.renderer.box(renderPos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
            }
        }
    }

    private EndCrystalEntity getEntity(BlockPos pos){
        for (Entity entity : mc.world.getEntities()){
            if (!(entity instanceof EndCrystalEntity)) continue;
            if (entity.getBlockPos().equals(pos)) return (EndCrystalEntity) entity;
        }
        return null;
    }

    private PlayerEntity getTarget(){
        List<PlayerEntity> targets = TargetHelper.getTargetsInRange(10);

        targets = targets.stream().filter(this::isThereValidPositions).collect(Collectors.toList());
        targets.sort(Comparator.comparing(PlayerUtils::distanceTo));
        return targets.isEmpty() ? null : targets.get(0);
    }

    private boolean isThereValidPositions(PlayerEntity player){
        BlockPos pos = player.getBlockPos().add(0, 1, 0);

        AtomicBoolean isValid = new AtomicBoolean(false);

        List<BlockPos> list = new ArrayList<>(){{
           add(pos.add(0, 1, 0));

           if (extraPositions.get()){
               add(pos.add(1, 0, 0));
               add(pos.add(-1, 0, 0));
               add(pos.add(0, 0, 1));
               add(pos.add(0, 0, -1));
           }
        }};

        list.forEach(p -> {
            if (isValidPos(p)) isValid.set(true);
        });

        return isValid.get();
    }

    private boolean isValidPos(BlockPos pos){
        return (BlockUtils.canBreak(pos) || BlockHelper.isAir(pos)) && BlockHelper.isAir(pos.add(0, 1, 0)) && PlayerUtils.distanceTo(pos) <= breakRange.get();
    }

    private BlockPos getValidPos(PlayerEntity player){
        BlockPos pos = player.getBlockPos().add(0, 1, 0);

        List<BlockPos> list = new ArrayList<>(){{
            add(pos.add(0, 1, 0));

            if (extraPositions.get()){
                add(pos.add(1, 0, 0));
                add(pos.add(-1, 0, 0));
                add(pos.add(0, 0, 1));
                add(pos.add(0, 0, -1));
            }
        }};

        list = list.stream().filter(this::isValidPos).collect(Collectors.toList());
        list.sort(Comparator.comparing(p -> getPriority(p, player)));
        return list.isEmpty() ? null : list.get(0);
    }

    private double getPriority(BlockPos pos, PlayerEntity player){
        if (pos.equals(player.getBlockPos().add(0, 2, 0))) return 0;
        return PlayerUtils.distanceTo(pos);
    }

    private Hand getHand(FindItemResult endCrystal){
        if (mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) return Hand.OFF_HAND;

        InvUtils.swap(endCrystal.slot(), true);
        return Hand.MAIN_HAND;
    }

    public enum RenderMode{
        Off,
        Box,
        Smooth
    }
}
