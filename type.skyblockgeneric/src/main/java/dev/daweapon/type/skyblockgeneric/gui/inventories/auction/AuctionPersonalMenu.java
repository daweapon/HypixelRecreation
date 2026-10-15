package dev.daweapon.type.skyblockgeneric.gui.inventories.auction;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.protocol.objects.auctions.AuctionFetchItemProtocol;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import dev.daweapon.commons.text.Text;
import dev.daweapon.proxyapi.ProxyService;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointUUIDList;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

abstract class AuctionPersonalMenu extends AuctionPagedMenu {
    private final boolean owned;

    protected AuctionPersonalMenu(boolean owned) {
        super(Text.key(owned ? "gui_auction.manage.title" : "gui_auction.bids.title"), InventoryType.CHEST_3_ROW,
                new int[]{10, 11, 12, 13, 14, 15, 16}, 18, 26, 21);
        this.owned = owned;
    }

    @Override protected CompletableFuture<List<AuctionItem>> fetch(SkyBlockPlayer player) {
        var field = owned ? SkyBlockDataHandler.Data.AUCTION_ACTIVE_OWNED : SkyBlockDataHandler.Data.AUCTION_ACTIVE_BIDS;
        var ids = List.copyOf(player.getSkyblockDataHandler().get(field, DatapointUUIDList.class).getValue());
        var service = new ProxyService(ServiceType.AUCTION_HOUSE);
        var futures = ids.stream().distinct().map(id -> service.handleRequest(new AuctionFetchItemProtocol(),
                new AuctionFetchItemProtocol.AuctionFetchItemMessage(id)).thenApply(result -> result.success() ? result.item() : null)).toList();
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenApply(ignored -> futures.stream()
                .map(CompletableFuture::join).filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(AuctionItem::getEndTime).reversed().thenComparing(AuctionItem::getUuid)).toList());
    }

    @Override protected void controls() {
        set(GUIClickableItem.getGoBackItem(22, new GUIAuctionHouse()));
        if (owned) set(new GUIClickableItem(23) {
            @Override public void run(InventoryPreClickEvent event, HypixelPlayer player) {
                new GUIAuctionCreateItem(AuctionPersonalMenu.this).open(player);
            }
            @Override public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.GOLDEN_HORSE_ARMOR, 1, Text.key("gui_auction.manage.create_button"),
                        Text.keyLines("gui_auction.manage.create_button.lore"));
            }
        });
    }
}
