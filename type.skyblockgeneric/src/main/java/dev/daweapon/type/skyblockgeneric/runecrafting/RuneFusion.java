package dev.daweapon.type.skyblockgeneric.runecrafting;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;

import java.util.UUID;

public final class RuneFusion {
    public static final int MAX_LEVEL = 3;

    private RuneFusion() {
    }

    public static double chancePercent(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 25;
            case UNCOMMON -> 35;
            case RARE -> 40;
            case EPIC -> 45;
            default -> 50;
        };
    }

    public static double xp(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 10;
            case UNCOMMON -> 15;
            case RARE -> 25;
            case EPIC -> 30;
            default -> 35;
        };
    }

    public static double failureXp(Rarity rarity) {
        return Math.floor(xp(rarity) / 2);
    }

    public static boolean canFuse(int leftLevel, int rightLevel) {
        return leftLevel == rightLevel && leftLevel < MAX_LEVEL;
    }

    public static boolean attempt(UUID player, ItemType rune, Rarity rarity) {
        return LootRoll.roll(player, rune, chancePercent(rarity));
    }
}
