package net.swofty.type.skyblockgeneric.fishing.registry;

import java.util.List;
import net.swofty.type.skyblockgeneric.fishing.FishingMedium;

// Config-driven fishing loot table for a set of regions/mediums: its regular, treasure, and
// junk item drops plus possible sea creature rolls.
public record FishingTableDefinition(
    String id,
    List<String> regions,
    List<FishingMedium> mediums,
    List<LootEntry> items,
    List<LootEntry> treasures,
    List<LootEntry> junk,
    List<SeaCreatureRoll> seaCreatures
) {
    public record LootEntry(String itemId, double chance, int amount, double skillXp) {
    }

    public record SeaCreatureRoll(String seaCreatureId, double chance) {
    }
}
