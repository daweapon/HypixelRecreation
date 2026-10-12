package net.swofty.type.skyblockgeneric.item.components;

import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;

import java.util.Set;

public class DyeComponent extends SkyBlockItemComponent {
    private static final Set<Material> LEATHER_ARMOR = Set.of(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);

    public DyeComponent(String itemId, String color) {
        String handlerId = "DYE:" + itemId;
        AnvilCombineRegistry.register(handlerId, new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> upgradeItem.getAttributeHandler().setDyeColor(color),
                (player, upgradeItem, sacrificeItem) -> LEATHER_ARMOR.contains(upgradeItem.getMaterial())
                        && !color.equals(upgradeItem.getAttributeHandler().getDyeColor()),
                (upgradeItem, sacrificeItem, player) -> 0,
                null));
        addInheritedComponent(new AnvilCombinableComponent(handlerId));
    }
}
