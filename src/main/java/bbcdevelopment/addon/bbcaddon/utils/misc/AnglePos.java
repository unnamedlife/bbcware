package bbcdevelopment.addon.bbcaddon.utils.misc;

import net.minecraft.util.math.Vec3d;

/**
 * @author GhostTypes
 */

public class AnglePos {
    private final Vec3d vec;
    private final float yaw, pitch;

    public AnglePos(Vec3d vec, float yaw, float pitch) {
        this.vec = vec;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Vec3d getPos() {
        return vec;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
