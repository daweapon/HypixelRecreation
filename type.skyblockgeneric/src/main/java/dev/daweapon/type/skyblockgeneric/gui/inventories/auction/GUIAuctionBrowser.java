package dev.daweapon.type.skyblockgeneric.gui.inventories.auction;

import lombok.Getter;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.protocol.objects.auctions.AuctionFetchItemsProtocol;
import dev.daweapon.commons.skyblock.auctions.AuctionCategories;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import dev.daweapon.commons.skyblock.auctions.AuctionsFilter;
import dev.daweapon.commons.skyblock.auctions.AuctionsSorting;
import dev.daweapon.commons.text.Text;
import dev.daweapon.proxyapi.ProxyService;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// GUI for browsing, sorting, and filtering paginated auction house listings by category
public class GUIAuctionBrowser extends AuctionPagedMenu {
    private static final int[] PAGINATED_SLOTS = new int[]{
            11, 12, 13, 14, 15, 16,
            20, 21, 22, 23, 24, 25,
            29, 30, 31, 32, 33, 34,
            38, 39, 40, 41, 42, 43
    };

    private AuctionsSorting sorting = AuctionsSorting.HIGHEST_BID;
    private AuctionsFilter filter = AuctionsFilter.SHOW_ALL;
    @Getter
    private AuctionCategories category = AuctionCategories.WEAPONS;

    public GUIAuctionBrowser() {
        super(Text.key("gui_auction.browser.title"), InventoryType.CHEST_6_ROW, PAGINATED_SLOTS, 46, 53, 48);

    }

    @Override protected CompletableFuture<List<AuctionItem>> fetch(SkyBlockPlayer player) {
        return new ProxyService(ServiceType.AUCTION_HOUSE).handleRequest(new AuctionFetchItemsProtocol(),
                new AuctionFetchItemsProtocol.AuctionFetchItemsMessage(sorting, filter, category)).thenApply(response -> {
            if (!response.success() || response.items() == null) throw new IllegalStateException("Auction browse failed");
            return response.items();
        });
    }

    @Override protected Material background() { return category.getMaterial(); }

    @Override protected void controls() {
        set(GUIClickableItem.getGoBackItem(49, new GUIAuctionHouse()));
        setTitle(Text.key("gui_auction.browser.title_with_category",
            StringUtility.toNormalCase(category.name())));

        set(new GUIClickableItem(50) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                changeSelection(() -> {
                    sorting = sorting.next();
                    if (filter == AuctionsFilter.BIN_ONLY && sorting == AuctionsSorting.MOST_BIDS) sorting = AuctionsSorting.HIGHEST_BID;
                });
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>(List.of(Text.literal(" ")));

                Arrays.stream(AuctionsSorting.values()).forEach(sort -> {
                    if (filter.equals(AuctionsFilter.BIN_ONLY)) {
                        if (sort.equals(AuctionsSorting.MOST_BIDS)) {
                            return;
                        }
                    }

                    if (sort == sorting) {
                        lore.add(Text.of("<key:'gui_auction.browser.sort_selected_prefix'><b>{}",
                                StringUtility.toNormalCase(sort.name())));
                    } else {
                        lore.add(Text.of("<key:'gui_auction.browser.sort_unselected_prefix'><7>{}",
                                StringUtility.toNormalCase(sort.name())));
                    }
                });

                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_auction.browser.sort_click"));

                return ItemStacks.item(Material.HOPPER, 1,
                        Text.key("gui_auction.browser.sort_button"), lore);
            }
        });
        set(new GUIClickableItem(52) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                changeSelection(() -> {
                    filter = e.getClick() instanceof Click.Right ? filter.previous() : filter.next();
                    if (filter == AuctionsFilter.BIN_ONLY && sorting == AuctionsSorting.MOST_BIDS) sorting = AuctionsSorting.HIGHEST_BID;
                });
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>(List.of(Text.literal(" ")));

                Arrays.stream(AuctionsFilter.values()).forEach(filter -> {
                    if (filter == GUIAuctionBrowser.this.filter) {
                        lore.add(Text.of("<key:'gui_auction.browser.filter_selected_prefix'><b>{}",
                                StringUtility.toNormalCase(filter.name())));
                    } else {
                        lore.add(Text.of("<key:'gui_auction.browser.filter_unselected_prefix'><7>{}",
                                StringUtility.toNormalCase(filter.name())));
                    }
                });

                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_auction.browser.filter_right_click"));
                lore.add(Text.key("gui_auction.browser.filter_click"));

                return ItemStacks.item(Material.GOLD_BLOCK, 1,
                        Text.key("gui_auction.browser.filter_button"), lore);
            }
        });
        for (int i = 0; i < AuctionCategories.values().length; i++) {
            AuctionCategories category = AuctionCategories.values()[i];
            set(new GUIClickableItem(i * 9) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    if (category != getCategory()) changeSelection(() -> GUIAuctionBrowser.this.category = category);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    List<Text> lore = new ArrayList<>(List.of(
                            Text.key("gui_auction.browser.category_subtitle"),
                            Text.literal(" "),
                            Text.key("gui_auction.browser.category_examples")));
                    category.getExamples().forEach(example ->
                            lore.add(Text.of("<key:'gui_auction.browser.category_example_prefix'><7>{}", example)));
                    lore.add(Text.literal(" "));

                    if (category.equals(getCategory())) {
                        lore.add(Text.key("gui_auction.browser.category_browsing"));
                    } else {
                        lore.add(Text.key("gui_auction.browser.category_click"));
                    }

                    return ItemStacks.item(category.getDisplayMaterial(), 1,
                            Text.of("<color:{}>{}", category.getColor(), StringUtility.toNormalCase(category.name())), lore);
                }
            });
        }

    }
}
