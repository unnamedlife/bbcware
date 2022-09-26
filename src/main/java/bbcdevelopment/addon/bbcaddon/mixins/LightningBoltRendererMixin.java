package bbcdevelopment.addon.bbcaddon.mixins;

import bbcdevelopment.addon.bbcaddon.modules.misc.KillEffect;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LightningEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningEntityRenderer.class)
public abstract class LightningBoltRendererMixin {
    @Inject(method = "render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void render(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci){
        if (entity == null) return;
        KillEffect killEffect = Modules.get().get(KillEffect.class);
        if (!killEffect.isActive()) return;
        matrices.scale(killEffect.scale.get().floatValue(), killEffect.scaleY.get().floatValue(), killEffect.scale.get().floatValue());
    }
}
