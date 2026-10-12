package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import lombok.Getter;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.bazaar.BazaarConnector;
import net.swofty.type.skyblockgeneric.bazaar.BazaarItemSet;
import net.swofty.type.skyblockgeneric.bazaar.BazaarNames;
import net.swofty.type.skyblockgeneric.bazaar.BazaarTile;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
// A category of the Bazaar: the category tabs on the left, the tiles of its groups and the shared button row
public class GUIBazaar extends HypixelInventoryGUI implements RefreshingGUI {
    private static final long STATE_REFRESH_MS = 5_000L;

    private final BazaarCategories category;
    private final List<BazaarTile> tiles;
    private final BazaarMenuState state = new BazaarMenuState();
    private final Map<BazaarTile, BazaarConnector.BazaarStatistics> prices = new ConcurrentHashMap<>();
    private long lastStateRefresh;

    public GUIBazaar(BazaarCategories category) {
        super(Text.key("gui_bazaar.main.title", category.displayName()), InventoryType.CHEST_6_ROW);
        this.category = category;
        this.tiles = category.tiles();

        for (int slot : BazaarGridLayout.PANE_SLOTS) {
            set(BazaarMenuButtons.pane(slot, category.getGlassItem()));
        }
        int[] slots = BazaarGridLayout.assign(tiles);
        for (int slot : BazaarGridLayout.unusedTileSlots(slots)) {
            set(BazaarMenuButtons.empty(slot));
        }
        renderCategoryTabs();
        for (int i = 0; i < tiles.size(); i++) {
            set(tileItem(slots[i], tiles.get(i)));
        }
        renderButtonRow();
    }

    private void renderButtonRow() {
        set(BazaarMenuButtons.search(BazaarGridLayout.SEARCH_SLOT, this::search));
        set(BazaarMenuButtons.sellInventory(BazaarGridLayout.SELL_INVENTORY_SLOT, state));
        set(BazaarMenuButtons.sellSacks(BazaarGridLayout.SELL_SACKS_SLOT, state));
        set(GUIClickableItem.getCloseItem(BazaarGridLayout.CLOSE_SLOT));
        set(BazaarMenuButtons.manageOrders(BazaarGridLayout.ORDERS_SLOT, state));
        set(BazaarMenuButtons.history(BazaarGridLayout.HISTORY_SLOT));
        set(BazaarMenuButtons.settings(BazaarGridLayout.SETTINGS_SLOT));
    }

    private HypixelInventoryGUI search(String query, HypixelPlayer player) {
        String needle = query.toLowerCase(Locale.ROOT);
        List<ItemType> found = new ArrayList<>();
        for (BazaarCategories each : BazaarCategories.values()) {
            for (BazaarItemSet set : each.getItems()) {
                for (ItemType type : set.items) {
                    if (BazaarNames.of(type).toLowerCase(Locale.ROOT).contains(needle) && !found.contains(type)) {
                        found.add(type);
                    }
                }
            }
        }
        if (found.isEmpty()) {
            player.sendMessage(Text.key("gui_bazaar.search.no_results", query));
            return this;
        }
        BazaarItemSet results = new BazaarItemSet(found.getFirst(), "Search", found.toArray(new ItemType[0]));
        return new GUIBazaarItemSet(category, results, 0, "Bazaar", "Bazaar", () -> new GUIBazaar(category));
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (player.isIronman()) {
            player.openView(new GUISpecialBazaar());
            return;
        }
        refreshLiveData(player, true);
    }

    private void renderCategoryTabs() {
        int index = 0;
        for (BazaarCategories each : BazaarCategories.values()) {
            int slot = BazaarGridLayout.TAB_SLOTS[index++];
            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    new GUIBazaar(each).open(p);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    boolean viewing = category == each;
                    var builder = ItemStacks.item(
                            each.getDisplayItem(), 1,
                            Text.parse(each.getColor() + each.displayName()),
                            List.of(
                                    Text.key("gui_bazaar.main.category_subtitle"),
                                    Text.literal(" "),
                                    viewing ? Text.key("gui_bazaar.main.category_viewing") : Text.key("gui_bazaar.main.category_click")
                            )
                    );
                    return viewing ? ItemStacks.enchanted(builder) : builder;
                }
            });
        }
    }

    private GUIClickableItem tileItem(int slot, BazaarTile tile) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                if (tile.nested()) {
                    new GUIBazaarNestedGroup(category, tile.name(), tile.sets()).open(p);
                } else if (tile.isSingleProduct()) {
                    new GUIBazaarItem(tile.first().items.getFirst()).open((SkyBlockPlayer) p);
                } else {
                    new GUIBazaarItemSet(category, tile.first()).open(p);
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                BazaarItemSet icon = tile.first();
                List<Text> lore;
                if (tile.isSingleProduct()) {
                    BazaarConnector.BazaarStatistics stats = prices.get(tile);
                    lore = BazaarTooltips.priceLore(stats == null ? null : stats.bestAsk(), stats == null ? null : stats.bestBid());
                } else {
                    lore = BazaarTooltips.groupLore(tile);
                }
                return BazaarIcons.of(icon.displayMaterial, Text.parse(category.getColor() + tile.name()), lore, false);
            }
        };
    }

    private void refreshLiveData(SkyBlockPlayer player, boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - lastStateRefresh < STATE_REFRESH_MS) {
            return;
        }
        lastStateRefresh = now;
        state.refresh(player);
        for (BazaarTile tile : tiles) {
            if (!tile.isSingleProduct()) {
                continue;
            }
            player.getBazaarConnector().getItemStatistics(tile.first().items.getFirst())
                    .thenAccept(stats -> prices.put(tile, stats))
                    .exceptionally(throwable -> {
                        Logger.error(throwable, "Failed to fetch bazaar data for {}", tile.name());
                        return null;
                    });
        }
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }

    @Override
    public int refreshRate() {
        return 10;
    }

    @Override
    public void refreshItems(HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        player.getBazaarConnector().isOnline().thenAccept(online -> {
            if (!online) {
                player.sendMessage(Text.key("gui_bazaar.main.offline_message"));
                player.closeInventory();
            } else {
                player.getBazaarConnector().processAllPendingTransactions();
            }
        });
        refreshLiveData(player, false);
    }
}
