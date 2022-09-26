package bbcdevelopment.addon.bbcaddon.modules.combat;

import bbcdevelopment.addon.bbcaddon.BBCAddon;
import bbcdevelopment.addon.bbcaddon.modules.BBCModule;
import bbcdevelopment.addon.bbcaddon.utils.player.InvHelper;
import bbcdevelopment.addon.bbcaddon.utils.security.Initialization;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class QuickMend extends BBCModule {
    private final SettingGroup sgAutomation = settings.createGroup("Automation");
    public final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgMisc = settings.createGroup("Misc");

    private final Setting<Boolean> autoMend = sgAutomation.add(new BoolSetting.Builder().name("auto-mend").defaultValue(false).build());
    private final Setting<Integer> takenDamage = sgAutomation.add(new IntSetting.Builder().name("taken-damage").defaultValue(100).range(0, 500).visible(autoMend::get).build());

    private final Setting<SwapMode> swapMode = sgGeneral.add(new EnumSetting.Builder<SwapMode>().name("swap").defaultValue(SwapMode.Inventory).build());
    private final Setting<Boolean> refill = sgGeneral.add(new BoolSetting.Builder().name("refill").defaultValue(false).visible(() -> swapMode.get() != SwapMode.Inventory).build());
    private final Setting<Integer> refillSlot = sgGeneral.add(new IntSetting.Builder().name("refill-slot").defaultValue(8).range(1, 9).visible(() -> swapMode.get() != SwapMode.Inventory && refill.get()).build());
    private final Setting<Boolean> syncSlot = sgGeneral.add(new BoolSetting.Builder().name("sync-slot").defaultValue(true).build());
    private final Setting<Boolean> autoToggle = sgGeneral.add(new BoolSetting.Builder().name("auto-toggle").defaultValue(true).visible(() -> !autoMend.get()).build());
    private final Setting<Double> minHealth = sgGeneral.add(new DoubleSetting.Builder().name("min-health").defaultValue(5).range(0, 36).build());
    private final Setting<Boolean> customPitch = sgGeneral.add(new BoolSetting.Builder().name("custom-pitch").defaultValue(false).build());
    private final Setting<Integer> pitchValue = sgGeneral.add(new IntSetting.Builder().name("pitch").defaultValue(90).range(-90, 90).visible(customPitch::get).build());

    private final Setting<Boolean> onlyOnHole = sgMisc.add(new BoolSetting.Builder().name("only-on-hole").defaultValue(true).build());
    private final Setting<Boolean> onlyOnGround = sgMisc.add(new BoolSetting.Builder().name("only-on-ground").defaultValue(true).build());
    private final Setting<Boolean> pauseOnEat = sgMisc.add(new BoolSetting.Builder().name("pause-on-eat").description("Pause placing while eating.").defaultValue(false).build());
    private final Setting<Boolean> pauseOnDrink = sgMisc.add(new BoolSetting.Builder().name("pause-on-drink").description("Pause placing while drinking.").defaultValue(false).build());
    private final Setting<Boolean> pauseOnMine = sgMisc.add(new BoolSetting.Builder().name("pause-on-mine").description("Pause placing while mining.").defaultValue(false).build());

    public QuickMend(){
        super(BBCAddon.Combat, "quick-mend", "");
    }

    @Override
    public void onActivate() {

    }

    @EventHandler
    private void onTick(TickEvent.Post event){
        if (onlyOnHole.get() && !PlayerUtils.isInHole(true)) return;
        if (onlyOnGround.get() && !mc.player.isOnGround()) return;
        if (PlayerUtils.shouldPause(pauseOnMine.get(), pauseOnEat.get(), pauseOnDrink.get())) return;
        if (PlayerUtils.getTotalHealth() < minHealth.get()) return;
        if (!canMend()) return;

        if (refill.get() && swapMode.get() != SwapMode.Inventory) doRefill();

        if (!autoMend.get()){
            if (autoToggle.get()){
                boolean shouldThrow = false;
                for (ItemStack itemStack : mc.player.getInventory().armor) {
                    if (itemStack.isEmpty()) continue;
                    if (EnchantmentHelper.getLevel(Enchantments.MENDING, itemStack) < 1) continue;
                    if (itemStack.isDamaged()) {
                        shouldThrow = true;
                        break;
                    }
                }
                if (!shouldThrow) {
                    toggle();
                    return;
                }
            }
            doMend();
        }
        else {
            for (ItemStack itemStack : mc.player.getInventory().armor) {
                if (itemStack.isEmpty()) continue;
                if (EnchantmentHelper.getLevel(Enchantments.MENDING, itemStack) < 1) continue;
                if (itemStack.getDamage() >= takenDamage.get()) doMend();
            }
        }
    }

    private void doMend(){
        float pitch = 90;
        if (customPitch.get()) pitch = pitchValue.get();

        float yaw = mc.player.getYaw();

        Rotations.rotate(yaw, pitch);

        FindItemResult bottle = InvUtils.find(Items.EXPERIENCE_BOTTLE);

        if (swapMode.get() == SwapMode.Silent || swapMode.get() == SwapMode.Normal && !bottle.isOffhand() && !bottle.isMain()){
            InvUtils.swap(bottle.slot(), true);
        }
        boolean holdsBed = (bottle.isOffhand() || bottle.isMainHand()) || swapMode.get() == SwapMode.Inventory;

        Hand hand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL ? Hand.OFF_HAND : Hand.MAIN_HAND;

        if (holdsBed){
            mc.player.swingHand(mc.player.getActiveHand());
            if (swapMode.get() == SwapMode.Inventory) {
                move(bottle, () -> mc.interactionManager.interactItem(mc.player, hand));
            } else mc.interactionManager.interactItem(mc.player, hand);;
        }

        boolean canSilent = swapMode.get() == SwapMode.Silent || (bottle.isHotbar() && swapMode.get() == SwapMode.Inventory);
        if (canSilent) {
            InvUtils.swapBack();
            if (syncSlot.get()) InvHelper.syncSlot();
        }
    }

    private void doRefill(){
        FindItemResult bottle = InvUtils.find(Items.EXPERIENCE_BOTTLE);
        if (!bottle.found() || bottle.slot() == refillSlot.get() - 1) return;
        InvUtils.move().from(bottle.slot()).toHotbar(refillSlot.get() - 1);
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

    private boolean canMend(){
        boolean canMend = false;
        boolean hasBottle = InvUtils.find(Items.EXPERIENCE_BOTTLE).found();

        for (ItemStack itemStack : mc.player.getInventory().armor) {
            if (itemStack.isEmpty()) continue;
            if (EnchantmentHelper.getLevel(Enchantments.MENDING, itemStack) >= 1) canMend = true;
        }

        return canMend && hasBottle;
    }

    public enum SwapMode{
        Off,
        Normal,
        Silent,
        Inventory
    }
}
