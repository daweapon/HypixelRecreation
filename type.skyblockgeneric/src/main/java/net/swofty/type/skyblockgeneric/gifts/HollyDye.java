package net.swofty.type.skyblockgeneric.gifts;

import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;

import java.util.Set;

public final class HollyDye {
    private static final String COLOR = "#3C6746";
    private static final Set<Material> LEATHER_ARMOR = Set.of(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);

    private HollyDye() {
    }

    public static void registerAnvilCombine() {
        AnvilCombineRegistry.register("DYE_HOLLY", new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> upgradeItem.getAttributeHandler().setDyeColor(COLOR),
                (player, upgradeItem, sacrificeItem) -> LEATHER_ARMOR.contains(upgradeItem.getMaterial())
                        && !COLOR.equals(upgradeItem.getAttributeHandler().getDyeColor()),
                (upgradeItem, sacrificeItem, player) -> 0,
                null));
    }
}
