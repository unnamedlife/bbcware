package bbcdevelopment.addon.bbcaddon.utils.world;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Objects;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PacketUtils {
    public BlockPosX bp;

    //Пакет Майн с таймером
    private double progress = 0;

    public PacketUtils() {
        progress = 0;
    }

    public void reset() {
        progress = 0;
    }
    public double getProgress() {
        return progress;
    }
    public boolean isReady() {
        return progress >= 1;
    }
    public boolean isReadyOn(double var) {
        return progress >= var;
    }

    public void mine(BlockPos blockPos, Task task, String packet) {
        if (blockPos == null) return;
        bp = new BlockPosX(blockPos);

        task.run(() -> {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, bp.get(), Direction.UP));
            if (Objects.equals(packet, "Instant")) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, bp.get(), Direction.UP));
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        });

        BlockState blockState = bp.state();
        double bestScore = -1;
        int bestSlot = -1;

        for (int i = 0; i < 9; i++) {
            double score = mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(blockState);

            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        progress += getBreakDelta(bestSlot != -1 ? bestSlot : mc.player.getInventory().selectedSlot, blockState);
    }

    public void abortMining(BlockPos blockPos) {
        if (blockPos == null) return;

        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, Direction.UP));
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
}
