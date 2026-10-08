package net.swofty.type.skyblockgeneric.item.handlers.pet.definition;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A pet's own stats, one block per rarity. A rarity with no block of its own uses the nearest
 * lower rarity's block, then the nearest higher.
 */
public final class PetStatTable {
    public static final PetStatTable EMPTY = new PetStatTable(new EnumMap<>(Rarity.class));

    private final EnumMap<Rarity, Map<ItemStatistic, PetValue>> blocks;

    public PetStatTable(EnumMap<Rarity, Map<ItemStatistic, PetValue>> blocks) {
        this.blocks = blocks;
    }

    public Map<ItemStatistic, PetValue> block(Rarity rarity) {
        Map<ItemStatistic, PetValue> exact = blocks.get(rarity);
        if (exact != null) return exact;
        for (int i = rarity.ordinal() - 1; i >= 0; i--) {
            Map<ItemStatistic, PetValue> lower = blocks.get(Rarity.values()[i]);
            if (lower != null) return lower;
        }
        for (int i = rarity.ordinal() + 1; i < Rarity.values().length; i++) {
            Map<ItemStatistic, PetValue> higher = blocks.get(Rarity.values()[i]);
            if (higher != null) return higher;
        }
        return Collections.emptyMap();
    }

    public ItemStatistics at(Rarity rarity, int level, int baseLevel) {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        block(rarity).forEach((statistic, value) -> {
            double amount = value.at(rarity, level, baseLevel);
            if (amount != 0) builder.withBase(statistic, amount);
        });
        return builder.build();
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }

    public static PetStatTable parse(Object raw, String where) {
        if (raw == null) return EMPTY;
        if (!(raw instanceof Map<?, ?> map)) throw new IllegalArgumentException(where + ": expected a rarity map");
        EnumMap<Rarity, Map<ItemStatistic, PetValue>> blocks = new EnumMap<>(Rarity.class);
        map.forEach((rarityKey, statsRaw) -> {
            Rarity rarity = Rarity.getRarity(String.valueOf(rarityKey));
            if (rarity == null) throw new IllegalArgumentException(where + ": unknown rarity '" + rarityKey + "'");
            Map<ItemStatistic, PetValue> block = new LinkedHashMap<>();
            if (statsRaw instanceof Map<?, ?> stats) {
                stats.forEach((statKey, valueRaw) -> {
                    ItemStatistic statistic = ItemStatistic.valueOf(String.valueOf(statKey).toUpperCase());
                    block.put(statistic, PetValue.parse(valueRaw, where + "." + rarityKey + "." + statKey));
                });
            }
            blocks.put(rarity, block);
        });
        return new PetStatTable(blocks);
    }
}
