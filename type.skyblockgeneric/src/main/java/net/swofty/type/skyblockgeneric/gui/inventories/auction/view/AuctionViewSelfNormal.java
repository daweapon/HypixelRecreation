package net.swofty.type.skyblockgeneric.gui.inventories.auction.view;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.type.skyblockgeneric.auction.AuctionClaims;
import net.swofty.type.skyblockgeneric.auction.AuctionCancellation;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointUUIDList;
import net.swofty.type.skyblockgeneric.gui.inventories.auction.GUIAuctionViewItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

// Renders the auction-view slots shown to the owner of an active/ended normal (bid-based) listing
public class AuctionViewSelfNormal implements AuctionView {
    @Override
    public void open(GUIAuctionViewItem gui, AuctionItem item, SkyBlockPlayer player) {
        java.util.UUID claimProfile = player.getSkyblockDataHandler().getCurrentProfileId();
        gui.set(new GUIItem(33) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_auction.view_self_normal.bid_history_total", item.getBids().size()));

                List<AuctionItem.Bid> bids = new ArrayList<>(item.getBids());
                bids.sort(Comparator.comparingLong(AuctionItem.Bid::value).reversed());

                for (int i = 0; i < 10; i++) {
                    if (i >= bids.size())
                        break;
                    AuctionItem.Bid bid = bids.get(i);

                    lore.add(Text.key("gui_auction.view_self_normal.bid_separator"));
                    lore.add(Text.key("gui_auction.view_self_normal.bid_value", bid.value()));
                    lore.add(Text.key("gui_auction.view_self_normal.bid_by", SkyBlockPlayer.getDisplayName(bid.uuid())));
                    lore.add(Text.of("<b>{}", StringUtility.formatTimeAsAgo(bid.timestamp())));
                }

                return ItemStacks.item(Material.FILLED_MAP, 1,
                        Text.key("gui_auction.view_self_normal.bid_history"), lore);
            }
        });

        if (item.getEndTime() < System.currentTimeMillis()) {
            List<UUID> ownedActive = player.getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.AUCTION_ACTIVE_OWNED, DatapointUUIDList.class).getValue();

            if (ownedActive.contains(item.getUuid())) {
                if (item.getBids().isEmpty()) {
                    gui.set(new GUIClickableItem(29) {
                        @Override
                        public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                            AuctionClaims.claim((SkyBlockPlayer) p, item.getUuid(), claimProfile, true);
                        }

                        @Override
                        public ItemStack.Builder getItem(HypixelPlayer p) {
                            SkyBlockPlayer player = (SkyBlockPlayer) p;
                            return ItemStacks.item(Material.GOLD_BLOCK, 1,
                                Text.key("gui_auction.view_self_normal.collect_auction"),
                                Text.keyLines("gui_auction.view_self_normal.collect_no_bids.lore"));
                        }
                    });
                } else {
                    gui.set(new GUIClickableItem(29) {
                        @Override
                        public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                            AuctionClaims.claim((SkyBlockPlayer) p, item.getUuid(), claimProfile, true);
                        }

                        @Override
                        public ItemStack.Builder getItem(HypixelPlayer p) {
                            return ItemStacks.item(Material.GOLD_BLOCK, 1,
                                Text.key("gui_auction.view_self_normal.collect_auction"),
                                Text.keyLines("gui_auction.view_self_normal.collect_with_bids.lore",
                                    item.getBids().stream().max(Comparator.comparingLong(AuctionItem.Bid::value)).map(AuctionItem.Bid::value).orElse(0L)));
                        }
                    });
                }
            } else {
                gui.set(new GUIItem(29) {
                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                        return ItemStacks.item(Material.BARRIER, 1,
                            Text.key("gui_auction.view_self_normal.auction_ended"),
                            Text.keyLines("gui_auction.view_self_normal.auction_ended_claimed.lore",
                                item.getBids().stream().max(Comparator.comparingLong(AuctionItem.Bid::value)).map(AuctionItem.Bid::value).orElse(0L)));
                    }
                });
            }
            return;
        }

        if (item.getBids().isEmpty()) {
            gui.set(new GUIClickableItem(29) {
                @Override public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    AuctionCancellation.cancel((SkyBlockPlayer) p, item.getUuid(), claimProfile);
                }
                @Override public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(Material.BARRIER, 1, Text.of("<c>Cancel Auction"),
                            List.of(Text.of("<7>Cancel this auction and return the item."), Text.of(""), Text.of("<e>Click to cancel!")));
                }
            });
            return;
        }
        gui.set(new GUIItem(29) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.BEDROCK, 1,
                    Text.key("gui_auction.view_self_normal.own_auction"),
                    Text.keyLines("gui_auction.view_self_normal.own_auction.lore"));
            }
        });
    }
}
