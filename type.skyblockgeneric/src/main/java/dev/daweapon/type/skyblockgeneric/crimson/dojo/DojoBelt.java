package dev.daweapon.type.skyblockgeneric.crimson.dojo;

import lombok.Getter;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

/** Belts earned from Dojo Points: White is Master Tao's gift, the rest are upgrades claimed in Belt Rewards. */
@Getter
public enum DojoBelt {
    WHITE("White", "<f>", 0, ItemType.DOJO_WHITE_BELT, 0, 20, 0.005, Material.WHITE_STAINED_GLASS_PANE),
    YELLOW("Yellow", "<e>", 1_000, ItemType.DOJO_YELLOW_BELT, 80_000, 30, 0.01, Material.YELLOW_STAINED_GLASS_PANE),
    GREEN("Green", "<a>", 2_000, ItemType.DOJO_GREEN_BELT, 100_000, 50, 0.03, Material.LIME_STAINED_GLASS_PANE),
    BLUE("Blue", "<9>", 4_000, ItemType.DOJO_BLUE_BELT, 120_000, 75, 0.04, Material.BLUE_STAINED_GLASS_PANE),
    BROWN("Brown", "<6>", 6_000, ItemType.DOJO_BROWN_BELT, 140_000, 100, 0.05, Material.BROWN_STAINED_GLASS_PANE),
    BLACK("Black", "<8>", 7_000, ItemType.DOJO_BLACK_BELT, 160_000, 150, 0.06, Material.BLACK_STAINED_GLASS_PANE),
    ;

    private final String displayName;
    private final String colour;
    private final int requiredPoints;
    private final ItemType itemType;
    private final int combatXP;
    private final int skyBlockXP;
    private final double damageReduction;
    private final Material pane;

    DojoBelt(String displayName, String colour, int requiredPoints, ItemType itemType, int combatXP,
             int skyBlockXP, double damageReduction, Material pane) {
        this.displayName = displayName;
        this.colour = colour;
        this.requiredPoints = requiredPoints;
        this.itemType = itemType;
        this.combatXP = combatXP;
        this.skyBlockXP = skyBlockXP;
        this.damageReduction = damageReduction;
        this.pane = pane;
    }

    public String coloured() {
        return colour + displayName;
    }

    public @Nullable DojoBelt previous() {
        return ordinal() == 0 ? null : values()[ordinal() - 1];
    }

    public static DojoBelt reached(long points) {
        DojoBelt result = WHITE;
        for (DojoBelt belt : values()) {
            if (points >= belt.requiredPoints) result = belt;
        }
        return result;
    }

    public static @Nullable DojoBelt of(ItemType type) {
        for (DojoBelt belt : values()) {
            if (belt.itemType == type) return belt;
        }
        return null;
    }

    public static int cumulativeSkyBlockXP(@Nullable DojoBelt highest) {
        if (highest == null) return 0;
        int total = 0;
        for (DojoBelt belt : values()) {
            if (belt.ordinal() <= highest.ordinal()) total += belt.skyBlockXP;
        }
        return total;
    }
}
