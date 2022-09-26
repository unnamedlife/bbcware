package bbcdevelopment.addon.bbcaddon.mixins;


import bbcdevelopment.addon.bbcaddon.event.ElytraMoveEvent;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", ordinal = 2))
    public void elytraMove(LivingEntity livingEntity, MovementType movementType, Vec3d vec3d) {
        if (!Utils.canUpdate()) return;
        ElytraMoveEvent elytraMoveEvent = MeteorClient.EVENT_BUS.post(new ElytraMoveEvent(vec3d.x, vec3d.y, vec3d.z));
        livingEntity.move(movementType, new Vec3d(elytraMoveEvent.x, elytraMoveEvent.y, elytraMoveEvent.z));
    }
}
