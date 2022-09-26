package bbcdevelopment.addon.bbcaddon;

import bbcdevelopment.addon.bbcaddon.utils.security.Initialization;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;

public class BBCAddon extends MeteorAddon {
	public static final Logger LOG = LoggerFactory.getLogger(BBCAddon.class);

    public static final String MOD_ID = "bbcaddon";
    public static final String ADDON = "BBC";

    public static final String VERSION = "v.1.0.4";
    public static final File FOLDER = new File(FabricLoader.getInstance().getGameDir().toString(), MOD_ID);
    public static final File RECORDINGS = new File(FOLDER, "recordings");

    public static final Category Combat = new Category("BBCPVP+", Items.RED_BED.getDefaultStack());
    public static final Category Movement = new Category("MOVMNT+", Items.PURPLE_BED.getDefaultStack());
    public static final Category Misc = new Category("MSCLNS+", Items.BLUE_BED.getDefaultStack());
    public static final Category Info = new Category("XXXTRA+", Items.WHITE_BED.getDefaultStack());

	@Override
	public void onInitialize() {
		LOG.info("Initializing BBC Addon... Welcome!");

		MeteorClient.EVENT_BUS.registerLambdaFactory("bbcdevelopment.addon.bbcaddon", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        if (!FOLDER.exists()) {
            FOLDER.getParentFile().mkdirs();
            FOLDER.mkdir();
        }

        Initialization.init();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Saving config...");
            Config.get().save();
            LOG.info("Thanks for using " + ADDON + " " + VERSION + "! Don't forget to join our discord -> https://discord.gg/UbuM7Cxtew");
        }));
	}

	@Override
	public void onRegisterCategories() {
		Modules.registerCategory(Combat);
		Modules.registerCategory(Movement);
		Modules.registerCategory(Info);
		Modules.registerCategory(Misc);
	}

    @Override
    public String getPackage() {
        return "bbcdevelopment.addon.bbcaddon";
    }
}
