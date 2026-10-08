package net.swofty.type.skyblockgeneric.fishing.hotspot;

import java.util.List;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.fishing.FishingMedium;

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
    List<net.swofty.type.skyblockgeneric.fishing.registry.FishingTableDefinition.SeaCreatureRoll> seaCreatures,
    List<SpawnPoint> spawnPoints
) {
    public record SpawnPoint(
        String serverType,
        double x,
        double y,
        double z
    ) {
    }
}
