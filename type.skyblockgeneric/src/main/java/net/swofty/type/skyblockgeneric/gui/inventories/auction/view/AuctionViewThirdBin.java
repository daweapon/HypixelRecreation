package net.swofty.type.skyblockgeneric.gui.inventories.auction.view;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.type.skyblockgeneric.auction.AuctionClaims;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointUUIDList;
import net.swofty.type.skyblockgeneric.gui.inventories.auction.GUIAuctionViewItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;


// Renders the auction-view slots shown to a non-owner viewing a Buy-It-Now listing
public class AuctionViewThirdBin implements AuctionView {
    @Override
    public void open(GUIAuctionViewItem gui, AuctionItem item, SkyBlockPlayer player) {
        java.util.UUID claimProfile = player.getSkyblockDataHandler().getCurrentProfileId();
        if (!item.getBids().isEmpty()) {
            if (item.getBids().getFirst().uuid().equals(player.getUuid())) {
                DatapointUUIDList activeBids = player.getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.AUCTION_ACTIVE_BIDS, DatapointUUIDList.class);

                if (activeBids.getValue().contains(item.getUuid())) {
                    gui.set(new GUIClickableItem(31) {
                        @Override
                        public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                            AuctionClaims.claim((SkyBlockPlayer) p, item.getUuid(), claimProfile, false);
                        }

                        @Override
                        public ItemStack.Builder getItem(HypixelPlayer p) {
                            return ItemStacks.item(Material.GOLD_BLOCK, 1,
                                Text.key("gui_auction.view_third_bin.claim_item"),
                                Text.keyLines("gui_auction.view_third_bin.claim_sold.lore",
                                    SkyBlockPlayer.getDisplayName(item.getBids().getFirst().uuid()),
                                    item.getBids().getFirst().value()));
                        }
                    });
                } else {
                    gui.set(new GUIItem(31) {
                        @Override
                        public ItemStack.Builder getItem(HypixelPlayer p) {
                            return ItemStacks.item(Material.BEDROCK, 1,
                                Text.key("gui_auction.view_third_bin.item_sold"),
                                Text.keyLines("gui_auction.view_third_bin.item_sold_claimed.lore",
                                    SkyBlockPlayer.getDisplayName(item.getBids().getFirst().uuid()),
                                    item.getBids().getFirst().value()));
                        }
                    });
                }
                return;
            }

            gui.set(new GUIItem(31) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(Material.BEDROCK, 1,
                        Text.key("gui_auction.view_third_bin.item_sold"),
                        Text.keyLines("gui_auction.view_third_bin.item_sold_other.lore",
                            SkyBlockPlayer.getDisplayName(item.getBids().getFirst().uuid()),
                            item.getBids().getFirst().value()));
                }
            });
            return;
        }

        gui.set(new GUIClickableItem(31) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                net.swofty.type.skyblockgeneric.auction.AuctionBidding.submit((SkyBlockPlayer) p,
                        claimProfile, item.getUuid(), item.getStartingPrice(), true);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.GOLD_NUGGET, 1,
                    Text.key("gui_auction.view_third_bin.buy_now"),
                    Text.keyLines("gui_auction.view_third_bin.buy_now.lore", item.getStartingPrice()));
            }
        });
    }

}
