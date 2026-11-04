package dev.daweapon.type.skyblockgeneric.dante;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.world.biome.Biome;

import java.util.HashMap;
import java.util.Map;

public final class DanteHubBiome {
    public static final int MIN_Y = 40;
    public static final int MAX_Y = 160;
    private static final int CELL = 4;
    private static final int CHECK_SECONDS = 10;
    private static final Map<Chunk, RegistryKey<Biome>[]> ORIGINAL = new HashMap<>();
    private static boolean applied;

    private DanteHubBiome() {
    }

    public static void initialize() {
        MinecraftServer.getSchedulerManager().buildTask(DanteHubBiome::sync)
                .repeat(TaskSchedule.seconds(CHECK_SECONDS)).schedule();
    }

    private static void sync() {
        if (HypixelConst.getTypeLoader().getType() != ServerType.SKYBLOCK_HUB) return;
        boolean wanted = DanteAccess.mayorIsDante();
        if (wanted == applied) return;
        Instance hub = HypixelConst.getInstanceContainer();
        if (hub == null) return;
        if (wanted) drain(hub);
        else restore();
        applied = wanted;
    }

    @SuppressWarnings("unchecked")
    private static void drain(Instance hub) {
        for (Chunk chunk : hub.getChunks()) {
            int baseX = chunk.getChunkX() << 4;
            int baseZ = chunk.getChunkZ() << 4;
            RegistryKey<Biome>[] saved = new RegistryKey[cellsPerChunk()];
            int index = 0;
            for (int x = 0; x < 16; x += CELL) {
                for (int z = 0; z < 16; z += CELL) {
                    for (int y = MIN_Y; y < MAX_Y; y += CELL) {
                        saved[index++] = chunk.getBiome(baseX + x, y, baseZ + z);
                        chunk.setBiome(baseX + x, y, baseZ + z, Biome.DESERT);
                    }
                }
            }
            ORIGINAL.put(chunk, saved);
            chunk.sendChunk();
        }
    }

    private static void restore() {
        for (Map.Entry<Chunk, RegistryKey<Biome>[]> entry : ORIGINAL.entrySet()) {
            Chunk chunk = entry.getKey();
            if (!chunk.isLoaded()) continue;
            RegistryKey<Biome>[] saved = entry.getValue();
            int baseX = chunk.getChunkX() << 4;
            int baseZ = chunk.getChunkZ() << 4;
            int index = 0;
            for (int x = 0; x < 16; x += CELL) {
                for (int z = 0; z < 16; z += CELL) {
                    for (int y = MIN_Y; y < MAX_Y; y += CELL) {
                        chunk.setBiome(baseX + x, y, baseZ + z, saved[index++]);
                    }
                }
            }
            chunk.sendChunk();
        }
        ORIGINAL.clear();
    }

    private static int cellsPerChunk() {
        return (16 / CELL) * (16 / CELL) * ((MAX_Y - MIN_Y) / CELL);
    }
}
