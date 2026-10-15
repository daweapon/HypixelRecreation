package dev.daweapon.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.RefreshingGUI;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarCategories;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarConnector;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarItemSet;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarProduct;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarProductGroup;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarProducts;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarTile;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

// The shared frame of every Bazaar menu below a category: black panes, the button row, paging, and the
// tiles for the entries of one page.
abstract class GUIBazaarMenu extends HypixelInventoryGUI implements RefreshingGUI {
    private static final long STATE_REFRESH_MS = 5_000L;

    protected final BazaarCategories category;
    protected final BazaarSetLayout layout;
    protected final int page;
    protected final BazaarMenuState state = new BazaarMenuState();
    private long lastRefresh;

    GUIBazaarMenu(BazaarCategories category, String parentName, String name, BazaarSetLayout layout, int page,
                  String backLabel, Supplier<HypixelInventoryGUI> back) {
        super(title(parentName, name, layout, layout.clampPage(page)), layout.inventoryType());
        this.category = category;
        this.layout = layout;
        this.page = layout.clampPage(page);

        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(BazaarMenuButtons.sellInventory(layout.sellInventorySlot(), state));
        set(BazaarMenuButtons.back(layout.backSlot(), backLabel, back));
        set(GUIClickableItem.getCloseItem(layout.closeSlot()));
        set(BazaarMenuButtons.manageOrders(layout.ordersSlot(), state));
        set(BazaarMenuButtons.viewMode(layout.settingsSlot()));
        if (this.page > 0) {
            set(pageButton(layout.previousPageSlot(), this.page - 1, "gui_bazaar.buttons.previous_page"));
        }
        if (this.page < layout.pageCount() - 1) {
            set(pageButton(layout.nextPageSlot(), this.page + 1, "gui_bazaar.buttons.next_page"));
        }
    }

    private static Text title(String parentName, String name, BazaarSetLayout layout, int page) {
        if (layout.pageCount() > 1) {
            return Text.key("gui_bazaar.menu.title_paged", page + 1, layout.pageCount(), parentName, name);
        }
        return Text.key("gui_bazaar.menu.title", parentName, name);
    }

    protected abstract HypixelInventoryGUI openPage(int page);

    private GUIClickableItem pageButton(int slot, int target, String key) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                openPage(target).open(p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.ARROW, 1, Text.key(key),
                        List.of(Text.key("gui_bazaar.buttons.page_label", target + 1)));
            }
        };
    }

    protected void setProductTile(int slot, BazaarProduct product, BazaarConnector.BazaarStatistics stats) {
        set(new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                new GUIBazaarItem(product).open((SkyBlockPlayer) p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return BazaarIcons.of(product, BazaarTooltips.productName(product),
                        BazaarTooltips.productLore(product, stats == null ? null : stats.bestAsk(), stats == null ? null : stats.bestBid()),
                        true);
            }
        });
    }

    protected void setProductGroupTile(int slot, BazaarProductGroup group, BazaarConnector.BazaarStatistics stats,
                                       Supplier<HypixelInventoryGUI> opening) {
        set(new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                if (group.isSingle()) {
                    new GUIBazaarItem(group.first()).open((SkyBlockPlayer) p);
                } else {
                    opening.get().open(p);
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = group.isSingle()
                        ? BazaarTooltips.priceLore(stats == null ? null : stats.bestAsk(), stats == null ? null : stats.bestBid())
                        : BazaarTooltips.groupLore(group);
                return BazaarIcons.of(group.first(), Text.parse(category.getColor() + group.label()), lore, false);
            }
        });
    }

    protected void setGroupTile(int slot, BazaarItemSet set, BazaarConnector.BazaarStatistics stats) {
        BazaarTile tile = new BazaarTile(set.name(), List.of(set), false, set.gridPosition);
        set(new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                if (tile.isSingleProduct()) {
                    new GUIBazaarItem(set.products().getFirst()).open((SkyBlockPlayer) p);
                } else {
                    GUIBazaarItemSet.opening(category, set).open(p);
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = tile.isSingleProduct()
                        ? BazaarTooltips.priceLore(stats == null ? null : stats.bestAsk(), stats == null ? null : stats.bestBid())
                        : BazaarTooltips.groupLore(tile);
                return BazaarIcons.of(set.icon(), Text.parse(category.getColor() + set.name()), lore, false);
            }
        });
    }

    protected void refreshState(SkyBlockPlayer player, boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - lastRefresh < STATE_REFRESH_MS) {
            return;
        }
        lastRefresh = now;
        state.refresh(player);
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public int refreshRate() {
        return 10;
    }

    @Override
    public void refreshItems(HypixelPlayer p) {
        if (!(p instanceof SkyBlockPlayer player)) {
            return;
        }
        player.getBazaarConnector().isOnline().thenAccept(online -> {
            if (!online) {
                player.sendMessage(Text.key("gui_bazaar.main.offline_message"));
                player.closeInventory();
            }
        });
        refreshState(player, false);
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        SkyBlockItem clickedItem = new SkyBlockItem(e.getClickedItem());
        e.setCancelled(true);
        if (clickedItem.isNA()) {
            return;
        }
        BazaarProduct product = BazaarProducts.fromStack(e.getClickedItem());
        if (product == null) {
            return;
        }
        Map.Entry<BazaarCategories, BazaarItemSet> entry = BazaarCategories.getFromProduct(product);
        if (entry == null) {
            return;
        }
        Thread.startVirtualThread(() -> {
            if (product.itemType() == null) {
                new GUIBazaarItem(product).open((SkyBlockPlayer) e.getPlayer());
            } else {
                new GUIBazaarItemSet(entry.getKey(), entry.getValue()).open((SkyBlockPlayer) e.getPlayer());
            }
        });
    }
}
