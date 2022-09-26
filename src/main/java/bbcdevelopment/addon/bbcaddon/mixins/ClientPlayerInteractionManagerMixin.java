package bbcdevelopment.addon.bbcaddon.mixins;

import bbcdevelopment.addon.bbcaddon.event.BreakBlockEvent;
import bbcdevelopment.addon.bbcaddon.utils.world.WorldHelper;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin{
    @Inject(method = "breakBlock", at = @At("RETURN"))
    public void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!WorldHelper.canUpdate()) return;
        MeteorClient.EVENT_BUS.post(new BreakBlockEvent(pos));
    }
}
