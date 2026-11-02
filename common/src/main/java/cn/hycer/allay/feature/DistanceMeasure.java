package cn.hycer.allay.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DistanceMeasure {

    private static final double MAX_RAYCAST_DISTANCE = 128.0;

    /**
     * 射线检测模式：
     * 起点为玩家当前脚底坐标，终点为准星指向的方块（最大 128 格）。
     */
    public static int executeRaycast(ServerPlayer player) {
        ServerLevel level = player.level();
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0f);
        Vec3 traceEnd = eyePos.add(lookVec.scale(MAX_RAYCAST_DISTANCE));

        BlockHitResult hit = level.clip(new ClipContext(
                eyePos,
                traceEnd,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        if (hit.getType() != HitResult.Type.BLOCK) {
            player.sendSystemMessage(Component.literal("§c[测距] 视线 128 格范围内未检测到方块"));
            return 0;
        }

        BlockPos targetPos = hit.getBlockPos();
        BlockPos startPos = player.blockPosition();

        sendReport(player, startPos, targetPos, "射线测距 (脚底 -> 准星方块)");
        renderParticles(player, startPos, targetPos);
        return 1;
    }

    /**
     * 两点测距模式：/dist <pos1> <pos2>
     */
    public static int executeTwoPoints(ServerPlayer player, BlockPos p1, BlockPos p2) {
        sendReport(player, p1, p2, "两点测距");
        renderParticles(player, p1, p2);
        return 1;
    }

    /**
     * 输出结构化测距报告
     */
    private static void sendReport(ServerPlayer player, BlockPos p1, BlockPos p2, String title) {
        int dx = Math.abs(p2.getX() - p1.getX());
        int dy = Math.abs(p2.getY() - p1.getY());
        int dz = Math.abs(p2.getZ() - p1.getZ());

        double euclidean = Math.sqrt((double) dx * dx + (double) dy * dy + (double) dz * dz);
        double horizontal = Math.sqrt((double) dx * dx + (double) dz * dz);

        int sizeX = dx + 1;
        int sizeY = dy + 1;
        int sizeZ = dz + 1;
        long totalBlocks = (long) sizeX * sizeY * sizeZ;

        MutableComponent header = Component.literal("§6─── [Allay 测距: " + title + "] ───\n")
                .append(Component.literal(String.format("§7起点: §f(%d, %d, %d) §7-> 终点: §f(%d, %d, %d)\n",
                        p1.getX(), p1.getY(), p1.getZ(),
                        p2.getX(), p2.getY(), p2.getZ())))
                .append(Component.literal(String.format("§e直线距离: §a%.2f 格  §7|  §b水平距离(XZ): §a%.2f 格\n",
                        euclidean, horizontal)))
                .append(Component.literal(String.format("§d轴向跨度: §fΔX: §a%d§7, §fΔY: §a%d§7, §fΔZ: §a%d\n",
                        dx, dy, dz)))
                .append(Component.literal(String.format("§e包围盒尺寸: §f%d × %d × %d = §a%d 格 (含端点)",
                        sizeX, sizeY, sizeZ, totalBlocks)));

        player.sendSystemMessage(header);
    }

    /**
     * 服务端粒子线与端点渲染
     */
    private static void renderParticles(ServerPlayer player, BlockPos p1, BlockPos p2) {
        ServerLevel level = player.level();
        Vec3 start = Vec3.atCenterOf(p1);
        Vec3 end = Vec3.atCenterOf(p2);

        double dist = start.distanceTo(end);
        int steps = Math.min((int) Math.ceil(dist * 2.0), 300); // 步长约 0.5 格，最多 300 个粒子

        if (steps > 0) {
            for (int i = 0; i <= steps; i++) {
                double progress = (double) i / steps;
                double x = start.x + (end.x - start.x) * progress;
                double y = start.y + (end.y - start.y) * progress;
                double z = start.z + (end.z - start.z) * progress;
                level.sendParticles(player, ParticleTypes.END_ROD, true, false,
                        x, y, z, 1, 0, 0, 0, 0.0);
            }
        }

        // 起点与终点处播放标记粒子
        level.sendParticles(player, ParticleTypes.HAPPY_VILLAGER, true, false,
                start.x, start.y + 0.5, start.z, 8, 0.25, 0.25, 0.25, 0.05);
        level.sendParticles(player, ParticleTypes.HAPPY_VILLAGER, true, false,
                end.x, end.y + 0.5, end.z, 8, 0.25, 0.25, 0.25, 0.05);
    }
}
