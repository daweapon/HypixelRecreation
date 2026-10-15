package dev.daweapon.type.crystalhollows.temple;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.crystalhollows.layout.CrystalHollowsLayout;
import dev.daweapon.type.generic.HypixelConst;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class TempleDoor {
    public static final int MIN_X = 32;
    public static final int MAX_X = 36;
    public static final int MIN_Y = 46;
    public static final int MAX_Y = 53;
    public static final int MIN_Z = 40;
    public static final int MAX_Z = 41;
    public static final int CLEARANCE = 2;
    public static final Block MATERIAL = Block.POLISHED_ANDESITE;
    private static final int ROW_TICKS = 3;
    private static final int RESEAL_CHECK_TICKS = 20;

    private static final Set<UUID> HOLDERS = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger GENERATION = new AtomicInteger();
    private static volatile boolean open;

    private TempleDoor() {
    }

    public static List<Vec> offsets() {
        List<Vec> offsets = new ArrayList<>();
        for (int y = MIN_Y; y <= MAX_Y; y++) offsets.addAll(row(y));
        return offsets;
    }

    public static List<Vec> row(int y) {
        List<Vec> row = new ArrayList<>();
        for (int x = MIN_X; x <= MAX_X; x++) {
            for (int z = MIN_Z; z <= MAX_Z; z++) row.add(new Vec(x, y, z));
        }
        return row;
    }

    public static boolean occupied(Collection<? extends Point> positions, Point origin) {
        for (Point position : positions) {
            double x = position.x() - origin.x();
            double y = position.y() - origin.y();
            double z = position.z() - origin.z();
            if (x >= MIN_X - CLEARANCE && x < MAX_X + 1 + CLEARANCE
                    && y >= MIN_Y - 1 && y < MAX_Y + 1 + CLEARANCE
                    && z >= MIN_Z - CLEARANCE && z < MAX_Z + 1 + CLEARANCE) return true;
        }
        return false;
    }

    public static boolean isOpen() {
        return open;
    }

    public static void keyAccepted(UUID player) {
        HOLDERS.add(player);
        open(HypixelConst.getInstanceContainer());
    }

    public static void released(UUID player) {
        if (HOLDERS.remove(player) && HOLDERS.isEmpty()) scheduleReseal(HypixelConst.getInstanceContainer());
    }

    public static void reset(Instance instance) {
        GENERATION.incrementAndGet();
        HOLDERS.clear();
        open = false;
        Pos origin = CrystalHollowsLayout.origin(JungleTemple.STRUCTURE);
        if (instance == null || origin == null) return;
        loaded(instance, origin).thenRun(() -> offsets().forEach(offset -> instance.setBlock(origin.add(offset), MATERIAL)));
    }

    public static void open(Instance instance) {
        Pos origin = CrystalHollowsLayout.origin(JungleTemple.STRUCTURE);
        if (instance == null || origin == null || open) return;
        open = true;
        int generation = GENERATION.incrementAndGet();
        loaded(instance, origin).thenRun(() -> animate(instance, origin, generation, true));
    }

    private static void scheduleReseal(Instance instance) {
        Pos origin = CrystalHollowsLayout.origin(JungleTemple.STRUCTURE);
        if (instance == null || origin == null || !open) return;
        int generation = GENERATION.incrementAndGet();
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (generation != GENERATION.get() || !HOLDERS.isEmpty()) return TaskSchedule.stop();
            if (occupied(instance.getPlayers().stream().map(Player::getPosition).toList(), origin)) {
                return TaskSchedule.tick(RESEAL_CHECK_TICKS);
            }
            open = false;
            animate(instance, origin, generation, false);
            return TaskSchedule.stop();
        });
    }

    private static void animate(Instance instance, Pos origin, int generation, boolean opening) {
        int[] next = {opening ? MAX_Y : MIN_Y};
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (generation != GENERATION.get()) return TaskSchedule.stop();
            int y = next[0];
            Block block = opening ? Block.AIR : MATERIAL;
            for (Vec offset : row(y)) instance.setBlock(origin.add(offset), block);
            Point middle = origin.add((MIN_X + MAX_X + 1) / 2D, y, (MIN_Z + MAX_Z + 1) / 2D);
            instance.playSound(Sound.sound(SoundEvent.BLOCK_STONE_BREAK, Sound.Source.BLOCK, 1f, opening ? 0.7f : 0.9f),
                    middle.x(), middle.y(), middle.z());
            next[0] += opening ? -1 : 1;
            boolean finished = opening ? next[0] < MIN_Y : next[0] > MAX_Y;
            return finished ? TaskSchedule.stop() : TaskSchedule.tick(ROW_TICKS);
        });
    }

    private static CompletableFuture<Void> loaded(Instance instance, Pos origin) {
        List<CompletableFuture<?>> loads = new ArrayList<>();
        for (int cx = (origin.blockX() + MIN_X) >> 4; cx <= (origin.blockX() + MAX_X) >> 4; cx++) {
            for (int cz = (origin.blockZ() + MIN_Z) >> 4; cz <= (origin.blockZ() + MAX_Z) >> 4; cz++) {
                loads.add(instance.loadChunk(cx, cz));
            }
        }
        return CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new));
    }
}
