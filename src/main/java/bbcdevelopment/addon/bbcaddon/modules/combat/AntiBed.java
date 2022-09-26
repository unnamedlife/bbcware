package bbcdevelopment.addon.bbcaddon.modules.combat;

import bbcdevelopment.addon.bbcaddon.BBCAddon;
import bbcdevelopment.addon.bbcaddon.modules.BBCModule;
import bbcdevelopment.addon.bbcaddon.utils.player.InvHelper;
import bbcdevelopment.addon.bbcaddon.utils.security.Initialization;
import bbcdevelopment.addon.bbcaddon.utils.world.BlockHelper;
import bbcdevelopment.addon.bbcaddon.utils.world.BlockPosX;
import bbcdevelopment.addon.bbcaddon.utils.world.DamageHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public class AntiBed extends BBCModule {
    public AntiBed(){
        super(BBCAddon.Combat, "anti-bed+", "");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgBreak = settings.createGroup("Break");
    private final SettingGroup sgBlow = settings.createGroup("Blow");

    private final Setting<Boolean> antiLay = sgGeneral.add(new BoolSetting.Builder().name("anti-lay").defaultValue(true).build());
    private final Setting<Boolean> onlyInNether = sgGeneral.add(new BoolSetting.Builder().name("only-in-nether").defaultValue(true).build());
    private final Setting<SwapMode> swapMode = sgGeneral.add(new EnumSetting.Builder<SwapMode>().name("swap-mode").description("Slot swap method.").defaultValue(SwapMode.Silent).build());
    private final Setting<Boolean> syncSlot = sgGeneral.add(new BoolSetting.Builder().name("sync-slot").description("Synchronize the slot to get rid of fakes.").defaultValue(true).build());
    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder().name("swing").defaultValue(true).build());
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder().name("rotate").defaultValue(true).build());

    private final Setting<Boolean> placeBlock = sgPlace.add(new BoolSetting.Builder().name("place-block").defaultValue(false).build());
    private final Setting<Boolean> slabsMiddle = sgPlace.add(new BoolSetting.Builder().name("slabs-middle").defaultValue(false).build());
    private final Setting<Boolean> carpetBottom = sgPlace.add(new BoolSetting.Builder().name("carpet-bottom").defaultValue(false).build());

    private final Setting<List<Block>> blocks = sgPlace.add(new BlockListSetting.Builder().name("blocks").defaultValue(Blocks.COBWEB).filter(this::BlockFilter).build());
    private final Setting<Boolean> onlyInHole = sgPlace.add(new BoolSetting.Builder().name("only-in-hole").defaultValue(true).build());
    private final Setting<Boolean> blockTop = sgPlace.add(new BoolSetting.Builder().name("block-top").description("Places string above you.").defaultValue(false).build());
    private final Setting<Boolean> blockMiddle = sgPlace.add(new BoolSetting.Builder().name("block-middle").description("Places string in your upper hitbox.").defaultValue(true).build());
    private final Setting<Boolean> blockBottom =sgPlace.add(new BoolSetting.Builder().name("block-bottom").description("Places string at your feet.").defaultValue(false).build());

    private final Setting<PlaceMode> placeMode = sgPlace.add(new EnumSetting.Builder<PlaceMode>().name("place-mode").description("place method.").defaultValue(PlaceMode.OnBed).build());

    private final Setting<Boolean> breakBeds = sgBreak.add(new BoolSetting.Builder().name("break-beds").defaultValue(true).build());
    private final Setting<Boolean> useAxe = sgBreak.add(new BoolSetting.Builder().name("use-axe").defaultValue(true).build());
     private final Setting<Boolean> instant = sgBreak.add(new BoolSetting.Builder().name("instant").defaultValue(true).build());
    private final Setting<Integer> tickDelay = sgBreak.add(new IntSetting.Builder().name("tick-delay").defaultValue(0).range(0, 20).build());

    private final Setting<Boolean> blowBeds = sgBlow.add(new BoolSetting.Builder().name("blow-beds").defaultValue(false).build());
    private final Setting<Boolean> antiSelfPop = sgBlow.add(new BoolSetting.Builder().name("anti-self-pop").defaultValue(true).build());

    private boolean isStarted;
    private BlockPosX currentPos;
    private int ticks;

    private BlockPosX placePosX;

    @Override
    public void onActivate() {

    }

    @EventHandler
    private void onLayTick(TickEvent.Post event){
        if (!antiLay.get() || (onlyInNether.get() && mc.world.getDimension().bedWorks())) return;
        if (mc.player.getInventory().getArmorStack(2).getItem() == Items.ELYTRA && mc.player.isFallFlying()) return;
        mc.player.setPose(EntityPose.STANDING);
    }

    @EventHandler
    private void onPlaceTick(TickEvent.Post event){
        if (!placeBlock.get() || (onlyInNether.get() && mc.world.getDimension().bedWorks())) return;
        if (placeMode.get() == PlaceMode.OnBed && placePosX == null) return;
        if (placeMode.get() == PlaceMode.OnBed && !BlockFilter(placePosX.state().getBlock()) && !(placePosX.air())) return;

        BlockPosX pos = new BlockPosX(mc.player.getBlockPos());

        if (slabsMiddle.get() && (placeMode.get() == PlaceMode.Always || equals(placePosX, pos.up()))){
            boolean wasSneak = mc.player.isSneaking();
            mc.player.setSneaking(true);

            if (placeMode.get() == PlaceMode.Always) placePosX = pos.up();

            place(placePosX, InvUtils.find(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof SlabBlock));

            if (placePosX.of(SlabBlock.class)){
                mc.player.setSneaking(wasSneak);
                placePosX = null;
            }
        }
        else if (carpetBottom.get() && (placeMode.get() == PlaceMode.Always) || equals(placePosX, pos)){
            boolean wasSneak = mc.player.isSneaking();
            mc.player.setSneaking(true);
            mc.player.jump();

            if (placeMode.get() == PlaceMode.Always) placePosX = pos;

            place(placePosX, InvUtils.find(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof CarpetBlock));

            if (placePosX.of(CarpetBlock.class)){
                mc.player.setSneaking(wasSneak);
                placePosX = null;
            }
        }
        else {
            if (onlyInHole.get() && !PlayerUtils.isInHole(true)) return;
            boolean shouldWork = false;

            for (int i = 0; i < 3; ++i){
                if (!pos.up(i).air()) continue;
                if ((placeMode.get() == PlaceMode.Always || (equals(placePosX, pos.add(0, i, 0)))) && shouldWork(i)) {
                    shouldWork = true;
                    if (placeMode.get() == PlaceMode.Always) placePosX = pos.up(i);
                }
            }

            if (!shouldWork) return;

            FindItemResult item = getInvBlock();
            if (!item.found()) return;

            place(placePosX, item);
            placePosX = null;
        }
    }


    @EventHandler
    private void onBreakTick(TickEvent.Post event){
        if (!breakBeds.get() || (onlyInNether.get() && mc.world.getDimension().bedWorks())) return;

        ticks--;
        BlockPos pos = mc.player.getBlockPos();
        BlockPosX bed = null;

        for (int i = 0; i < 3; ++i){
            BlockPosX testPos = new BlockPosX(pos.add(0, i, 0));
            if (testPos.of(BedBlock.class)){
                bed = testPos;
                break;
            }
        }

        if (bed == null) return;

        if (currentPos != null && !currentPos.equals(bed)){
            currentPos = bed;
            isStarted = false;
        }else currentPos = bed;

        FindItemResult itemResult = InvUtils.find(Items.NETHERITE_AXE, Items.DIAMOND_PICKAXE);

        if (itemResult.found() && useAxe.get()) {
            if (swapMode.get() == SwapMode.Silent || swapMode.get() == SwapMode.Normal && !itemResult.isOffhand() && !itemResult.isMain()) InvUtils.swap(itemResult.slot(), true);

            boolean holdsBed = (itemResult.isOffhand() || itemResult.isMainHand()) || swapMode.get() == SwapMode.Inventory;
            if (holdsBed){
                if (swapMode.get() == SwapMode.Inventory) {
                    BlockPosX finalBed = bed;
                    move(itemResult, () -> breakBed(finalBed));
                } else breakBed(bed);
            }
        }else breakBed(bed);

        if (placeBlock.get() && placeMode.get() == PlaceMode.OnBed) placePosX = bed;
    }

    @EventHandler
    private void onStartBreaking(StartBreakingBlockEvent event){
        if (event.blockPos.equals(currentPos)) isStarted = true;
    }

    @EventHandler
    private void onBlowTick(TickEvent.Post event){
        if (!blowBeds.get() || mc.world.getDimension().bedWorks()) return;
        BlockPos pos = mc.player.getBlockPos();
        BlockPosX bed = null;

        for (int i = 0; i < 3; ++i){
            BlockPosX testPos = new BlockPosX(pos.add(0, i, 0));

            if (testPos.of(BedBlock.class)){
                bed = testPos;
                break;
            }
        }

        if (bed == null) return;

        double selfDamage = DamageHelper.bedDamage(mc.player, bed, false);
        if (PlayerUtils.getTotalHealth() - selfDamage <= 0 && antiSelfPop.get()) return;

        blowBed(bed);
        if (placeBlock.get() && placeMode.get() == PlaceMode.OnBed) placePosX = bed;
    }

    private void breakBed(BlockPosX posX){
        if (!instant.get()){
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, posX, Direction.UP));
            if (swing.get()) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, posX, Direction.UP));
            if (swing.get()) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        else {
            if (ticks <= 0){
                ticks = tickDelay.get();
                if (!isStarted) {
                    BlockUtils.breakBlock(posX, false);
                    if (swing.get()) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
                else {
                    if (rotate.get()) Rotations.rotate(Rotations.getYaw(posX), Rotations.getPitch(posX), () -> mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, posX, Direction.UP)));
                     else mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, posX, Direction.UP));
                    mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }
        }
    }

    public boolean move(FindItemResult itemResult, Runnable runnable) {
        if (itemResult.isOffhand()) {
            runnable.run();
            return true;
        }

        move(mc.player.getInventory().selectedSlot, itemResult.slot());
        runnable.run();
        move(mc.player.getInventory().selectedSlot, itemResult.slot());
        return true;
    }

    private void move(int from, int to) {
        ScreenHandler handler = mc.player.currentScreenHandler;

        Int2ObjectArrayMap<ItemStack> stack = new Int2ObjectArrayMap<>();
        stack.put(to, handler.getSlot(to).getStack());

        mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(handler.syncId, handler.getRevision(), PlayerInventory.MAIN_SIZE + from, to, SlotActionType.SWAP, handler.getCursorStack().copy(), stack));
    }

    private void blowBed(BlockPosX posX){
        BlockHitResult breakResult = new BlockHitResult(posX.closestVec3d(), Direction.UP, posX, false);
        mc.interactionManager.interactBlock(mc.player, Hand.OFF_HAND, breakResult);
    }

    private void place(BlockPosX posX, FindItemResult itemResult){
        if (!itemResult.found() || posX == null) return;

        if (swapMode.get() == SwapMode.Silent || swapMode.get() == SwapMode.Normal && !itemResult.isOffhand() && !itemResult.isMain()) InvUtils.swap(itemResult.slot(), true);

        boolean holdsBed = (itemResult.isOffhand() || itemResult.isMainHand()) || swapMode.get() == SwapMode.Inventory;

        Runnable runnable = () -> {
            BlockHitResult placeResult = new BlockHitResult(posX.closestVec3d(), Direction.DOWN, posX, false);
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, placeResult, 0));
        };

        if (holdsBed){
            if (swapMode.get() == SwapMode.Inventory) {
                move(itemResult, runnable);
            } else runnable.run();
        }
        boolean canSilent = swapMode.get() == SwapMode.Silent || (itemResult.isHotbar() && swapMode.get() == SwapMode.Inventory);
        if (canSilent) {
            InvUtils.swapBack();
            if (syncSlot.get()) InvHelper.syncSlot();
        }
    }

    private FindItemResult getInvBlock() {
        return InvUtils.find(itemStack -> blocks.get().contains(Block.getBlockFromItem(itemStack.getItem())));
    }

    private boolean shouldWork(int i){
        if (i == 0 && blockBottom.get()) return true;
        if (i == 1 && blockMiddle.get()) return true;
        return i == 2 && blockTop.get();
    }

    private boolean BlockFilter(Block block){
        if (block.asItem() == Items.STRING) return true;
        if (block == Blocks.COBWEB) return true;
        if (BlockHelper.buttons.contains(block)) return true;
        return false;
    }

    public boolean equals(BlockPos b1, BlockPos b2){
        return b1.getX() == b2.getX() && b1.getY() == b2.getY() && b1.getZ() == b2.getZ();
    }

    public enum SwapMode{
        Off,
        Normal,
        Silent,
        Inventory
    }

    public enum PlaceMode{
        OnBed,
        Always
    }
}
