package dev.daweapon.type.skyblockgeneric.enchantment;

import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

// Which Ultimate enchantment belongs on an item when it is maxed: a known item first, then its item type.
public final class UltimateEnchantChoice {
    private static final Map<String, EnchantmentType> BY_ITEM = Map.ofEntries(
            Map.entry("HYPERION", EnchantmentType.WISE),
            Map.entry("SCYLLA", EnchantmentType.WISE),
            Map.entry("ASTRAEA", EnchantmentType.WISE),
            Map.entry("VALKYRIE", EnchantmentType.WISE),
            Map.entry("NECRON_BLADE", EnchantmentType.WISE),
            Map.entry("RAGNAROCK_AXE", EnchantmentType.CHIMERA),
            Map.entry("DAEDALUS_AXE", EnchantmentType.CHIMERA),
            Map.entry("STARRED_DAEDALUS_AXE", EnchantmentType.CHIMERA),
            Map.entry("DAEDALUS_BLADE", EnchantmentType.CHIMERA),
            Map.entry("ASPECT_OF_THE_JERRY", EnchantmentType.JERRY),
            Map.entry("TERMINATOR", EnchantmentType.REITERATE),
            Map.entry("SHADOW_FURY", EnchantmentType.COMBO),
            Map.entry("LIVID_DAGGER", EnchantmentType.COMBO)
    );

    private static final List<Map.Entry<EnchantItemGroups, EnchantmentType>> BY_GROUP = List.of(
            Map.entry(EnchantItemGroups.FISHING_ROD, EnchantmentType.FLASH),
            Map.entry(EnchantItemGroups.HOE, EnchantmentType.CROP_FEVER),
            Map.entry(EnchantItemGroups.PICKAXE, EnchantmentType.FLOWSTATE),
            Map.entry(EnchantItemGroups.DRILL, EnchantmentType.FLOWSTATE),
            Map.entry(EnchantItemGroups.GAUNTLET, EnchantmentType.FLOWSTATE),
            Map.entry(EnchantItemGroups.AXE, EnchantmentType.FIRST_IMPRESSION),
            Map.entry(EnchantItemGroups.BOW, EnchantmentType.REITERATE),
            Map.entry(EnchantItemGroups.SWORD, EnchantmentType.SOUL_EATER),
            Map.entry(EnchantItemGroups.LONG_SWORD, EnchantmentType.SOUL_EATER),
            Map.entry(EnchantItemGroups.FISHING_WEAPON, EnchantmentType.SOUL_EATER),
            Map.entry(EnchantItemGroups.NECKLACE, EnchantmentType.THE_ONE),
            Map.entry(EnchantItemGroups.CHESTPLATE, EnchantmentType.LEGION),
            Map.entry(EnchantItemGroups.HELMET, EnchantmentType.WISDOM),
            Map.entry(EnchantItemGroups.LEGGINGS, EnchantmentType.LAST_STAND),
            Map.entry(EnchantItemGroups.BOOTS, EnchantmentType.BANK)
    );

    private UltimateEnchantChoice() {}

    public static @Nullable EnchantmentType forItem(@Nullable String itemName, List<EnchantItemGroups> groups) {
        EnchantmentType known = itemName == null ? null : BY_ITEM.get(itemName);
        if (known != null && appliesTo(known, groups)) return known;
        for (var entry : BY_GROUP) {
            if (groups.contains(entry.getKey()) && appliesTo(entry.getValue(), groups)) return entry.getValue();
        }
        return null;
    }

    private static boolean appliesTo(EnchantmentType type, List<EnchantItemGroups> groups) {
        return type.getEnch().getGroups().stream().anyMatch(groups::contains);
    }
}
