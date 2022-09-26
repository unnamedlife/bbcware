package bbcdevelopment.addon.bbcaddon.mixins.meteor;

import bbcdevelopment.addon.bbcaddon.modules.info.ChatConfig;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatUtils.class)
public class ChatUtilsMixin {
    @Inject(method = "getPrefix", at = @At("HEAD"), cancellable = true, remap = false)
    private static void getPrefix(CallbackInfoReturnable<Text> cir) {
        if (!Modules.get().isActive(ChatConfig.class)) return;
        if (Modules.get().get(ChatConfig.class).noMeteorPrefix.get()) {
            MutableText logo = Text.literal("BBC");
            MutableText prefix = Text.literal("");
            logo.setStyle(logo.getStyle().withFormatting(Formatting.DARK_GRAY));
            prefix.setStyle(prefix.getStyle().withFormatting(Formatting.DARK_GRAY));
            prefix.append("[");
            prefix.append(logo);
            prefix.append("] ");
            cir.setReturnValue(prefix);
        }
    }
}
