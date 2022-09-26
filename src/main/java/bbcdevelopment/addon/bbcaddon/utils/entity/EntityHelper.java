package bbcdevelopment.addon.bbcaddon.utils.entity;

import bbcdevelopment.addon.bbcaddon.utils.world.BlockHelper;
import bbcdevelopment.addon.bbcaddon.utils.world.BlockPosX;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class EntityHelper {
    public static boolean isInHole(Entity entity){
        boolean isHole = true;
        BlockPosX posX = new BlockPosX(entity.getBlockPos());
        for (Direction direction : Direction.values()){
            if (direction == Direction.UP) continue;
            BlockPosX offset = posX.offset(direction);
            if (!offset.blastRes()) isHole = false;
        }
        return isHole;
    }

    public static boolean isMoving(LivingEntity entity) {
        return entity.forwardSpeed != 0 || entity.sidewaysSpeed != 0;
    }

    public static double getPlayerSpeed(Entity entity) {
        if (mc.player == null) return 0;

        double tX = Math.abs(entity.getX() - entity.prevX);
        double tZ = Math.abs(entity.getZ() - entity.prevZ);
        double length = Math.sqrt(tX * tX + tZ * tZ);

        Timer timer = Modules.get().get(Timer.class);
        if (timer.isActive()) length *= Modules.get().get(Timer.class).getMultiplier();

        return length * 20;
    }

    public static boolean isInBedrockHole(Entity entity, boolean doubleHole){
        BlockPosX pos = new BlockPosX(entity.getBlockPos());
        int air = 0;
        boolean bedrock = true;

        for (Direction dir : Direction.values()){
            if (dir == Direction.UP) continue;

            BlockPosX offsetPos = pos.offset(dir);

            if (!offsetPos.of(Blocks.BEDROCK) && (!offsetPos.air() && doubleHole)) bedrock = false;

            if (offsetPos.air() && doubleHole){
                air++;
                for (Direction anotherDir : Direction.values()){
                    if (anotherDir == Direction.UP || anotherDir == dir.getOpposite()) continue;
                    BlockPosX anotherOffsetPos = offsetPos.offset(anotherDir);
                    if (!anotherOffsetPos.of(Blocks.BEDROCK)) bedrock = false;
                }
            }
        }
        return bedrock && (air == 1 || air == 0);
    }
}
