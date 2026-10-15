package dev.daweapon.type.skyblockgeneric.forge;

import net.minestom.server.item.Material;

public enum ForgeCategory {
    REFINING("Refining", Material.FURNACE),
    FORGING("Forging", Material.ANVIL),
    TOOLS("Tools", Material.DIAMOND_PICKAXE),
    GEAR("Gear", Material.DIAMOND_CHESTPLATE),
    REFORGE_STONES("Reforge Stones", Material.ANVIL),
    DRILL_PARTS("Drill Parts", Material.REDSTONE_BLOCK),
    PERFECT_GEMSTONES("Perfect Gemstones", Material.EMERALD),
    PETS("Pets", Material.BONE),
    OTHER("Other", Material.CHEST);

    private final String displayName;
    private final Material icon;

    ForgeCategory(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String displayName() {
        return displayName;
    }

    public Material icon() {
        return icon;
    }
}
