package dev.daweapon.type.skyblockgeneric.fishing.hotspot;

import java.util.List;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.fishing.FishingMedium;

// Config-driven definition of a fishing hotspot: where it can spawn, which medium/regions it
// applies to, and the statistic buffs and sea creatures it offers while active.
public record HotspotDefinition(
    String id,
    String displayName,
    List<String> regions,
    FishingMedium medium,
    int maxActive,
    int durationSeconds,
    ItemStatistics buffs,
    List<dev.daweapon.type.skyblockgeneric.fishing.registry.FishingTableDefinition.SeaCreatureRoll> seaCreatures,
    List<SpawnPoint> spawnPoints,
    java.util.Map<String, TreasureOverride> treasureOverrides
) {
    public record TreasureOverride(String itemId, int min, int max) {
    }

    public record SpawnPoint(
        String serverType,
        double x,
        double y,
        double z
    ) {
    }
}
