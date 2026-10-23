package dev.daweapon.type.skyblockgeneric.entity.mob.island;

import net.minestom.server.instance.Instance;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/** Which monsters may naturally spawn in a biome, from the wiki's Biome Stick table (non-Stranded rows). */
public final class IslandBiomeRules {
    private static final Map<String, Set<IslandMobSpecies>> ONLY = Map.of(
            "minecraft:the_end", Set.of(IslandMobSpecies.ENDERMAN),
            "minecraft:nether_wastes", Set.of(IslandMobSpecies.SLIME, IslandMobSpecies.CHICKEN));

    private IslandBiomeRules() {
    }

    public static @Nullable Set<IslandMobSpecies> onlySpecies(String biomeKey) {
        return ONLY.get(biomeKey);
    }

    public static @Nullable Set<IslandMobSpecies> onlySpeciesAt(Instance instance, Point position) {
        var biome = instance.getBiome(position);
        return biome == null ? null : onlySpecies(biome.name());
    }
}
