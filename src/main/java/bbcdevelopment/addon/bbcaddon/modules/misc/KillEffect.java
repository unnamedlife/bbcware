package bbcdevelopment.addon.bbcaddon.modules.misc;

import bbcdevelopment.addon.bbcaddon.BBCAddon;
import bbcdevelopment.addon.bbcaddon.modules.BBCModule;
import bbcdevelopment.addon.bbcaddon.utils.security.Initialization;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.io.File;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;

public class KillEffect extends BBCModule {
    public KillEffect(){
        super(BBCAddon.Misc, "Effects", "");
    }

    private static final File SOUNDS_FOLDER = new File(BBCAddon.FOLDER, "sounds");
    private static final String DEFAULT_SOUND = "finalfantasy.mp3";

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final SettingGroup sgThunder = settings.createGroup("Thunder");
    private final SettingGroup sgTitle = settings.createGroup("Title");
    private final SettingGroup sgSound = settings.createGroup("Sound");

    private final Setting<Boolean> ignoreSelf = sgGeneral.add(new BoolSetting.Builder().name("ignore-self").defaultValue(true).build());
    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder().name("ignore-friends").defaultValue(true).build());
    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder().name("range").defaultValue(15).range(0, 200).build());

    private final Setting<Boolean> thunder = sgThunder.add(new BoolSetting.Builder().name("thunder").defaultValue(true).build());
    private final Setting<Integer> count = sgThunder.add(new IntSetting.Builder().name("count").defaultValue(100).range(1, 200).sliderRange(1, 200).build());
    public final Setting<Double> scale = sgThunder.add(new DoubleSetting.Builder().name("scale").defaultValue(0.05).range(0.01, 2).build());
    public final Setting<Double> scaleY = sgThunder.add(new DoubleSetting.Builder().name("scale-y").defaultValue(1).range(0, 3).build());
    private final Setting<Double> offset = sgThunder.add(new DoubleSetting.Builder().name("offset").defaultValue(0.5).range(0, 2).build());

    private final Setting<Boolean> title = sgTitle.add(new BoolSetting.Builder().name("title").defaultValue(true).build());
    private final Setting<String> text = sgTitle.add(new StringSetting.Builder().name("text").defaultValue("Good night {player}!").build());
    private final Setting<String> subText = sgTitle.add(new StringSetting.Builder().name("subText").defaultValue("ez?").build());
    private final Setting<Formatting> textFormatting = sgTitle.add(new EnumSetting.Builder<Formatting>().name("text-formatting").defaultValue(Formatting.GREEN).build());
    private final Setting<Formatting> subTextFormatting = sgTitle.add(new EnumSetting.Builder<Formatting>().name("subText-formatting").defaultValue(Formatting.GREEN).build());
    private final Setting<Formatting> playerFormatting = sgTitle.add(new EnumSetting.Builder<Formatting>().name("player-formatting").defaultValue(Formatting.AQUA).build());

    private final ArrayList<PlayerEntity> playersDead = new ArrayList<>();

    @Override
    public void onActivate() {


        playersDead.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event){
        if (mc.world == null) {
            playersDead.clear();
            return;
        }

        try {
            mc.world.getPlayers().forEach(this::create);
        }catch (ConcurrentModificationException ignored){}
    }

    public void create(PlayerEntity player){
        if (mc.player.distanceTo(player) > range.get()) return;
        if (ignoreSelf.get() && player == mc.player) return;
        if (ignoreFriends.get() && Friends.get().isFriend(player)) return;

        if (playersDead.contains(player)) {
            if (player.getHealth() > 0)
                playersDead.remove(player);
        }
        else {
            if (player.getHealth() == 0){
                if (thunder.get()){
                    for(int i = 0; i < count.get(); i++){
                        LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
                        Random random = new Random();
                        double x = player.getX() + random.nextDouble(-offset.get(), offset.get());
                        double y = player.getY() + random.nextDouble(-offset.get(), offset.get());
                        double z = player.getZ() + random.nextDouble(-offset.get(), offset.get());
                        lightningEntity.setPosition(x, y, z);

                        mc.world.addEntity(lightningEntity.getId(), lightningEntity);
                    }
                }
                if (title.get()){
                    mc.inGameHud.setTitle(Text.of(textFormatting.get() + getString(text.get(), player)));
                    mc.inGameHud.setSubtitle(Text.of(subTextFormatting.get() + subText.get()));
                }
                playersDead.add(player);
            }
        }
    }


    private String getString(String s, PlayerEntity player){
        if (s.contains("{player}")) s = s.replace("{player}", playerFormatting.get() + player.getEntityName() + textFormatting.get());
        return s;
    }

    public String[] getAvailableSounds() {
        List<String> sounds = new ArrayList<>(1);

        File[] files = SOUNDS_FOLDER.listFiles(File::isFile);
        if (files != null) {
            for (File file : files) {
                int i = file.getName().lastIndexOf('.');
                if (file.getName().substring(i).equals(".mp3")) {
                    sounds.add(file.getName().substring(0, i));
                }
            }
        }

        return sounds.toArray(new String[0]);
    }
}
