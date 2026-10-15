package dev.daweapon.type.skyblockgeneric.gui.inventories.enchantment;

import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.List;

// The categories the Enchantments Guide can be narrowed to.
enum EnchantmentGuideFilter {
    ALL("All Enchantments", Material.ENCHANTED_BOOK, List.of()),
    ULTIMATE("Ultimate", Material.NETHER_STAR, List.of()),
    MELEE("Melee Weapons", Material.DIAMOND_SWORD, List.of(EnchantItemGroups.SWORD, EnchantItemGroups.LONG_SWORD,
            EnchantItemGroups.FISHING_WEAPON, EnchantItemGroups.GAUNTLET)),
    BOW("Bows", Material.BOW, List.of(EnchantItemGroups.BOW)),
    ARMOR("Armor", Material.DIAMOND_CHESTPLATE, List.of(EnchantItemGroups.ARMOR, EnchantItemGroups.HELMET,
            EnchantItemGroups.CHESTPLATE, EnchantItemGroups.LEGGINGS, EnchantItemGroups.BOOTS)),
    EQUIPMENT("Equipment", Material.LEATHER_HORSE_ARMOR, List.of(EnchantItemGroups.EQUIPMENT, EnchantItemGroups.NECKLACE,
            EnchantItemGroups.CLOAK, EnchantItemGroups.BELT, EnchantItemGroups.GLOVES, EnchantItemGroups.BRACELET)),
    MINING("Mining", Material.DIAMOND_PICKAXE, List.of(EnchantItemGroups.PICKAXE, EnchantItemGroups.DRILL)),
    FARMING("Farming", Material.GOLDEN_HOE, List.of(EnchantItemGroups.HOE)),
    FORAGING("Foraging", Material.IRON_AXE, List.of(EnchantItemGroups.AXE)),
    FISHING("Fishing", Material.FISHING_ROD, List.of(EnchantItemGroups.FISHING_ROD));

    final String displayName;
    final Material icon;
    private final List<EnchantItemGroups> groups;

    EnchantmentGuideFilter(String displayName, Material icon, List<EnchantItemGroups> groups) {
        this.displayName = displayName;
        this.icon = icon;
        this.groups = groups;
    }

    boolean matches(EnchantmentType type) {
        return switch (this) {
            case ALL -> true;
            case ULTIMATE -> type.getEnch().isUltimate();
            default -> type.getEnch().getGroups().stream().anyMatch(groups::contains);
        };
    }

    EnchantmentGuideFilter next(int step) {
        EnchantmentGuideFilter[] all = values();
        return all[Math.floorMod(ordinal() + step, all.length)];
    }
}
