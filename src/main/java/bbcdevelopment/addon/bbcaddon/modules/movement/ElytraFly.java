package bbcdevelopment.addon.bbcaddon.modules.movement;

import bbcdevelopment.addon.bbcaddon.BBCAddon;
import bbcdevelopment.addon.bbcaddon.event.ElytraMoveEvent;
import bbcdevelopment.addon.bbcaddon.modules.BBCModule;
import bbcdevelopment.addon.bbcaddon.utils.security.Initialization;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends BBCModule {
    public ElytraFly(){
        super(BBCAddon.Movement, "NCP ElyFly", "");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBoost = settings.createGroup("Boost");
    private final SettingGroup sgAutoBoost = settings.createGroup("Auto Boost");


    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>().name("mode").defaultValue(Mode.Boost).build());

    private final Setting<Double> boost = sgBoost.add(new DoubleSetting.Builder().name("boost").defaultValue(0.4).range(0.1, 10).build());
    private final Setting<Boolean> limit = sgBoost.add(new BoolSetting.Builder().name("limit").defaultValue(true).build());
    private final Setting<Boolean> safePitch = sgBoost.add(new BoolSetting.Builder().name("safe-pitch").defaultValue(true).build());
    private final Setting<Integer> minPitch = sgBoost.add(new IntSetting.Builder().name("pitch").defaultValue(0).range(-90, 90).sliderRange(-90, 90).visible(safePitch::get).build());
    private final Setting<Integer> pitch = sgBoost.add(new IntSetting.Builder().name("pitch").defaultValue(-45).range(-90, 90).sliderRange(-90, 90).visible(safePitch::get).build());
    private final Setting<Double> maxBoostSpeed = sgBoost.add(new DoubleSetting.Builder().name("max-speed-boost").defaultValue(30).range(0, 200).build());
    private final Setting<Boolean> customMultiple = sgBoost.add(new BoolSetting.Builder().name("custom-multiple").defaultValue(true).build());
    private final Setting<Double> multiple = sgBoost.add(new DoubleSetting.Builder().name("multiple").defaultValue(0.5).range(0.01, 1).visible(customMultiple::get).build());

    private final Setting<Integer> targetY = sgAutoBoost.add(new IntSetting.Builder().name("target-y").defaultValue(130).range(-63, 400).sliderRange(0, 400).build());


    private int teleportId;

    private AutoBoostStage autoBoostStage = AutoBoostStage.Boost;

    @Override
    public void onActivate() {

    }

    @EventHandler
    private void onElytraMove(ElytraMoveEvent event) {
        switch (mode.get()){
            case Boost, AutoBoost -> {
                float yaw = (float) Math.toRadians(mc.player.getYaw());
                double speedX = 0;
                double speedZ = 0;

                if (mc.options.forwardKey.isPressed()){
                    speedX = -MathHelper.sin(yaw) * boost.get() / 20;
                    speedZ = MathHelper.cos(yaw) * boost.get() / 20;
                }
                else if(mc.options.backKey.isPressed()) {
                    speedX = MathHelper.sin(yaw) * boost.get() / 20;
                    speedZ = -MathHelper.cos(yaw) * boost.get() / 20;
                }

                Vec3d vec3d = mc.player.getVelocity();
                double length = vec3d.length() * 20;

                if (length > maxBoostSpeed.get() && limit.get()){
                    double factor = maxBoostSpeed.get() / length;
                    if (customMultiple.get()) factor = multiple.get();
                    mc.player.getVelocity().multiply(factor);
                }
                else {
                    if (mode.get() == Mode.Boost && (mc.player.getPitch() >= minPitch.get() && safePitch.get() || !safePitch.get())) mc.player.addVelocity(speedX, 0, speedZ);
                    else {
                        targetY.get();
                    }
                }
            }
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event){
        if (event.packet instanceof PlayerPositionLookS2CPacket packet){
            if (mc.player.isAlive()){
                if (this.teleportId <= 0) {
                    this.teleportId = ((PlayerPositionLookS2CPacket) event.packet).getTeleportId();
                } else {
                    if (mc.world.isPosLoaded(mc.player.getBlockX(), mc.player.getBlockZ())){
                        mc.player.setPitch(minPitch.get());
                    }
                }
            }
            teleportId = packet.getTeleportId();
        }
    }

    public enum AutoBoostStage{
        Boost,
        GeyUp,
        GetDown
    }

    public enum Mode{
        Boost, AutoBoost
    }
}
