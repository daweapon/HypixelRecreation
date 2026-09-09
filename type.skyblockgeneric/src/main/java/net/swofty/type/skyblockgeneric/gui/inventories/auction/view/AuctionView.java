package net.swofty.type.skyblockgeneric.gui.inventories.auction.view;

import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.type.skyblockgeneric.gui.inventories.auction.GUIAuctionViewItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Strategy for rendering the role/type-specific slots of an auction listing view
public interface AuctionView {
    void open(GUIAuctionViewItem gui, AuctionItem item, SkyBlockPlayer player);
}
