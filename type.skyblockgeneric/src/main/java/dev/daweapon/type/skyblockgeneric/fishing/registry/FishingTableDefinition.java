package dev.daweapon.type.skyblockgeneric.fishing.registry;

import java.util.List;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.fishing.FishingMedium;
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
    @Nullable EssenceTreasure essenceTreasure,
    List<WeightedLoot> weightedItems,
    List<TreasureQuality> treasureQualities
) {
    public static final String COINS = "COINS";

    public boolean hasQualityTreasure() {
        return !weightedItems.isEmpty() && !treasureQualities.isEmpty();
    }

    /** A weighted entry rolled for an amount between min and max; {@link #COINS} pays coins instead of an item. */
    public record WeightedLoot(String itemId, double weight, int min, int max) {
    }

    /** GOOD / GREAT / OUTSTANDING treasure: its share of treasure catches and its weighted pool. */
    public record TreasureQuality(String name, double share, List<WeightedLoot> entries) {
    }

    public record LootEntry(String itemId, double chance, int amount, double skillXp) {
    }

    public record SeaCreatureRoll(String seaCreatureId, double chance, double weight) {
    }

    public record EssenceTreasure(EssenceType type, double chance, int min, int max, double skillXp) {
    }
}
