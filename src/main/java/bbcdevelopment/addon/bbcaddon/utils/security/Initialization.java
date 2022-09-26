package bbcdevelopment.addon.bbcaddon.utils.security;

import bbcdevelopment.addon.bbcaddon.modules.BBCModule;
import bbcdevelopment.addon.bbcaddon.utils.world.DamageHelper;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Initialization {
    private static final List<Module> moduleList = new ArrayList<>();

    // executor.stop() breaks everything
    public static Thread executor = Thread.currentThread();

    public static void init(){
        modules();
        utils();
        prefix();
    }

    private static void prefix(){
        ChatUtils.registerCustomPrefix("bbcdevelopment.addon.bbcaddon", Initialization::getPrefix);
    }

    private static void utils(){
        DamageHelper.init();
    }

    private static void modules(){
        Set<Class<? extends BBCModule>> reflections = new Reflections("bbcdevelopment.addon.bbcaddon.modules").getSubTypesOf(BBCModule.class);

        reflections.forEach(aClass -> {
            try {
                moduleList.add(aClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                Initialization.executor.stop();
                throw new RuntimeException(e);
            }
        });

        moduleList.forEach(module -> Modules.get().add(module));
    }

    public static Text getPrefix() {
        MutableText logo = Text.literal("BBC");
        MutableText prefix = Text.literal("");
        logo.setStyle(logo.getStyle().withFormatting(Formatting.DARK_GRAY));
        prefix.setStyle(prefix.getStyle().withFormatting(Formatting.DARK_GRAY));
        prefix.append("[");
        prefix.append(logo);
        prefix.append("] ");
        return prefix;
    }
}
