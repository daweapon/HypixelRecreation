package net.swofty.type.skyblockgeneric.gui.inventories.auction;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServiceType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIQueryItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.auction.AuctionCreation;
import net.swofty.type.skyblockgeneric.auction.AuctionEligibility;
import net.swofty.commons.skyblock.auctions.AuctionRules;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAuctionEscrow;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// GUI for creating a new auction listing (or BIN) from an item held in escrow
public class GUIAuctionCreateItem extends HypixelInventoryGUI implements RefreshingGUI {
    private final HypixelInventoryGUI previousGUI;
    private java.util.UUID profile;

    public GUIAuctionCreateItem(HypixelInventoryGUI previousGUI) {
        super(Text.key("gui_auction.create.title"), InventoryType.CHEST_6_ROW);

        this.previousGUI = previousGUI;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        profile = ((SkyBlockPlayer) getPlayer()).getSkyblockDataHandler().getCurrentProfileId();
        if (((SkyBlockPlayer) getPlayer()).isIronman()) {
            getPlayer().sendMessage(Text.key("gui_auction.house.ironman_blocked"));
            getPlayer().closeInventory();
            return;
        }
        fill(Material.BLACK_STAINED_GLASS_PANE, "");
        set(GUIClickableItem.getGoBackItem(49, previousGUI));

        DatapointAuctionEscrow.AuctionEscrow escrow = ((SkyBlockPlayer) getPlayer()).getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.AUCTION_ESCROW, DatapointAuctionEscrow.class).getValue();
        if (escrow.isPending()) {
            set(new GUIClickableItem(29) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(Material.CLOCK, 1, "<e>Check Pending Auction",
                            "<7>Your item and fee are reserved.\n<7>Click to retry without paying again.");
                }
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    AuctionCreation.submit((SkyBlockPlayer) player, profile);
                }
            });
            return;
        }
        if (escrow.isBin())
            setTitle(Text.key("gui_auction.create.title_bin"));

        set(new GUIClickableItem(13) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                if (escrow.getItem() == null)
                    return ItemStacks.item(Material.STONE_BUTTON, 1,
                            Text.key("gui_auction.create.select_item"),
                            List.of(Text.key("gui_auction.create.select_item.lore")));

                SkyBlockItem item = escrow.getItem();
                ItemStack itemStack = new NonPlayerItemUpdater(item).getUpdatedItem().build();
                List<Text> lore = new ArrayList<>();

                lore.add(Text.literal(" "));
                lore.add(ItemStacks.nameText(itemStack));
                lore.addAll(ItemStacks.loreText(itemStack));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_auction.create.auction_for_item_pickup"));

                return ItemStacks.item(itemStack.material(), itemStack.amount(),
                        Text.key("gui_auction.create.auction_for_item"), lore);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                synchronized (escrow) {
                    if (escrow.isPending() || escrow.getItem() == null) return;
                    player.addAndUpdateItem(escrow.getItem());
                    escrow.setItem(null);
                }

                new GUIAuctionCreateItem(previousGUI).open(player);
            }
        });

        set(new GUIClickableItem(33) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUIAuctionDuration().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>();
                if (escrow.isBin()) {
                    lore.addAll(Text.keyLines("gui_auction.create.duration_bin.lore"));
                } else {
                    lore.addAll(Text.keyLines("gui_auction.create.duration_normal.lore"));
                }
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_auction.create.duration_extra_fee", AuctionRules.durationFee(escrow.getDuration())));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_auction.create.duration_click"));

                return ItemStacks.item(Material.CLOCK, 1,
                        Text.key("gui_auction.create.duration_label", StringUtility.getAuctionSetupFormattedTime(escrow.getDuration())),
                        lore);
            }
        });
        set(new GUIClickableItem(48) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                escrow.setBin(!escrow.isBin());
                new GUIAuctionCreateItem(previousGUI).open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                if (escrow.isBin()) {
                    return ItemStacks.item(Material.POWERED_RAIL, 1,
                            Text.key("gui_auction.create.switch_to_auction"),
                            Text.keyLines("gui_auction.create.switch_to_auction.lore"));
                } else {
                    return ItemStacks.item(Material.GOLD_INGOT, 1,
                            Text.key("gui_auction.create.switch_to_bin"),
                            Text.keyLines("gui_auction.create.switch_to_bin.lore"));
                }
            }
        });
        set(new GUIClickableItem(29) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                AuctionCreation.submit(player, profile);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                if (escrow.getItem() == null) {
                    return ItemStacks.item(Material.RED_TERRACOTTA, 1,
                            Text.key("gui_auction.create.submit_no_item"),
                            Text.keyLines("gui_auction.create.submit_no_item.lore"));
                } else {
                    return ItemStacks.item(Material.GREEN_TERRACOTTA, 1,
                        Text.key("gui_auction.create.submit_ready", escrow.isBin() ? "Bin " : ""),
                        Text.keyLines("gui_auction.create.submit_ready.lore",
                            escrow.getItem().getDisplayNameText(),
                            StringUtility.getAuctionSetupFormattedTime(escrow.getDuration()),
                            escrow.isBin() ? Text.key("gui_auction.create.price_bin_label") : Text.key("gui_auction.create.price_normal_label"),
                            StringUtility.commaify(escrow.getPrice()),
                            AuctionRules.listingFee(escrow.getPrice(), escrow.getDuration(), escrow.isBin())));
                }
            }
        });
        set(new GUIQueryItem(31) {

            @Override
            public HypixelInventoryGUI onQueryFinish(String query, HypixelPlayer player) {
                long val;
                try {
                    val = Long.parseLong(query);
                } catch (NumberFormatException ex) {
                    player.sendMessage(Text.key("gui_auction.create.number_parse_error"));
                    return GUIAuctionCreateItem.this;
                }
                if (val <= 0 || val > AuctionRules.MAX_PRICE) {
                    player.sendMessage(Text.of("<c>Enter a price from 1 to 50,000,000,000 coins."));
                    return GUIAuctionCreateItem.this;
                }
                escrow.setPrice(val);

                return GUIAuctionCreateItem.this;
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                Material material;
                List<Text> lore = new ArrayList<>();
                if (escrow.isBin()) {
                    material = Material.GOLD_INGOT;
                    lore.addAll(Text.keyLines("gui_auction.create.price_bin.lore"));
                } else {
                    material = Material.POWERED_RAIL;
                    lore.addAll(Text.keyLines("gui_auction.create.price_normal.lore"));
                }
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_auction.create.price_extra_fee",
                        AuctionRules.listingFee(escrow.getPrice(), escrow.getDuration(), escrow.isBin())
                                - AuctionRules.durationFee(escrow.getDuration())));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_auction.create.price_click"));

                String priceKey = escrow.isBin() ? "gui_auction.create.price_label_bin" : "gui_auction.create.price_label_normal";
                return ItemStacks.item(material, 1,
                    Text.key(priceKey, StringUtility.commaify(escrow.getPrice())), lore);
            }
        });

        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {

    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {

    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        ItemStack current = e.getClickedItem();
        SkyBlockItem item = new SkyBlockItem(current);

        if (item.isNA()) return;
        if (item.isAir()) return;

        if (!AuctionEligibility.canList(item)) {
            e.setCancelled(true);
            ((HypixelPlayer) e.getPlayer()).sendMessage(Text.of("<c>This item cannot be auctioned."));
            return;
        }

        DatapointAuctionEscrow.AuctionEscrow escrow = ((SkyBlockPlayer) getPlayer()).getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.AUCTION_ESCROW, DatapointAuctionEscrow.class).getValue();

        synchronized (escrow) {
            if (escrow.isPending()) {
                e.setCancelled(true);
                return;
            }
            if (escrow.getItem() != null) {
                ((HypixelPlayer) e.getPlayer()).sendMessage(Text.key("gui_auction.create.already_have_item"));
                return;
            }

            e.setCancelled(true);
            escrow.setItem(item);
            e.getPlayer().getInventory().setItemStack(e.getSlot(), ItemStack.AIR);
        }
        new GUIAuctionCreateItem(previousGUI).open(getPlayer());
    }

    @Override
    public void refreshItems(HypixelPlayer player) {
        new ProxyService(ServiceType.AUCTION_HOUSE).isOnline().thenAccept(online -> {
            if (!online) {
                player.sendMessage(Text.key("gui_auction.create.offline_message"));
                player.closeInventory();
            }
        });
    }

    @Override
    public int refreshRate() {
        return 10;
    }
}
