package bbcdevelopment.addon.bbcaddon.mixins;


import bbcdevelopment.addon.bbcaddon.event.PlayerMoveEvent;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.LivingEntityMoveEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "move", at = @At("HEAD"))
    private void onMove(MovementType type, Vec3d movement, CallbackInfo info) {
        if ((Object) this == mc.player) {
            PlayerMoveEvent event = PlayerMoveEvent.get(type, movement);
            MeteorClient.EVENT_BUS.post(PlayerMoveEvent.get(type, movement));
            if (event.isCancelled()) event.cancel();
        }
    }
}
