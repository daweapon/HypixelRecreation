package net.swofty.type.skyblockgeneric.fishing.registry;

import java.util.List;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.fishing.FishingMedium;
import org.jetbrains.annotations.Nullable;

// Config-driven fishing loot table for a set of regions/mediums: its regular, treasure, and
// junk item drops plus possible sea creature rolls. A weighted table rolls Sea Creature Chance once
// and then picks one eligible creature by weight, as Hypixel's island pools do.
public record FishingTableDefinition(
    String id,
    List<String> regions,
    List<String> serverTypes,
    List<FishingMedium> mediums,
    List<LootEntry> items,
    List<LootEntry> treasures,
    List<LootEntry> junk,
    List<SeaCreatureRoll> seaCreatures,
    boolean weightedSeaCreatures,
    @Nullable EssenceTreasure essenceTreasure
) {
    public record LootEntry(String itemId, double chance, int amount, double skillXp) {
    }

    public record SeaCreatureRoll(String seaCreatureId, double chance, double weight) {
    }

    public record EssenceTreasure(EssenceType type, double chance, int min, int max, double skillXp) {
    }
}
