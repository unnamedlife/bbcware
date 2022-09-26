package bbcdevelopment.addon.bbcaddon.modules.combat;

import bbcdevelopment.addon.bbcaddon.BBCAddon;
import bbcdevelopment.addon.bbcaddon.modules.BBCModule;
import bbcdevelopment.addon.bbcaddon.utils.entity.EntityHelper;
import bbcdevelopment.addon.bbcaddon.utils.player.InvHelper;
import bbcdevelopment.addon.bbcaddon.utils.security.Initialization;
import bbcdevelopment.addon.bbcaddon.utils.world.BlockHelper;
import bbcdevelopment.addon.bbcaddon.utils.world.BlockPosX;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayerInteractionManagerAccessor;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoCity extends BBCModule {
    public AutoCity(){
        super(BBCAddon.Combat, "AutoCity+", "");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgInventory = settings.createGroup("Inventory");
    private final SettingGroup sgMisc = settings.createGroup("Misc");
    private final SettingGroup sgPause = settings.createGroup("Pause");
    private final SettingGroup sgRender = settings.createGroup("Render");


    private final Setting<Double> breakRange = sgGeneral.add(new DoubleSetting.Builder().name("break-range").description("The distance where you can place/break beds.").defaultValue(5).range(0, 7).sliderRange(0, 7).build());
    private final Setting<Boolean> keepPosition = sgGeneral.add(new BoolSetting.Builder().name("keep-position").defaultValue(true).build());
    private final Setting<MineMode> mineMode = sgGeneral.add(new EnumSetting.Builder<MineMode>().name("mine-mode").defaultValue(MineMode.Packet).build());
    private final Setting<SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<SortMode>().name("sort-mode").defaultValue(SortMode.Openest).build());

    private final Setting<SwapMode> swapMode = sgInventory.add(new EnumSetting.Builder<SwapMode>().name("swap-mode").description("Slot swap method.").defaultValue(SwapMode.Silent).build());
    private final Setting<Boolean> syncSlot = sgInventory.add(new BoolSetting.Builder().name("sync-slot").description("Synchronize the slot to get rid of fakes.").defaultValue(true).build());

    private final Setting<Boolean> pauseOnEat = sgPause.add(new BoolSetting.Builder().name("pause-on-eat").description("Pause placing while eating.").defaultValue(false).build());
    private final Setting<Boolean> pauseOnDrink = sgPause.add(new BoolSetting.Builder().name("pause-on-drink").description("Pause placing while drinking.").defaultValue(false).build());
    private final Setting<Boolean> pauseOnMine = sgPause.add(new BoolSetting.Builder().name("pause-on-mine").description("Pause placing while mining.").defaultValue(false).build());

    private final Setting<Boolean> swing = sgRender.add(new BoolSetting.Builder().name("swing").description("Whether to swing hand client-side.").defaultValue(true).build());
    private final Setting<RenderMode> render = sgRender.add(new EnumSetting.Builder<RenderMode>().name("render").description("Renders the block where it is placing a anchor.").defaultValue(RenderMode.Smooth).build());
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).visible(() -> render.get() != RenderMode.Off).build());
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color for positions to be placed.").defaultValue(new SettingColor(15, 255, 211,75)).visible(() -> render.get() != RenderMode.Off).build());
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color for positions to be placed.").defaultValue(new SettingColor(15, 255, 211)).visible(() -> render.get() != RenderMode.Off).build());

    private double progress;
    private BlockPosX keepPos;

    @Override
    public void onActivate() {

    }

    @Override
    public void onDeactivate() {
        keepPos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event){
        if (shouldPause()) return;

        FindItemResult pickAxe = InvUtils.findInHotbar(Items.NETHERITE_PICKAXE, Items.DIAMOND_PICKAXE);

        if (!pickAxe.found()){
            warning("No pickaxe in hotbar...toggle");
            toggle();
            return;
        }

        PlayerEntity player = getTarget();
        if (player == null) return;

        List<BlockPosX> cities = getCities(player);
        if (cities.isEmpty()) return;

        switch (sortMode.get()){
            case Openest -> cities.sort(Comparator.comparing(this::getOpenScore));
            case Distance -> cities.sort(Comparator.comparing(PlayerUtils::distanceTo));
        }

        if (keepPos == null || !keepPosition.get()) keepPos = cities.get(0);

        if (!keepPos.air()){
            mine(pickAxe, keepPos);
        }
    }

    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event){
        if (event.newState.isAir() && event.pos.equals(keepPos)){
            progress = 0;
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event){
        if (keepPos == null || render.get() == RenderMode.Off) return;

        switch (render.get()){
            case Smooth -> {
                double min = progress / 2;
                Vec3d vec3d = new Vec3d(keepPos.getX() + 0.5, keepPos.getY() + 0.5, keepPos.getZ() + 0.5);
                Box box = new Box(vec3d.x - min, vec3d.y - min, vec3d.z - min, vec3d.x + min, vec3d.y + min, vec3d.z + min);
                event.renderer.box(box, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
            }
            case Box -> event.renderer.box(keepPos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
    }

    private void mine(FindItemResult itemResult, BlockPosX posX){
        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = itemResult.slot();
        switch (mineMode.get()){
            case Vanilla -> {
                mc.interactionManager.updateBlockBreakingProgress(posX, Direction.UP);
                progress = ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).getBreakingProgress();
            }
            case Packet -> {
                if (canBreak(itemResult.slot(), posX)){
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, posX, Direction.UP));
                    if (swing.get()) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, posX, Direction.UP));
                }
            }
        }

        if (swapMode.get() == SwapMode.Silent) {
            //mc.player.getInventory().selectedSlot = prev;
            InvUtils.swap(prev, false);
            if (syncSlot.get()) InvHelper.syncSlot();
        }
    }

    private PlayerEntity getTarget(){
        List<PlayerEntity> targets = new ArrayList<>();

        for (PlayerEntity player : mc.world.getPlayers()){
            if (player.isCreative() || player.isSpectator() || Friends.get().isFriend(player) || player == mc.player || player.isDead()) continue;
            if (EntityHelper.isInBedrockHole(player, true)) continue;

            targets.add(player);
        }

        targets.sort(Comparator.comparing(player -> mc.player.distanceTo(player)));
        return targets.isEmpty() ? null : targets.get(0);
    }

    private List<BlockPosX> getCities(PlayerEntity player){
        List<BlockPosX> positions = new ArrayList<>();
        List<Entity> entityBoxes;

        BlockPosX playerPos = new BlockPosX(player.getBlockPos());

        List<BlockPosX> sphere = BlockHelper.getSphere(playerPos, 5, 1);

        for (BlockPosX posX : sphere){
            if (posX.air() || !BlockUtils.canBreak(posX)) continue;
            if (posX.distance() > breakRange.get()) continue;
            entityBoxes = mc.world.getOtherEntities(null, new Box(posX), entity -> entity == player);
            if (!entityBoxes.isEmpty()) continue;

            for (Direction direction : Direction.values()){
                if (direction == Direction.UP || direction == Direction.DOWN) continue;
                entityBoxes = mc.world.getOtherEntities(null, new Box(posX.offset(direction)), entity -> entity == player);
                if (!entityBoxes.isEmpty()) positions.add(posX);
            }
        }
        return positions;
    }

    private int getOpenScore(BlockPosX posX){
        int i = 4;
        for (Direction direction : Direction.values()){
            if (posX.offset(direction).air()) i--;
        }
        return i;
    }

    private boolean shouldPause(){
        return PlayerUtils.shouldPause(pauseOnMine.get(), pauseOnEat.get(), pauseOnDrink.get());
    }

    public boolean canBreak(int slot, BlockPos blockPos) {
        if (progress >= 1) return true;
        BlockState blockState = mc.world.getBlockState(blockPos);

        if (progress < 1)
            progress += getBreakDelta(slot != 420 ? slot : mc.player.getInventory().selectedSlot, blockState);
        return false;
    }

    private double getBreakDelta(int slot, BlockState state) {
        float hardness = state.getHardness(null, null);
        if (hardness == -1) return 0;
        else {
            return getBlockBreakingSpeed(slot, state) / hardness / (!state.isToolRequired() || mc.player.getInventory().main.get(slot).isSuitableFor(state) ? 30 : 100);
        }
    }

    private double getBlockBreakingSpeed(int slot, BlockState block) {
        double speed = mc.player.getInventory().main.get(slot).getMiningSpeedMultiplier(block);

        if (speed > 1) {
            ItemStack tool = mc.player.getInventory().getStack(slot);

            int efficiency = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, tool);

            if (efficiency > 0 && !tool.isEmpty()) speed += efficiency * efficiency + 1;
        }

        if (StatusEffectUtil.hasHaste(mc.player)) {
            speed *= 1 + (StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2F;
        }

        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float k = switch (mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };

            speed *= k;
        }

        if (mc.player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(mc.player)) {
            speed /= 5.0F;
        }

        if (!mc.player.isOnGround()) {
            speed /= 5.0F;
        }

        return speed;
    }

    public enum SwapMode{
        Off,
        Normal,
        Silent
    }

    public enum MineMode{
        Vanilla,
        Packet
    }

    public enum SortMode{
        Distance,
        Openest
    }

    public enum RenderMode{
        Off,
        Box,
        Smooth
    }
}
