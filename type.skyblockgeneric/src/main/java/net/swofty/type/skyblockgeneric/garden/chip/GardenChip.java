package net.swofty.type.skyblockgeneric.garden.chip;

import lombok.Getter;
import net.minestom.server.item.Material;

@Getter
public enum GardenChip {
    VERMIN_VAPORIZER("VERMIN_VAPORIZER_GARDEN_CHIP", "Vermin Vaporizer", Material.PURPLE_CONCRETE, new double[]{3, 4, 5}, "", " Bonus Pest Chance", "Grants <green>Bonus Pest Chance<gray> per chip level.", true),
    SYNTHESIS("SYNTHESIS_GARDEN_CHIP", "Synthesis", Material.YELLOW_CONCRETE, new double[]{1, 1.5, 2}, "%", " Copper from analyzing mutations", "Analyzing a mutation in the Crop Analyzer gives more Copper per chip level.", true),
    SOWLEDGE("SOWLEDGE_GARDEN_CHIP", "Sowledge", Material.GREEN_CONCRETE, new double[]{1, 1.25, 1.5}, "", " Farming Wisdom", "Grants Farming Wisdom per chip level.", false),
    MECHAMIND("MECHAMIND_GARDEN_CHIP", "Mechamind", Material.MAGENTA_CONCRETE, new double[]{1.5, 2, 2.5}, "%", " Farming Tool XP", "Grants more Farming Tool experience per chip level.", true),
    HYPERCHARGE("HYPERCHARGE_GARDEN_CHIP", "Hypercharge", Material.BLUE_CONCRETE, new double[]{3, 4, 5}, "%", " stronger Farming Fortune buffs", "Temporary Farming Fortune buffs are stronger per chip level.", true),
    EVERGREEN("EVERGREEN_GARDEN_CHIP", "Evergreen", Material.LIME_CONCRETE, new double[]{2, 2.5, 3}, "%", " more base crops", "Gain more base crops when harvesting in the Greenhouse per chip level.", true),
    OVERDRIVE("OVERDRIVE_GARDEN_CHIP", "Overdrive", Material.ORANGE_CONCRETE, new double[]{5, 6, 7}, "", " Crop Fortune during Jacob's Contest", "Grants Crop Fortune for the active crop during Jacob's Farming Contest.", false),
    CROPSHOT("CROPSHOT_GARDEN_CHIP", "Cropshot", Material.RED_CONCRETE, new double[]{3, 4, 5}, "", " Farming Fortune", "Grants Farming Fortune per chip level.", true),
    QUICKDRAW("QUICKDRAW_GARDEN_CHIP", "Quickdraw", Material.LIGHT_BLUE_CONCRETE, new double[]{1.5, 2, 2.5}, "%", " faster Visitors", "Decreases the time for Visitors to appear when harvesting crops per chip level.", true),
    RAREFINDER("RAREFINDER_GARDEN_CHIP", "Rarefinder", Material.CYAN_CONCRETE, new double[]{1.5, 2, 2.5}, "", " Overbloom", "Grants Overbloom per chip level.", false);

    public static final int[] CONSUMED_FOR_RARITY = {1, 5, 21};
    public static final int[] MAX_LEVEL = {10, 15, 20};
    public static final String[] RARITY_NAMES = {"RARE", "EPIC", "LEGENDARY"};
    public static final String[] RARITY_COLOURS = {"<9>", "<5>", "<6>"};

    private final String itemId;
    private final String displayName;
    private final Material icon;
    private final double[] perLevel;
    private final String unit;
    private final String suffix;
    private final String description;
    private final boolean active;

    GardenChip(String itemId, String displayName, Material icon, double[] perLevel, String unit, String suffix, String description, boolean active) {
        this.itemId = itemId;
        this.displayName = displayName;
        this.icon = icon;
        this.perLevel = perLevel;
        this.unit = unit;
        this.suffix = suffix;
        this.description = description;
        this.active = active;
    }

    public static GardenChip ofItem(String itemId) {
        if ("TUTORIAL_GARDEN_CHIP".equals(itemId)) return CROPSHOT;
        for (GardenChip chip : values()) if (chip.itemId.equals(itemId)) return chip;
        return null;
    }

    public static int rarityIndex(int consumed) {
        int index = -1;
        for (int i = 0; i < CONSUMED_FOR_RARITY.length; i++) if (consumed >= CONSUMED_FOR_RARITY[i]) index = i;
        return index;
    }
}
