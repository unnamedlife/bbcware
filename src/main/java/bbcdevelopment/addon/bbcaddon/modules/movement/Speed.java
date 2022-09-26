package bbcdevelopment.addon.bbcaddon.modules.movement;

import bbcdevelopment.addon.bbcaddon.BBCAddon;
import bbcdevelopment.addon.bbcaddon.event.PlayerMoveEvent;
import bbcdevelopment.addon.bbcaddon.mixins.PlayerPositionLookS2CPacketAccessor;
import bbcdevelopment.addon.bbcaddon.modules.BBCModule;
import bbcdevelopment.addon.bbcaddon.utils.math.TimerUtils;
import bbcdevelopment.addon.bbcaddon.utils.player.PlayerHelper;
import bbcdevelopment.addon.bbcaddon.utils.security.Initialization;
import bbcdevelopment.addon.bbcaddon.utils.world.BlockHelper;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Anchor;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Speed extends BBCModule {
    public Speed(){
        super(BBCAddon.Movement, "Speedy", "");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBoost = settings.createGroup("Boost");
    private final SettingGroup sgActions = settings.createGroup("Actions");
    private final SettingGroup sgMisc = settings.createGroup("Misc");

    private final Setting<Double> baseSpeedValue = sgGeneral.add(new DoubleSetting.Builder().name("base-speed").defaultValue(0.316).range(0, 3).sliderRange(0, 10).build());
    private final Setting<JumpSpeed> jumpMode = sgGeneral.add(new EnumSetting.Builder<JumpSpeed>().name("jump-mode").description("").defaultValue(JumpSpeed.Custom).build());
    public final Setting<Double> jumpValue = sgGeneral.add(new DoubleSetting.Builder().name("jump-height").defaultValue(0.3).min(0.01).sliderMin(0.01).sliderMax(3).visible(() -> jumpMode.get() == JumpSpeed.Custom).build());
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>().name("mode").description("").defaultValue(Mode.Strafe).build());
    private final Setting<FrictionMode> frictionMode = sgGeneral.add(new EnumSetting.Builder<FrictionMode>().name("friction-mode").defaultValue(FrictionMode.Fast).build());
    public final Setting<Double> timer = sgGeneral.add(new DoubleSetting.Builder().name("timer").description("Timer override.").defaultValue(1.101).min(0.01).sliderMin(0.01).sliderMax(10).build());
    public final Setting<Boolean> resetTimer = sgGeneral.add(new BoolSetting.Builder().name("reset-timer").defaultValue(true).build());
    public final Setting<Integer> setbackDelay = sgGeneral.add(new IntSetting.Builder().name("setback-delay").defaultValue(40).range(0, 60).build());
    private final Setting<Boolean> retain = sgGeneral.add(new BoolSetting.Builder().name("retain").defaultValue(false).build());
    private final Setting<Boolean> airStrafe = sgGeneral.add(new BoolSetting.Builder().name("air-strafe").defaultValue(true).build());
    private final Setting<Boolean> autoJump = sgGeneral.add(new BoolSetting.Builder().name("auto-jump").defaultValue(false).build());

    private final Setting<Boolean> boost = sgBoost.add(new BoolSetting.Builder().name("boost").defaultValue(true).build());
    public final Setting<Double> multiply = sgBoost.add(new DoubleSetting.Builder().name("Multiply").defaultValue(0.117D).min(0.1D).max(1D).build());
    public final Setting<Double> max = sgBoost.add(new DoubleSetting.Builder().name("Max").defaultValue(0.5D).min(0.166D).max(3D).build());


    private final Setting<Boolean> strictSprint = sgActions.add(new BoolSetting.Builder().name("strict-sprint").defaultValue(false).build());
    private final Setting<Boolean> strictJump = sgActions.add(new BoolSetting.Builder().name("strict-jump").defaultValue(false).build());
    private final Setting<Boolean> strictCollision = sgActions.add(new BoolSetting.Builder().name("strict-collision").defaultValue(false).build());

    private final Setting<Boolean> inLiquids = sgMisc.add(new BoolSetting.Builder().name("in-liquids").description("Uses speed when in lava or water.").defaultValue(false).build());
    private final Setting<Boolean> whenSneaking = sgMisc.add(new BoolSetting.Builder().name("when-sneaking").description("Uses speed when sneaking.").defaultValue(false).build());



    private double playerSpeed;
    private double latestMoveSpeed;
    private double boostSpeed;

    private int timersTick;
    private boolean accelerate;
    private boolean offsetPackets;

    private int strictTicks;

    private StrafeStage strafeStage = StrafeStage.Speed;
    private GroundStage groundStage = GroundStage.CheckSpace;

    private final TimerUtils setbackTimer = new TimerUtils();
    private final TimerUtils boostTimer = new TimerUtils();

    private int teleportId;

    @Override
    public void onActivate() {

    }

    @Override
    public void onDeactivate() {
        Modules.get().get(Timer.class).setOverride(Timer.OFF);
    }

    @EventHandler
    private void onTick(TickEvent.Post event){
        latestMoveSpeed = Math.sqrt(StrictMath.pow(mc.player.getX() - mc.player.prevX, 2) + StrictMath.pow(mc.player.getZ() - mc.player.prevZ, 2));
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event){
        event.setCancelled(true);
        if (!setbackTimer.passedTicks(setbackDelay.get())) return;
        if (!whenSneaking.get() && mc.player.isSneaking()) return;
        if (!inLiquids.get() && (mc.player.isTouchingWater() || mc.player.isInLava())) return;

        double baseSpeed = PlayerHelper.getBaseMoveSpeed(baseSpeedValue.get());

        if (strictSprint.get() && !mc.player.isSprinting()){
            if (mc.getNetworkHandler() != null){
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            }
        }


        switch (mode.get()){
            case Strafe, StrafeStrict, StrafeLow, StrafeGround -> {
                // [Timer] //
                if (resetTimer.get()){
                    timersTick++;
                    if (timersTick >= 5){
                        Modules.get().get(Timer.class).setOverride(Timer.OFF);
                        timersTick = 0;
                    }
                    else if (PlayerUtils.isMoving()){
                        Modules.get().get(Timer.class).setOverride(timer.get());

                        // [slight boost] //
                        ((IVec3d) event.movement).setXZ(event.movement.x * 1.02, event.movement.z * 1.02);
                    }
                }else {
                    Modules.get().get(Timer.class).setOverride(PlayerUtils.isMoving() ? timer.get() : Timer.OFF);
                }


                if (PlayerUtils.isMoving()){
                    if (mc.player.isOnGround()) strafeStage = StrafeStage.Start;

                    // [Check burrow] //
                    if (mode.get() == Mode.Strafe || mode.get() == Mode.StrafeLow){
                        //if (BlockHelper.isSolid(mc.player.getBlockPos())) return;
                    }
                }

                // [On Fall] //
                if (mode.get() == Mode.StrafeStrict){
                    double yDifference = mc.player.getY() - Math.floor(mc.player.getY());

                    if (roundDouble(yDifference, 3) == roundDouble(0.138, 3)){
                        strafeStage = StrafeStage.Fall;

                        // [Falling motion] //
                        ((IVec3d) mc.player.getVelocity()).setY(mc.player.getVelocity().y -0.08);
                        ((IVec3d) event.movement).setY(event.movement.y - 0.09316090325960147);
                        ((IVec3d) mc.player.getPos()).setY(mc.player.getPos().y - 0.09316090325960147);
                    }
                }

                if (strafeStage != StrafeStage.Collision || !PlayerUtils.isMoving()){
                    // [start jumping] //

                    if (strafeStage == StrafeStage.Start){
                        strafeStage = StrafeStage.Jump;
                        double jumpSpeed = 0.3999999463558197;

                        if (strictJump.get()) jumpSpeed = 0.42;

                        if (jumpMode.get() == JumpSpeed.Vanilla){
                            if (mode.get() == Mode.StrafeLow) jumpSpeed = 0.31;
                            else jumpSpeed = 0.42;
                        }
                        else if (mode.get() == Mode.StrafeLow) jumpSpeed = 0.27;

                        if (jumpMode.get() == JumpSpeed.Custom) jumpSpeed = jumpValue.get();

                        if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST))
                            jumpSpeed += (mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1;

                        // [jump] //
                        if (autoJump.get()){
                            ((IVec3d) mc.player.getVelocity()).setY(jumpSpeed);
                            ((IVec3d) event.movement).setY(jumpSpeed);

                            double acceleration = 2.149;

                            if (mode.get() == Mode.Strafe){
                                acceleration = 1.395;
                                if (accelerate) {
                                    acceleration = 1.6835;
                                }
                            }

                            playerSpeed *= acceleration;
                        }
                    }
                    else if (strafeStage == StrafeStage.Jump){
                        strafeStage = StrafeStage.Speed;
                        double scaledMoveSpeed = 3 * (latestMoveSpeed - baseSpeed);
                        playerSpeed = latestMoveSpeed - scaledMoveSpeed;
                        accelerate = !accelerate;
                    }
                    else {
                        if (!mc.world.isSpaceEmpty(mc.player.getBoundingBox().offset(0.0, mc.player.getVelocity().y, 0.0)) || mc.player.verticalCollision){
                            strafeStage = StrafeStage.Collision;

                            if (retain.get()) strafeStage = StrafeStage.Start;
                        }

                        double collisionSpeed = latestMoveSpeed - (latestMoveSpeed / 159);
                        if (strictCollision.get()){
                            collisionSpeed = baseSpeed;
                            latestMoveSpeed = 0;
                        }
                        playerSpeed = collisionSpeed;
                    }
                }
                else {
                    if (mc.player.isOnGround()) strafeStage = StrafeStage.Start;
                    if (BlockHelper.isReplaceable(mc.player.getBlockPos())){
                        playerSpeed = baseSpeed * 1.38;
                    }
                }

                playerSpeed = Math.max(playerSpeed, baseSpeed);

                if (mode.get() == Mode.StrafeStrict){
                    playerSpeed = Math.min(baseSpeed, strictTicks > 25 ? 0.465 : 0.44);
                }

                strictTicks++;

                if (strictTicks > 50) {
                    strictTicks = 0;
                }

                if (frictionMode.get() == FrictionMode.Factor){
                    float friction = 1;
                    if (mc.player.isTouchingWater()) friction = 0.89F;
                    else if (mc.player.isInLava()) friction = 0.535F;
                    playerSpeed *= friction;
                }

                float forward = mc.player.input.movementForward;
                float strafe = mc.player.input.movementSideways;
                float yaw = mc.player.getYaw();

                if (mode.get() == Mode.StrafeStrict){
                    if (!PlayerUtils.isMoving()){
                        ((IVec3d) event.movement).setXZ(0, 0);
                    }
                    else if (forward != 0){
                        if (strafe >= 1) {
                            yaw += (forward > 0 ? -45 : 45);
                            strafe = 0;
                        }

                        else if (strafe <= -1) {
                            yaw += (forward > 0 ? 45 : -45);
                            strafe = 0;
                        }

                        if (forward > 0) {
                            forward = 1;
                        }

                        else if (forward < 0) {
                            forward = -1;
                        }
                    }
                }
                else {
                    if (!PlayerUtils.isMoving()){
                        ((IVec3d) event.movement).setXZ(0, 0);
                    }
                    else if (forward != 0){
                        if (strafe > 0) yaw += forward > 0 ? -45 : 45;
                        else if (strafe < 0) yaw += forward > 0 ? 45 : -45;
                        strafe = 0;
                        if (forward > 0) forward = 1;
                        else if (forward < 0) forward = -1;
                    }
                }

                if (boost.get() && !boostTimer.passedMillis(50))
                    playerSpeed += Math.min(boostSpeed * multiply.get(), max.get());

                double cos = Math.cos(Math.toRadians(yaw + 90));
                double sin = Math.sin(Math.toRadians(yaw + 90));

                double x = (forward * playerSpeed * cos) + (strafe * playerSpeed * sin);
                double z = (forward * playerSpeed * sin) - (strafe * playerSpeed * cos);

                Anchor anchor = Modules.get().get(Anchor.class);
                if (anchor.isActive() && anchor.controlMovement) {
                    x = anchor.deltaX;
                    z = anchor.deltaZ;
                }

                ((IVec3d) event.movement).setXZ(x, z);

                if (!PlayerUtils.isMoving()){
                    ((IVec3d) event.movement).setXZ(0, 0);
                }
            }
            case OnGround -> {
                if (mc.player.isOnGround() && PlayerUtils.isMoving()){
                    if (groundStage == GroundStage.FakeJump){
                        offsetPackets = true;
                        double acceleration = 2.149;
                        playerSpeed -= acceleration;
                        groundStage = GroundStage.Speed;
                    }
                    else if (groundStage == GroundStage.Speed){
                        double scaledMoveSpeed = 0.66 * (latestMoveSpeed - baseSpeed);
                        playerSpeed = latestMoveSpeed - scaledMoveSpeed;
                        groundStage = GroundStage.FakeJump;
                    }

                    if (!mc.world.isSpaceEmpty(mc.player.getBoundingBox().offset(0.0, mc.player.getVelocity().y, 0.0)) || mc.player.verticalCollision){
                        groundStage = GroundStage.FakeJump;
                        double collisionSpeed = latestMoveSpeed - (latestMoveSpeed / 159);
                        if (strictCollision.get()){
                            collisionSpeed = baseSpeed;
                            latestMoveSpeed = 0;
                        }

                        playerSpeed = collisionSpeed;
                    }
                }

                if (airStrafe.get() && !mc.player.isOnGround()){
                    ((IVec3d) mc.player.getVelocity()).setY(mc.player.getVelocity().y / 1.1);
                }

                if (boost.get() && !boostTimer.passedMillis(50))
                    playerSpeed += Math.min(boostSpeed * multiply.get(), max.get());

                double[] dir = PlayerHelper.directionSpeed((float) playerSpeed);
                ((IVec3d) event.movement).setXZ(dir[0], dir[1]);

                Anchor anchor = Modules.get().get(Anchor.class);
                if (anchor.isActive() && anchor.controlMovement) {
                    dir[0] = anchor.deltaX;
                    dir[1] = anchor.deltaZ;
                }

                ((IVec3d) event.movement).setXZ(dir[0], dir[1]);
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
                        setbackTimer.reset();
                        ((PlayerPositionLookS2CPacketAccessor) packet).setX(packet.getX());
                        ((PlayerPositionLookS2CPacketAccessor) packet).setY(packet.getY());
                        ((PlayerPositionLookS2CPacketAccessor) packet).setZ(packet.getZ());
                        reset();
                    }
                }
            }
            ((PlayerPositionLookS2CPacketAccessor) event.packet).setYaw(mc.player.getYaw());
            ((PlayerPositionLookS2CPacketAccessor) event.packet).setPitch(mc.player.getPitch());
            packet.getFlags().remove(PlayerPositionLookS2CPacket.Flag.X_ROT);
            packet.getFlags().remove(PlayerPositionLookS2CPacket.Flag.Y_ROT);
            teleportId = packet.getTeleportId();
        }
        if (event.packet instanceof ExplosionS2CPacket explosionS2CPacket){
            boostSpeed = Math.abs((explosionS2CPacket.getPlayerVelocityX()) + Math.abs(explosionS2CPacket.getPlayerVelocityZ()));
            boostTimer.reset();
        }
        if (event.packet instanceof EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket){
            if (entityVelocityUpdateS2CPacket.getId() != mc.player.getId()) return;

            if (boostSpeed < Math.abs(entityVelocityUpdateS2CPacket.getVelocityX()) + Math.abs(entityVelocityUpdateS2CPacket.getVelocityZ())) {
                boostSpeed = Math.abs(entityVelocityUpdateS2CPacket.getVelocityX()) + Math.abs(entityVelocityUpdateS2CPacket.getVelocityZ());
                boostTimer.reset();
            }
        }
    }

    // [Misc Util] //

    public double roundDouble(double number, int scale) {
        BigDecimal bigDecimal = new BigDecimal(number);
        bigDecimal = bigDecimal.setScale(scale, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    private void reset(){
        strafeStage = StrafeStage.Collision;
        playerSpeed = 0;
        latestMoveSpeed = 0;
        strictTicks = 0;
        accelerate = false;
        teleportId = 0;
    }

    public enum FrictionMode{
        Factor,
        Fast,
        Strict
    }

    public enum JumpSpeed{
        NCP,
        Vanilla,
        Custom
    }

    public enum Mode{
        Strafe, StrafeStrict, StrafeLow,
        StrafeGround, OnGround
    }

    public enum StrafeStage{
        Collision,
        Start,
        Jump,
        Fall,
        Speed
    }

    public enum GroundStage{
        Speed,
        FakeJump,
        CheckSpace
    }
}
