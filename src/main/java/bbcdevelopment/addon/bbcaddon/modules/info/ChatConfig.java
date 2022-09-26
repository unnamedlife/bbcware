package bbcdevelopment.addon.bbcaddon.modules.info;

import bbcdevelopment.addon.bbcaddon.BBCAddon;
import bbcdevelopment.addon.bbcaddon.modules.BBCModule;
import bbcdevelopment.addon.bbcaddon.utils.security.Initialization;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChatConfig extends BBCModule {
    public final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> noMeteorPrefix = sgGeneral.add(new BoolSetting.Builder().name("no-meteor-prefix").description(".").defaultValue(true).build());
    public final Setting<Boolean> chatFormatting = sgGeneral.add(new BoolSetting.Builder().name("chat-formatting").description("Changes style of messages.").defaultValue(false).build());
    private final Setting<ChatFormatting> formattingMode = sgGeneral.add(new EnumSetting.Builder<ChatFormatting>().name("mode").description("The style of messages.").defaultValue(ChatFormatting.Bold).visible(chatFormatting::get).build());

    public ChatConfig() {
        super(BBCAddon.Info, "chat-config", "The way to render chat messages.");
    }

    @Override
    public void onActivate() {

    }

    @EventHandler
    public void chatFormatting(PacketEvent.Receive event) {
        if (!(event.packet instanceof GameMessageS2CPacket) || !chatFormatting.get()) return;
        Text message = ((GameMessageS2CPacket) event.packet).content();

        mc.inGameHud.getChatHud().addMessage(Text.literal("").setStyle(Style.EMPTY.withFormatting(getFormatting(formattingMode.get()))).append(message));
        event.cancel();
    }

    private Formatting getFormatting(ChatFormatting chatFormatting) {
        return switch (chatFormatting) {
            case Obfuscated -> Formatting.OBFUSCATED;
            case Bold -> Formatting.BOLD;
            case Strikethrough -> Formatting.STRIKETHROUGH;
            case Underline -> Formatting.UNDERLINE;
            case Italic -> Formatting.ITALIC;
        };
    }

    public enum ChatFormatting {
        Obfuscated, Bold, Strikethrough, Underline, Italic
    }
}
