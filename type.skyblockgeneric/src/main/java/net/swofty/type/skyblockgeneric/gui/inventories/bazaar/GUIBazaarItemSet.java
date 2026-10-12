package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.bazaar.BazaarItemSet;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.EnchantedComponent;
import net.swofty.type.skyblockgeneric.item.components.ItemModelComponent;
import net.swofty.type.skyblockgeneric.item.components.SkullHeadComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// GUI listing the items within one bazaar item set, arranged for the set's size
public class GUIBazaarItemSet extends HypixelInventoryGUI implements RefreshingGUI {
    private final BazaarCategories category;
    private final BazaarItemSet itemSet;
    private final BazaarSetLayout layout;
    private final int page;

    public GUIBazaarItemSet(BazaarCategories category, BazaarItemSet itemSet) {
        this(category, itemSet, 0);
    }

    public GUIBazaarItemSet(BazaarCategories category, BazaarItemSet itemSet, int page) {
        super(Text.key("gui_bazaar.item_set.title", StringUtility.toNormalCase(category.name()), itemSet.displayName),
                new BazaarSetLayout(itemSet.items.size()).inventoryType());

        this.category = category;
        this.itemSet = itemSet;
        this.layout = new BazaarSetLayout(itemSet.items.size());
        this.page = layout.clampPage(page);

        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getCloseItem(layout.closeSlot()));
        set(GUIClickableItem.getGoBackItem(layout.backSlot(), new GUIBazaar(category)));
        addPageButtons();
        set(new GUIClickableItem(layout.ordersSlot()) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUIBazaarOrders().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.item(Material.BOOK, """
                        <key:gui_bazaar.item_set.manage_orders_button>
                        <key:gui_bazaar.item_set.manage_orders_button.lore.1>
                        <key:gui_bazaar.item_set.manage_orders_button.lore.2>
                        <key:gui_bazaar.item_set.manage_orders_button.lore.3>""");
            }
        });
    }

    private void addPageButtons() {
        if (page > 0) {
            set(pageButton(layout.previousPageSlot(), page - 1, "gui_bazaar.item_set.previous_page"));
        }
        if (page < layout.pageCount() - 1) {
            set(pageButton(layout.nextPageSlot(), page + 1, "gui_bazaar.item_set.next_page"));
        }
    }

    private GUIClickableItem pageButton(int slot, int targetPage, String key) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                new GUIBazaarItemSet(category, itemSet, targetPage).open((SkyBlockPlayer) p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.ARROW, 1, Text.key(key, targetPage + 1, layout.pageCount()), List.of());
            }
        };
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        int[] slots = layout.slots(page);
        int firstIndex = layout.firstIndex(page);
        for (int i = 0; i < slots.length; i++) {
            ItemType itemType = itemSet.items.get(firstIndex + i);
            int slot = slots[i];

            CompletableFuture<Void> future = ((SkyBlockPlayer) e.player()).getBazaarConnector().getItemStatistics(itemType)
                    .thenAccept(stats -> {
                        set(new GUIClickableItem(slot) {
                            @Override
                            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                                SkyBlockPlayer player = (SkyBlockPlayer) p;
                                new GUIBazaarItem(itemType).open(player);
                            }

                            @Override
                            public ItemStack.Builder getItem(HypixelPlayer p) {
                                SkyBlockPlayer player = (SkyBlockPlayer) p;
                                List<Text> lore = new ArrayList<>();
                                lore.add(Text.key("gui_bazaar.item_set.commodity_label", StringUtility.toNormalCase(itemType.rarity.name())));
                                lore.add(Text.literal(" "));

                                if (stats.bestAsk() > 0) {
                                    lore.add(Text.key("gui_bazaar.item_set.buy_price", new DecimalFormat("#,###").format(stats.bestAsk())));
                                    lore.add(Text.key("gui_bazaar.item_set.buy_price_best", StringUtility.shortenNumber(stats.bestAsk())));
                                } else {
                                    lore.add(Text.key("gui_bazaar.item_set.buy_price_none"));
                                    lore.add(Text.key("gui_bazaar.item_set.buy_price_none_desc"));
                                }

                                lore.add(Text.literal(" "));

                                if (stats.bestBid() > 0) {
                                    lore.add(Text.key("gui_bazaar.item_set.sell_price", new DecimalFormat("#,###").format(stats.bestBid())));
                                    lore.add(Text.key("gui_bazaar.item_set.sell_price_best", StringUtility.shortenNumber(stats.bestBid())));
                                } else {
                                    lore.add(Text.key("gui_bazaar.item_set.sell_price_none"));
                                    lore.add(Text.key("gui_bazaar.item_set.sell_price_none_desc"));
                                }

                                lore.add(Text.literal(" "));
                                lore.add(Text.key("gui_bazaar.item_set.click_to_view"));

                                SkyBlockItem item = new SkyBlockItem(itemType);

                                return ItemStacks.lore(getFromSkyBlockItem(item), lore);
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        // Handle errors gracefully
                        set(new GUIClickableItem(slot) {
                            @Override
                            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                                SkyBlockPlayer player = (SkyBlockPlayer) p;
                                new GUIBazaarItem(itemType).open(player);
                            }

                            @Override
                            public ItemStack.Builder getItem(HypixelPlayer p) {
                                SkyBlockPlayer player = (SkyBlockPlayer) p;
                                List<Text> lore = new ArrayList<>();
                                lore.add(Text.key("gui_bazaar.item_set.commodity_label", StringUtility.toNormalCase(itemType.rarity.name())));
                                lore.add(Text.literal(" "));
                                lore.add(Text.key("gui_bazaar.item_set.error_loading"));
                                lore.add(Text.key("gui_bazaar.item_set.error_try_again"));
                                lore.add(Text.literal(" "));
                                lore.add(Text.key("gui_bazaar.item_set.click_to_view"));

                                ItemStack.Builder errorBuilder = ItemStacks.item(itemType.material, 1,
                                        Text.of("<color:{}>{}", itemType.rarity.getColor(), itemType.getDisplayName()),
                                        lore);
                                SkyBlockItem errorItem = new SkyBlockItem(itemType);
                                if (errorItem.hasComponent(ItemModelComponent.class)) {
                                    errorBuilder = errorBuilder.set(DataComponents.ITEM_MODEL,
                                            errorItem.getComponent(ItemModelComponent.class).getItemModel());
                                }
                                return errorBuilder;
                            }
                        });
                        return null;
                    });

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> updateItemStacks(getInventory(), getPlayer()));
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
        SkyBlockItem clickedItem = new SkyBlockItem(e.getClickedItem());
        ItemType type = clickedItem.getAttributeHandler().getPotentialType();
        e.setCancelled(true);

        if (clickedItem.isNA()) {
            return;
        }

        if (type == null) {
            return;
        }

        Map.Entry<BazaarCategories, BazaarItemSet> entry = BazaarCategories.getFromItem(type);

        if (entry == null) {
            return;
        }

        Thread.startVirtualThread(() -> {
            new GUIBazaarItemSet(entry.getKey(), entry.getValue()).open((SkyBlockPlayer) e.getPlayer());
        });
    }

    @Override
    public void refreshItems(HypixelPlayer p) {
        if (!(p instanceof SkyBlockPlayer player)) {
            return;
        }
        player.getBazaarConnector().isOnline().thenAccept(online -> {
            if (!online) {
                player.sendMessage(Text.key("gui_bazaar.item_set.offline_message"));
                player.closeInventory();
            }
        });
    }

    @Override
    public int refreshRate() {
        return 10;
    }

    private ItemStack.Builder getFromSkyBlockItem(SkyBlockItem item) {
        ItemStack.Builder builder;
        Text name = Text.literal(item.getDisplayName());

        if (item.hasComponent(SkullHeadComponent.class)) {
            builder = ItemStacks.head(item.getComponent(SkullHeadComponent.class).getSkullTexture(item),
                    item.getAmount(), name, List.of());
        } else {
            builder = ItemStacks.item(item.getMaterial(), item.getAmount(), name, List.of());
        }

        // ItemStacks.item()/.head() only sets the base material/skull texture - a custom
        // ITEM_MODEL (used by every non-skull custom-icon item) is otherwise silently ignored,
        // leaving the bazaar listing showing the plain vanilla material instead of its real icon.
        if (item.hasComponent(ItemModelComponent.class)) {
            builder = builder.set(DataComponents.ITEM_MODEL, item.getComponent(ItemModelComponent.class).getItemModel());
        }

        if (item.hasComponent(EnchantedComponent.class)) return ItemStacks.enchanted(builder);
        else return builder;
    }
}
