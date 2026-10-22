package dev.daweapon.type.skyblockgeneric.user.island;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.biome.Biome;
import dev.daweapon.type.skyblockgeneric.item.utility.BiomeSticks;

import java.util.ArrayList;
import java.util.List;

public final class IslandBiomes {
    public static final int MAX_CHUNK_RADIUS = 9;
    public static final String PLAINS_KEY = "minecraft:plains";

    public record Choice(String name, String key) {
    }

    private IslandBiomes() {
    }

    public static List<Choice> choices() {
        List<Choice> choices = new ArrayList<>();
        choices.add(new Choice("Plains", PLAINS_KEY));
        BiomeSticks.sticks().forEach(stick -> choices.add(new Choice(stick.biomeName(), stick.biomeKey())));
        return choices;
    }

    public static int chunkRadius(double borderDiameter) {
        return Math.min(MAX_CHUNK_RADIUS, (int) Math.ceil(borderDiameter / 2 / 16) + 1);
    }

    public static void apply(Instance instance, String biomeKey) {
        RegistryKey<Biome> biome = RegistryKey.unsafeOf(biomeKey);
        int radius = chunkRadius(instance.getWorldBorder().diameter());
        for (int chunkX = -radius; chunkX <= radius; chunkX++) {
            for (int chunkZ = -radius; chunkZ <= radius; chunkZ++) {
                instance.loadChunk(chunkX, chunkZ).thenAccept(chunk -> {
                    if (chunk != null) MinecraftServer.getSchedulerManager().scheduleNextTick(() -> BiomeSticks.setBiome(chunk, biome));
                });
            }
        }
    }
}
