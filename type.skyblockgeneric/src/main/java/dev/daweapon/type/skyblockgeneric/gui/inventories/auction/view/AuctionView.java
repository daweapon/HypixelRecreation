package dev.daweapon.type.skyblockgeneric.gui.inventories.auction.view;

import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import dev.daweapon.type.skyblockgeneric.gui.inventories.auction.GUIAuctionViewItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Strategy for rendering the role/type-specific slots of an auction listing view
public interface AuctionView {
    void open(GUIAuctionViewItem gui, AuctionItem item, SkyBlockPlayer player);
}
