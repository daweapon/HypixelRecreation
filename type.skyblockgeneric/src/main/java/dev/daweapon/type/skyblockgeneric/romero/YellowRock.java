package dev.daweapon.type.skyblockgeneric.romero;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import net.minestom.server.item.Material;

public final class YellowRock {
    private YellowRock() {
    }

    public static boolean isSource(Material material, RegionType region) {
        return material == Material.GOLD_ORE && (region == RegionType.SAVANNA_WOODLAND || region == RegionType.SOUL_CAVE);
    }

    public static boolean silkTouch(SkyBlockItem tool) {
        return tool.getAttributeHandler().hasEnchantment(EnchantmentType.SILK_TOUCH);
    }

    public static boolean isRock(SkyBlockItem item) {
        return ItemType.get("YELLOW_ROCK").equals(item.getAttributeHandler().getPotentialType());
    }
}
