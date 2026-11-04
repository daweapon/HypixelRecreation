package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;

import java.util.Arrays;
import java.util.List;

/**
 * One row of a wiki sea creature drop table. A wiki range ("2-4") is stored as its rounded midpoint, since a loot
 * record holds a single amount. Rows whose item isn't in the catalogue are skipped.
 */
public record SeaCreatureDrop(String item, int amount, double chancePercent) {
    public static SeaCreatureDrop always(String item, int amount) {
        return new SeaCreatureDrop(item, amount, 100);
    }

    public static SeaCreatureDrop always(String item, int min, int max) {
        return always(item, midpoint(min, max));
    }

    public static SeaCreatureDrop chance(String item, int amount, double chancePercent) {
        return new SeaCreatureDrop(item, amount, chancePercent);
    }

    public static SeaCreatureDrop oneIn(String item, double odds) {
        return new SeaCreatureDrop(item, 1, 100D / odds);
    }

    public static SkyBlockLootTable table(SeaCreatureDrop... drops) {
        return new SkyBlockLootTable() {
            @Override
            public List<LootRecord> getLootTable() {
                return Arrays.stream(drops)
                        .filter(drop -> drop.type() != null)
                        .map(drop -> new LootRecord(drop.type(), drop.amount(), drop.chancePercent()))
                        .toList();
            }

            @Override
            public CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
    }

    private static int midpoint(int min, int max) {
        return (int) Math.round((min + max) / 2D);
    }

    public ItemType type() {
        return ItemType.get(item);
    }
}
