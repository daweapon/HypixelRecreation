package dev.daweapon.type.skyblockgeneric.museum;

import lombok.Getter;
import net.minestom.server.item.Material;

@Getter
public enum MuseumCategory {
    COMBAT("Combat", "<c>", Material.STONE_SWORD, 20),
    FARMING("Farming", "<a>", Material.GOLDEN_HOE, 21),
    MINING("Mining", "<6>", Material.STONE_PICKAXE, 22),
    FISHING("Fishing", "<b>", Material.FISHING_ROD, 23),
    FORAGING("Foraging", "<2>", Material.JUNGLE_SAPLING, 24),
    DUNGEONEERING("Dungeoneering", "<4>", Material.WITHER_SKELETON_SKULL, 30),
    HUNTING("Hunting", "<e>", Material.LEAD, 31),
    SPECIAL("Special Items", "<d>", Material.CAKE, 32);

    public static final int SPECIAL_ITEM_LIMIT = 48;

    private final String displayName;
    private final String color;
    private final Material icon;
    private final int menuSlot;

    MuseumCategory(String displayName, String color, Material icon, int menuSlot) {
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
        this.menuSlot = menuSlot;
    }

    public boolean isSpecial() {
        return this == SPECIAL;
    }

    public String coloredName() {
        return color + displayName;
    }
}
