package dev.daweapon.type.hub.gui.shen;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.shen.ShenAuctionItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/** The display item for something Shen auctions: the real item when it exists, else its look-alike. */
final class ShenItemIcons {
    private ShenItemIcons() {}

    static ItemStack.Builder icon(SkyBlockPlayer player, ShenAuctionItem item, List<Text> extraLore) {
        ItemType type = item.itemType();
        if (item.isAvailable() && type != null) {
            ItemStack.Builder builder = PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(type).getItemStack());
            List<Text> lore = new ArrayList<>(ItemStacks.loreText(builder));
            lore.addAll(extraLore);
            return ItemStacks.lore(builder, lore);
        }
        Text name = Text.of(item.getDisplayName());
        if (item.getHeadTexture() != null) return ItemStacks.head(item.getHeadTexture(), name, extraLore);
        return ItemStacks.item(item.getIcon(), 1, name, extraLore);
    }
}
