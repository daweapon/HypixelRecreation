package dev.daweapon.type.skyblockgeneric.gui.inventories.auction.view;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import dev.daweapon.type.skyblockgeneric.auction.AuctionClaims;
import dev.daweapon.type.skyblockgeneric.auction.AuctionCancellation;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.gui.inventory.item.GUIItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointUUIDList;
import dev.daweapon.type.skyblockgeneric.gui.inventories.auction.GUIAuctionViewItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.UUID;

// Renders the auction-view slots shown to the owner of an ended Buy-It-Now listing
public class AuctionViewSelfBIN implements AuctionView {
    @Override
    public void open(GUIAuctionViewItem gui, AuctionItem item, SkyBlockPlayer player) {
        java.util.UUID claimProfile = player.getSkyblockDataHandler().getCurrentProfileId();
        if (!item.getBids().isEmpty()) {
            List<UUID> ownedActive = player.getSkyblockDataHandler().get(dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler.Data.AUCTION_ACTIVE_OWNED, DatapointUUIDList.class).getValue();

            if (ownedActive.contains(item.getUuid())) {
                gui.set(new GUIClickableItem(31) {
                    @Override
                    public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                            AuctionClaims.claim((SkyBlockPlayer) p, item.getUuid(), claimProfile, true);
                        }

                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                        return ItemStacks.item(Material.GOLD_BLOCK, 1,
                            Text.key("gui_auction.view_self_bin.collect_auction"),
                            Text.keyLines("gui_auction.view_self_bin.collect_sold.lore",
                                SkyBlockPlayer.getDisplayName(item.getBids().getFirst().uuid()),
                                item.getBids().getFirst().value()));
                    }
                });
            } else {
                gui.set(new GUIItem(31) {
                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                        return ItemStacks.item(Material.BEDROCK, 1,
                            Text.key("gui_auction.view_self_bin.cannot_buy_own"),
                            Text.keyLines("gui_auction.view_self_bin.cannot_buy_own_claimed.lore"));
                    }
                });
            }
            return;
        }

        if (item.getEndTime() < System.currentTimeMillis()) {
            List<UUID> ownedActive = player.getSkyblockDataHandler().get(dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler.Data.AUCTION_ACTIVE_OWNED, DatapointUUIDList.class).getValue();

            if (ownedActive.contains(item.getUuid())) {
                gui.set(new GUIClickableItem(31) {
                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                        return ItemStacks.item(Material.GOLD_INGOT, 1,
                            Text.key("gui_auction.view_self_bin.claim_item_back"),
                            Text.keyLines("gui_auction.view_self_bin.claim_item_back.lore"));
                    }

                    @Override
                    public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                            AuctionClaims.claim((SkyBlockPlayer) p, item.getUuid(), claimProfile, true);
                        }
                });
            } else {
                gui.set(new GUIItem(31) {
                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                        return ItemStacks.item(Material.BEDROCK, 1,
                            Text.key("gui_auction.view_self_bin.cannot_buy_own"),
                            Text.keyLines("gui_auction.view_self_bin.cannot_buy_own_expired.lore"));
                    }
                });
            }
            return;
        }

        gui.set(new GUIClickableItem(31) {
            @Override public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                AuctionCancellation.cancel((SkyBlockPlayer) p, item.getUuid(), claimProfile);
            }
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.BARRIER, 1, Text.of("<c>Cancel Auction"),
                        List.of(Text.of("<7>Cancel this auction and return the item."), Text.of(""), Text.of("<e>Click to cancel!")));
            }
        });
    }
}
