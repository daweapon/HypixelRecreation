package net.swofty.type.skyblockgeneric.fishing.winter;

import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;

import java.util.Map;
import java.util.Set;

// Anvil handlers for the Winter Sea Creature dyes, which recolour leather armour like other dyes.
public final class WinterItemHandlers {
    private static final Set<Material> LEATHER_ARMOR = Set.of(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
    private static final Map<String, String> DYES = Map.of(
            "DYE_ICEBERG", "#71A6D2",
            "DYE_AQUAMARINE", "#7FFFD4");

    private WinterItemHandlers() {
    }

    public static void registerAnvilCombines() {
        DYES.forEach((handlerId, color) -> AnvilCombineRegistry.register(handlerId, new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> upgradeItem.getAttributeHandler().setDyeColor(color),
                (player, upgradeItem, sacrificeItem) -> LEATHER_ARMOR.contains(upgradeItem.getMaterial())
                        && !color.equals(upgradeItem.getAttributeHandler().getDyeColor()),
                (upgradeItem, sacrificeItem, player) -> 0,
                null)));
    }
}
