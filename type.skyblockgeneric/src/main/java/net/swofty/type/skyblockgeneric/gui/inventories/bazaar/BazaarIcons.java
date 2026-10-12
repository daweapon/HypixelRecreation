package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.EnchantedComponent;
import net.swofty.type.skyblockgeneric.item.components.ItemModelComponent;
import net.swofty.type.skyblockgeneric.item.components.SkullHeadComponent;

import java.util.List;

final class BazaarIcons {
    private BazaarIcons() {
    }

    static ItemStack.Builder of(ItemType type, Text name, List<Text> lore, boolean glint) {
        SkyBlockItem item = new SkyBlockItem(type);
        ItemStack.Builder builder;
        if (item.hasComponent(SkullHeadComponent.class)) {
            builder = ItemStacks.head(item.getComponent(SkullHeadComponent.class).getSkullTexture(item), 1, name, lore);
        } else {
            builder = ItemStacks.item(item.getMaterial(), 1, name, lore);
        }
        if (item.hasComponent(ItemModelComponent.class)) {
            builder = builder.set(DataComponents.ITEM_MODEL, item.getComponent(ItemModelComponent.class).getItemModel());
        }
        if (glint && item.hasComponent(EnchantedComponent.class)) {
            return ItemStacks.enchanted(builder);
        }
        return builder;
    }
}
