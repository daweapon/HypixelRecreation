package net.swofty.type.skyblockgeneric.item.set.sets.terror;

import net.swofty.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Hydra Strike numbers per Terror tier (wiki, Terror Armor, after the 0.20.9 revamp). */
public enum TerrorTier {
    BASIC(new double[]{2, 4, 6}, 1, 20,
            List.of(ItemType.TERROR_HELMET, ItemType.TERROR_CHESTPLATE, ItemType.TERROR_LEGGINGS, ItemType.TERROR_BOOTS)),
    HOT(new double[]{2.75, 5.5, 8.25}, 1.5, 30,
            List.of(ItemType.HOT_TERROR_HELMET, ItemType.HOT_TERROR_CHESTPLATE, ItemType.HOT_TERROR_LEGGINGS, ItemType.HOT_TERROR_BOOTS)),
    BURNING(new double[]{3.5, 7, 10.5}, 2, 40,
            List.of(ItemType.BURNING_TERROR_HELMET, ItemType.BURNING_TERROR_CHESTPLATE, ItemType.BURNING_TERROR_LEGGINGS, ItemType.BURNING_TERROR_BOOTS)),
    FIERY(new double[]{4.25, 8.5, 12.75}, 2.5, 50,
            List.of(ItemType.FIERY_TERROR_HELMET, ItemType.FIERY_TERROR_CHESTPLATE, ItemType.FIERY_TERROR_LEGGINGS, ItemType.FIERY_TERROR_BOOTS)),
    INFERNAL(new double[]{5, 10, 15}, 3, 60,
            List.of(ItemType.INFERNAL_TERROR_HELMET, ItemType.INFERNAL_TERROR_CHESTPLATE, ItemType.INFERNAL_TERROR_LEGGINGS, ItemType.INFERNAL_TERROR_BOOTS));

    public static final int MINIMUM_PIECES = 2;
    private static final long[] DECAY_MILLIS = {4000, 7000, 10000};
    private static final TerrorTier[] VALUES = values();

    private final double[] damagePerStack;
    private final double arrowSpeedPerStack;
    private final double extraArrowDamage;
    private final List<ItemType> pieces;

    TerrorTier(double[] damagePerStack, double arrowSpeedPerStack, double extraArrowDamage, List<ItemType> pieces) {
        this.damagePerStack = damagePerStack;
        this.arrowSpeedPerStack = arrowSpeedPerStack;
        this.extraArrowDamage = extraArrowDamage;
        this.pieces = pieces;
    }

    public static @Nullable TerrorTier of(@Nullable ItemType type) {
        if (type == null) return null;
        for (TerrorTier tier : VALUES) {
            if (tier.pieces.contains(type)) return tier;
        }
        return null;
    }

    private static int index(int wornPieces) {
        return Math.max(0, Math.min(wornPieces, 4) - MINIMUM_PIECES);
    }

    public double damagePerStack(int wornPieces) {
        return damagePerStack[index(wornPieces)];
    }

    public double arrowSpeedPerStack() {
        return arrowSpeedPerStack;
    }

    public double extraArrowDamage() {
        return extraArrowDamage;
    }

    public static long decayMillis(int wornPieces) {
        return DECAY_MILLIS[index(wornPieces)];
    }

    public List<ItemType> pieces() {
        return pieces;
    }
}
