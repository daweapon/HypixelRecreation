package dev.daweapon.type.skyblockgeneric.gui.inventories.auction.view;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.auction.AuctionBidRefund;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import dev.daweapon.commons.skyblock.auctions.AuctionRules;
import dev.daweapon.type.skyblockgeneric.auction.AuctionClaims;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.gui.inventory.item.GUIItem;
import dev.daweapon.type.generic.gui.inventory.item.GUIQueryItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointUUIDList;
import dev.daweapon.type.skyblockgeneric.gui.inventories.auction.GUIAuctionViewItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.*;

// Renders the auction-view slots shown to a non-owner bidding on a normal (bid-based) listing
public class AuctionViewThirdNormal implements AuctionView {
    @Override
    public void open(GUIAuctionViewItem gui, AuctionItem item, SkyBlockPlayer player) {
        java.util.UUID claimProfile = player.getSkyblockDataHandler().getCurrentProfileId();
        AuctionItem.Bid highestBid = item.getBids().stream().max(Comparator.comparingLong(AuctionItem.Bid::value)).orElse(null);
        if (highestBid == null) {
            if (gui.bidAmount == 0)
                gui.bidAmount = item.getStartingPrice();
            gui.minimumBidAmount = item.getStartingPrice();
        } else {
            if (gui.bidAmount == 0)
                gui.bidAmount = AuctionRules.minimumBid(item.getStartingPrice(), highestBid.value());
            gui.minimumBidAmount = AuctionRules.minimumBid(item.getStartingPrice(), highestBid.value());
        }

        gui.set(new GUIItem(33) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_auction.view_third_normal.bid_history_total", item.getBids().size()));

                List<AuctionItem.Bid> bids = new ArrayList<>(item.getBids());
                bids.sort(Comparator.comparingLong(AuctionItem.Bid::value).reversed());

                for (int i = 0; i < 10; i++) {
                    if (i >= bids.size())
                        break;
                    AuctionItem.Bid bid = bids.get(i);

                    lore.add(Text.key("gui_auction.view_third_normal.bid_separator"));
                    lore.add(Text.key("gui_auction.view_third_normal.bid_value", bid.value()));
                    lore.add(Text.key("gui_auction.view_third_normal.bid_by", SkyBlockPlayer.getDisplayName(bid.uuid())));
                    lore.add(Text.of("<b>{}", StringUtility.formatTimeAsAgo(bid.timestamp())));
                }

                return ItemStacks.item(Material.FILLED_MAP, 1,
                        Text.key("gui_auction.view_third_normal.bid_history"), lore);
            }
        });

        if (System.currentTimeMillis() > item.getEndTime()) {
            DatapointUUIDList activeBids = player.getSkyblockDataHandler().get(dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler.Data.AUCTION_ACTIVE_BIDS, DatapointUUIDList.class);

            AuctionItem.Bid winningBid = item.getBids().stream().max(Comparator.comparingLong(AuctionItem.Bid::value)).orElse(null);
            long refund = AuctionBidRefund.amount(item, player.getUuid());
            if (winningBid == null || !winningBid.uuid().equals(player.getUuid())) {
                if (activeBids.getValue().contains(item.getUuid())) {
                    gui.set(new GUIClickableItem(29) {
                        @Override
                        public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                            AuctionClaims.claim((SkyBlockPlayer) p, item.getUuid(), claimProfile, false);
                        }

                        @Override
                        public ItemStack.Builder getItem(HypixelPlayer p) {
                            return ItemStacks.item(Material.BARRIER, 1,
                                Text.key("gui_auction.view_third_normal.auction_ended_lost"),
                                Text.keyLines("gui_auction.view_third_normal.auction_ended_lost_claim.lore",
                                    refund));
                        }
                    });
                } else {
                    gui.set(new GUIItem(29) {
                        @Override
                        public ItemStack.Builder getItem(HypixelPlayer p) {
                            return ItemStacks.item(Material.BARRIER, 1,
                                    Text.key("gui_auction.view_third_normal.auction_ended_lost"),
                                    Text.keyLines("gui_auction.view_third_normal.auction_ended_lost_no_claim.lore"));
                        }
                    });
                }
            } else {
                if (activeBids.getValue().contains(item.getUuid())) {
                    gui.set(new GUIClickableItem(29) {
                        @Override
                        public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                            AuctionClaims.claim((SkyBlockPlayer) p, item.getUuid(), claimProfile, false);
                        }

                        @Override
                        public ItemStack.Builder getItem(HypixelPlayer p) {
                            return ItemStacks.item(Material.EMERALD, 1,
                                    Text.key("gui_auction.view_third_normal.auction_ended_won"),
                                    Text.keyLines("gui_auction.view_third_normal.auction_ended_won_claim.lore"));
                        }
                    });
                } else {
                    gui.set(new GUIItem(29) {
                        @Override
                        public ItemStack.Builder getItem(HypixelPlayer p) {
                            return ItemStacks.item(Material.EMERALD, 1,
                                    Text.key("gui_auction.view_third_normal.auction_ended_won"),
                                    Text.keyLines("gui_auction.view_third_normal.auction_ended_won_claimed.lore"));
                        }
                    });
                }
            }
            return;
        }

        gui.set(new GUIQueryItem(31) {
            @Override
            public HypixelInventoryGUI onQueryFinish(String query, HypixelPlayer player) {
                long val;
                try {
                    val = dev.daweapon.type.skyblockgeneric.utility.CoinAmountParser.parseLong(query);
                } catch (NumberFormatException ex) {
                    player.sendMessage(Text.key("gui_auction.view_third_normal.number_parse_error"));
                    return gui;
                }
                if (val < gui.minimumBidAmount) {
                    player.sendMessage(Text.key("gui_auction.view_third_normal.bid_too_low", gui.minimumBidAmount));
                    return gui;
                }

                gui.bidAmount = val;

                return gui;
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.GOLD_INGOT, 1,
                    Text.key("gui_auction.view_third_normal.bid_amount", gui.bidAmount),
                    Text.keyLines("gui_auction.view_third_normal.bid_amount.lore", gui.minimumBidAmount, gui.bidAmount));
            }
        });
        gui.set(new GUIClickableItem(29) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                if (gui.bidAmount < gui.minimumBidAmount) {
                    player.sendMessage(Text.key("gui_auction.view_third_normal.bid_too_low", gui.minimumBidAmount));
                    return;
                }

                dev.daweapon.type.skyblockgeneric.auction.AuctionBidding.submit(player,
                        claimProfile, item.getUuid(), gui.bidAmount, false);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.GOLD_NUGGET, 1,
                    Text.key("gui_auction.view_third_normal.submit_bid"),
                    Text.keyLines("gui_auction.view_third_normal.submit_bid.lore", gui.bidAmount));
            }
        });
    }

}
