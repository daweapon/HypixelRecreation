package dev.daweapon.type.skyblockgeneric.spidersden;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.LinkedHashMap;
import java.util.Map;

/** Shaggy's Arachne Crystal: 2 Arachne Fragment, 16 Enchanted Spider Eye and 16 Enchanted String (wiki: Shaggy, Arachne Fragment). */
public final class ArachneCrystalTrade {
    public static final Map<ItemType, Integer> COST = new LinkedHashMap<>();

    static {
        COST.put(ItemType.ARACHNE_FRAGMENT, 2);
        COST.put(ItemType.ENCHANTED_SPIDER_EYE, 16);
        COST.put(ItemType.ENCHANTED_STRING, 16);
    }

    private ArachneCrystalTrade() {
    }

    public static boolean canAfford(SkyBlockPlayer player) {
        return COST.entrySet().stream().allMatch(entry -> player.getAmountAnywhere(entry.getKey()) >= entry.getValue());
    }

    public static boolean trade(SkyBlockPlayer player) {
        if (!canAfford(player) || !player.removeItemsFromPlayer(COST)) return false;
        player.addAndUpdateItem(new SkyBlockItem(ItemType.ARACHNE_CRYSTAL));
        return true;
    }
}
