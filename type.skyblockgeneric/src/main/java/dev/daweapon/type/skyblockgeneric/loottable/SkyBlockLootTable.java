package dev.daweapon.type.skyblockgeneric.loottable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;
import java.util.*;
import java.util.function.Function;

public abstract class SkyBlockLootTable {
    public abstract @NonNull List<LootRecord> getLootTable();
    public abstract @NonNull CalculationMode getCalculationMode();

    public int makeAmountBetween(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }

    public @NonNull List<ItemType> getLootTableItems() {
        List<ItemType> items = new ArrayList<>();
        for (LootRecord record : getLootTable()) {
            items.add(record.itemType);
        }
        return items;
    }

    /**
     * Function will always return a LootRecord for every itemtype
     * @param player The player to run the chances for
     * @return A map of the loot that the player will receive
     */
    public @NonNull Map<ItemType, LootRecord> runChances(SkyBlockPlayer player, LootAffector... affectors) {
        Map<ItemType, LootRecord> loot = new HashMap<>();
        CalculationMode mode = getCalculationMode();

        if (mode == CalculationMode.PICK_ONE) {
            List<LootRecord> eligible = getLootTable().stream()
                    .filter(record -> record.shouldCalculate.apply(player))
                    .toList();
            if (!eligible.isEmpty()) {
                LootRecord picked = LootRoll.pick(player.getUuid(), eligible, LootRecord::getItemType,
                        LootRecord::getChancePercent, ThreadLocalRandom.current());
                loot.put(picked.itemType, picked);
            }
        } else if (mode == CalculationMode.CALCULATE_INDIVIDUAL) {
            for (LootRecord record : getLootTable()) {
                double adjustedChancePercent = 0;
                if (record.shouldCalculate.apply(player)) {
                    adjustedChancePercent = record.chancePercent;
                    for (LootAffector affector : affectors) {
                        adjustedChancePercent = affector.getAffector().apply(player, adjustedChancePercent, null);
                    }
                }
                if (LootRoll.roll(player.getUuid(), record.itemType, adjustedChancePercent)) {
                    loot.put(record.itemType, record);
                }
            }
        }

        return loot;
    }

    @Getter
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class LootRecord {
        private final ItemType itemType;
        private final int amount;
        private final double chancePercent;
        private Function<SkyBlockPlayer, Boolean> shouldCalculate = player -> true;

        public static LootRecord none(int chance) {
            return new LootRecord(ItemType.AIR, 0, chance, player -> true);
        }

        public static LootRecord none(int chance, Function<SkyBlockPlayer, Boolean> shouldCalculate) {
            return new LootRecord(ItemType.AIR, 0, chance, shouldCalculate);
        }

        public static boolean isNone(LootRecord lootRecord) {
            return lootRecord.itemType == ItemType.AIR && lootRecord.amount == 0;
        }
    }

    public enum CalculationMode {
        // Adds up all the chances and picks one based on that
        PICK_ONE,
        // Calculates each item individually, and gives the player each item that they have a chance to get
        CALCULATE_INDIVIDUAL
    }
}
