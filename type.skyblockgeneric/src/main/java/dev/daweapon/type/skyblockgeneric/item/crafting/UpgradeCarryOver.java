package dev.daweapon.type.skyblockgeneric.item.crafting;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Upgrade recipes (Ancestral Spade into Archaic Spade, Crown of Greed into Crown of Avarice, ...)
 * keep what the player put into the item they upgraded: its enchantments, reforge, books,
 * gemstones and so on.
 */
public final class UpgradeCarryOver {
    private static final Set<String> CARRIED = Set.of(
            "enchantments", "reforge", "hot_potato_book_data", "recombobulated", "gems", "soul_bound",
            "dye_color", "rune_infused_with", "rune_level", "coins_consumed", "applied_scrolls", "abiphone_extra_contacts", "sack_upgrades",
            "farming_tool_xp", "farming_tool_overflow", "farming_tool_overclockers", "drill_data");

    private UpgradeCarryOver() {
    }

    public static SkyBlockItem apply(SkyBlockItem result, ItemType upgradesFrom, SkyBlockItem[] grid) {
        SkyBlockItem source = find(upgradesFrom, grid);
        if (source == null) return result;
        SkyBlockItem upgraded = result.clone();
        for (ItemAttribute attribute : upgraded.attributes) {
            if (!CARRIED.contains(attribute.getKey())) continue;
            ItemAttribute carried = source.getAttribute(attribute.getKey());
            if (carried != null) attribute.setValue(carried.getValue());
        }
        dev.daweapon.type.skyblockgeneric.garden.tool.FarmingTools.refresh(upgraded);
        return upgraded;
    }

    private static @Nullable SkyBlockItem find(ItemType type, SkyBlockItem[] grid) {
        for (SkyBlockItem item : grid) {
            if (item != null && !item.isNA() && item.getAttributeHandler().getPotentialType() == type) return item;
        }
        return null;
    }
}
