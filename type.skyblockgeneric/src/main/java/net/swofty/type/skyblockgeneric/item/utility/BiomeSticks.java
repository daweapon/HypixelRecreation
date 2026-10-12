package net.swofty.type.skyblockgeneric.item.utility;

import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.biome.Biome;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BiomeSticks {
    public static final int MIN_RADIUS = 1;
    public static final int MAX_RADIUS = 5;
    public static final int DEFAULT_RADIUS = 3;
    private static final int TARGET_RANGE = 5;
    private static final int BIOME_CELL = 4;

    public record Stick(String handlerId, String biomeKey, String biomeName) {
    }

    private static final List<Stick> STICKS = List.of(
            new Stick("JUNGLE_BIOME_STICK", "minecraft:jungle", "Jungle"),
            new Stick("ROOFED_FOREST_BIOME_STICK", "minecraft:dark_forest", "Roofed Forest"),
            new Stick("SAVANA_BIOME_STICK", "minecraft:savanna", "Savanna"));

    private static final Map<UUID, Integer> RADIUS = new ConcurrentHashMap<>();

    private BiomeSticks() {
    }

    public static void registerInteractables() {
        for (Stick stick : STICKS) {
            InteractableRegistry.register(stick.handlerId(), InteractableItemConfig.builder()
                    .rightClickHandler((player, item) -> apply(player, stick))
                    .leftClickHandler((player, item) -> cycleRadius(player))
                    .build());
        }
    }

    public static int nextRadius(int current) {
        return current >= MAX_RADIUS || current < MIN_RADIUS ? MIN_RADIUS : current + 1;
    }

    public static Set<Long> cells(int centerX, int centerZ, int radius) {
        Set<Long> cells = new LinkedHashSet<>();
        for (int x = centerX - radius; x <= centerX + radius; x += 1) {
            for (int z = centerZ - radius; z <= centerZ + radius; z += 1) {
                cells.add(pack(Math.floorDiv(x, BIOME_CELL), Math.floorDiv(z, BIOME_CELL)));
            }
        }
        return cells;
    }

    private static long pack(int cellX, int cellZ) {
        return ((long) cellX << 32) | (cellZ & 0xFFFFFFFFL);
    }

    private static int cellX(long packed) {
        return (int) (packed >> 32);
    }

    private static int cellZ(long packed) {
        return (int) packed;
    }

    private static void cycleRadius(SkyBlockPlayer player) {
        int radius = nextRadius(RADIUS.getOrDefault(player.getUuid(), DEFAULT_RADIUS));
        RADIUS.put(player.getUuid(), radius);
        player.sendMessage("<7>Selected Radius: <b>{}", radius);
    }

    private static void apply(SkyBlockPlayer player, Stick stick) {
        if (!HypixelConst.isIslandServer()) {
            player.sendMessage("<c>You can only change the biome on your Private Island!");
            return;
        }
        Point target = player.getTargetBlockPosition(TARGET_RANGE);
        Instance instance = player.getInstance();
        if (target == null || instance == null) return;

        RegistryKey<Biome> biome = RegistryKey.unsafeOf(stick.biomeKey());
        int radius = RADIUS.getOrDefault(player.getUuid(), DEFAULT_RADIUS);
        int minY = instance.getCachedDimensionType().minY();
        int maxY = minY + instance.getCachedDimensionType().height();
        Set<Chunk> touched = new LinkedHashSet<>();
        for (long cell : cells(target.blockX(), target.blockZ(), radius)) {
            int blockX = cellX(cell) * BIOME_CELL;
            int blockZ = cellZ(cell) * BIOME_CELL;
            Chunk chunk = instance.getChunk(CoordConversion.globalToChunk(blockX), CoordConversion.globalToChunk(blockZ));
            if (chunk == null) continue;
            for (int y = minY; y < maxY; y += BIOME_CELL) chunk.setBiome(blockX, y, blockZ, biome);
            touched.add(chunk);
        }
        touched.forEach(Chunk::sendChunk);
        player.sendMessage("<7>Changed the biome to <b>{}<7>!", stick.biomeName());
    }
}
