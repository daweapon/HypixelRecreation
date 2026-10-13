package gg.itzkatze.thehypixelrecreationmod.features.schematic;

import gg.itzkatze.thehypixelrecreationmod.features.KeybindRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class SchematicSelection {
    private static final double CROSSHAIR_RANGE = 128;
    private static final double PREVIEW_RANGE_SQUARED = 64 * 64;
    private static final int PREVIEW_INTERVAL_TICKS = 5;
    private static final int MAX_PREVIEW_PARTICLES = 600;

    private static BlockPos pos1;
    private static BlockPos pos2;
    private static String dimension;
    private static boolean previewEnabled;
    private static int ticks;

    private SchematicSelection() {
    }

    public static BlockPos pos1() {
        return pos1;
    }

    public static BlockPos pos2() {
        return pos2;
    }

    public static String dimension() {
        return dimension;
    }

    public static boolean isComplete() {
        return pos1 != null && pos2 != null;
    }

    public static void setPos1(BlockPos pos) {
        pos1 = pos;
        dimension = currentDimension();
    }

    public static void setPos2(BlockPos pos) {
        pos2 = pos;
        dimension = currentDimension();
    }

    public static void clear() {
        pos1 = null;
        pos2 = null;
        dimension = null;
    }

    public static boolean togglePreview() {
        previewEnabled = !previewEnabled;
        return previewEnabled;
    }

    public static BlockPos defaultTarget(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            throw new IllegalStateException("No world is currently loaded.");
        }

        HitResult hit = player.pick(CROSSHAIR_RANGE, 1.0f, false);
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            return blockHit.getBlockPos();
        }

        return player.blockPosition();
    }

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }

        while (KeybindRegistry.schematicPos1Key != null && KeybindRegistry.schematicPos1Key.consumeClick()) {
            setPos1(defaultTarget(client));
            client.player.sendSystemMessage(Component.literal("§aSchematic pos1 set to " + describe(pos1)));
        }
        while (KeybindRegistry.schematicPos2Key != null && KeybindRegistry.schematicPos2Key.consumeClick()) {
            setPos2(defaultTarget(client));
            client.player.sendSystemMessage(Component.literal("§aSchematic pos2 set to " + describe(pos2)));
        }

        if (!previewEnabled || !isComplete() || !currentDimension().equals(dimension)) {
            return;
        }
        if (++ticks % PREVIEW_INTERVAL_TICKS != 0) {
            return;
        }
        spawnPreview(client.level, client.player);
    }

    public static String describe(BlockPos pos) {
        return pos == null ? "not set" : pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    private static String currentDimension() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : level.dimension().identifier().toString();
    }

    private static void spawnPreview(ClientLevel level, LocalPlayer player) {
        double minX = Math.min(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxX = Math.max(pos1.getX(), pos2.getX()) + 1;
        double maxY = Math.max(pos1.getY(), pos2.getY()) + 1;
        double maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1;

        double[] xs = {minX, maxX};
        double[] ys = {minY, maxY};
        double[] zs = {minZ, maxZ};
        int[] budget = {MAX_PREVIEW_PARTICLES};
        for (double y : ys) {
            for (double z : zs) {
                edge(level, player, minX, y, z, maxX, y, z, budget);
            }
        }
        for (double x : xs) {
            for (double z : zs) {
                edge(level, player, x, minY, z, x, maxY, z, budget);
            }
        }
        for (double x : xs) {
            for (double y : ys) {
                edge(level, player, x, y, minZ, x, y, maxZ, budget);
            }
        }
    }

    private static void edge(
        ClientLevel level,
        LocalPlayer player,
        double x1, double y1, double z1,
        double x2, double y2, double z2,
        int[] budget
    ) {
        double length = Math.abs(x2 - x1) + Math.abs(y2 - y1) + Math.abs(z2 - z1);
        int steps = (int) Math.max(1, Math.ceil(length));
        for (int step = 0; step <= steps && budget[0] > 0; step++) {
            double t = (double) step / steps;
            double x = x1 + (x2 - x1) * t;
            double y = y1 + (y2 - y1) * t;
            double z = z1 + (z2 - z1) * t;
            if (player.distanceToSqr(x, y, z) > PREVIEW_RANGE_SQUARED) {
                continue;
            }
            level.addAlwaysVisibleParticle(ParticleTypes.END_ROD, x, y, z, 0, 0, 0);
            budget[0]--;
        }
    }
}
