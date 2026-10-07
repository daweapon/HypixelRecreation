package net.swofty.type.hub.gui.shen;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.shen.ShenAuctionItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
