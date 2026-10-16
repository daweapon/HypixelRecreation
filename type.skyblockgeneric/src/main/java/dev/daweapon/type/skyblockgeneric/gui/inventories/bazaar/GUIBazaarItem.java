package dev.daweapon.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.RefreshingGUI;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.gui.inventory.item.GUIItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarCategories;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarConnector;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarItemSet;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarOrderBook;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarSettings;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarProduct;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarProductGroup;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarProducts;
import dev.daweapon.type.skyblockgeneric.bazaar.EnchantmentProduct;
import dev.daweapon.type.skyblockgeneric.gui.inventories.bazaar.selections.GUIBazaarOrderAmountSelection;
import dev.daweapon.type.skyblockgeneric.gui.inventories.bazaar.selections.GUIBazaarPriceSelection;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

// GUI for a single bazaar item: shows live buy/sell stats and links to order creation
public class GUIBazaarItem extends HypixelInventoryGUI implements RefreshingGUI {
    private final BazaarProduct product;
    private final boolean specialBazaar;
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");
    private BazaarConnector.BazaarStatistics currentStats;
    private BazaarConnector.BazaarItemData currentData;
    private int ownOrders;

    public GUIBazaarItem(ItemType itemType) {
        this(BazaarProducts.of(itemType), false);
    }

    public GUIBazaarItem(ItemType itemType, boolean specialBazaar) {
        this(BazaarProducts.of(itemType), specialBazaar);
    }

    public GUIBazaarItem(BazaarProduct product) {
        this(product, false);
    }

    public GUIBazaarItem(BazaarProduct product, boolean specialBazaar) {
        super(Text.key("gui_bazaar.item.title", groupName(product), product.name()), InventoryType.CHEST_4_ROW);
        this.product = product;
        this.specialBazaar = specialBazaar;

        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));

        Map.Entry<BazaarCategories, BazaarItemSet> bazaarCategory = BazaarCategories.getFromProduct(product);
        if (bazaarCategory != null) {
            BazaarCategories parentCategory = bazaarCategory.getKey();
            BazaarItemSet parentSet = bazaarCategory.getValue();
            if (product instanceof EnchantmentProduct enchantment) {
                setEnchantmentBack(parentCategory, parentSet, enchantment);
            } else {
                set(BazaarMenuButtons.back(30, parentSet.isSingleProduct() ? backLabel(parentSet) : parentSet.name(),
                        () -> parentSet.isSingleProduct()
                                ? GUIBazaarItemSet.parentOf(parentCategory, parentSet)
                                : new GUIBazaarItemSet(parentCategory, parentSet)));
            }
        }

        if (!specialBazaar) set(new GUIClickableItem(32) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                new GUIBazaarOrders().open(p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key(ownOrders == 0 ? "gui_bazaar.item.manage_orders_none" : "gui_bazaar.item.manage_orders_some", ownOrders));
                lore.add(Text.key(ownOrders == 0 ? "gui_bazaar.item.manage_orders_none.2" : "gui_bazaar.item.manage_orders_some.2"));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.item.manage_orders_click"));
                return ItemStacks.item(Material.BOOK, 1, Text.key("gui_bazaar.item.manage_orders_button"), lore);
            }
        });

        if (!specialBazaar) set(new GUIClickableItem(33) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                p.sendMessage(Text.key("gui_bazaar.item.view_graphs.message"));
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.PAPER, 1, Text.key("gui_bazaar.item.view_graphs"), List.of(
                        Text.key("gui_bazaar.item.view_graphs.mode"),
                        Text.literal(" "),
                        Text.key("gui_bazaar.item.view_graphs.lore.1"),
                        Text.key("gui_bazaar.item.view_graphs.lore.2"),
                        Text.literal(" "),
                        Text.key("gui_bazaar.item.view_graphs.unavailable")));
            }
        });

        ItemType ignorable = product.itemType();
        if (!specialBazaar && ignorable != null) set(new GUIClickableItem(34) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                BazaarSettings.toggleIgnored(p.getUuid(), ignorable);
                updateItemStacks(getInventory(), getPlayer());
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                boolean ignored = BazaarSettings.isIgnored(p.getUuid(), ignorable);
                return ItemStacks.item(product.material(), 1, Text.key("gui_bazaar.item.instasell_ignore"), List.of(
                        Text.key("gui_bazaar.item.instasell_ignore.lore.1"),
                        Text.key("gui_bazaar.item.instasell_ignore.lore.2"),
                        Text.key("gui_bazaar.item.instasell_ignore.lore.3"),
                        Text.literal(" "),
                        Text.key(ignored ? "gui_bazaar.item.instasell_ignore.ignored" : "gui_bazaar.item.instasell_ignore.allowed"),
                        Text.literal(" "),
                        Text.key("gui_bazaar.item.instasell_ignore.click")));
            }
        });

        if (!specialBazaar) set(new GUIClickableItem(31) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                var entry = BazaarCategories.getFromProduct(product);
                new GUIBazaar(entry == null ? BazaarCategories.FARMING : entry.getKey()).open(p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.head("c232e3820897429157619b0ee099fec0628f602fff12b695de54aef11d923ad7", """
                        <key:gui_bazaar.item.go_back_bazaar>
                        <key:gui_bazaar.item.go_back_bazaar.lore>""");
            }
        });

        set(new GUIItem(13) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockItem item = product.item();
                if (item != null) {
                    return new NonPlayerItemUpdater(item).getUpdatedItem();
                }
                return BazaarIcons.of(product, Text.of("<color:{}>{}", product.rarity().getColor(), product.name()),
                        List.of(Text.key("gui_bazaar.item.essence_balance", String.format("%,d", product.held((SkyBlockPlayer) p)))), false);
            }
        });
    }

    private static List<Text> bookLines(List<BazaarConnector.MarketOrder> orders, boolean highestFirst, String headerKey) {
        List<BazaarOrderBook.Level> levels = BazaarOrderBook.top(orders, highestFirst);
        List<Text> lines = new ArrayList<>();
        if (levels.isEmpty()) {
            lines.add(Text.key(headerKey));
            lines.add(Text.key("gui_bazaar.item.book_empty"));
            return lines;
        }
        boolean single = levels.size() == 1 && levels.get(0).orders() == 1;
        lines.add(Text.key(single && highestFirst ? "gui_bazaar.item.only_order" : headerKey));
        for (BazaarOrderBook.Level level : levels) {
            lines.add(Text.key("gui_bazaar.item.book_line", BazaarTooltips.coins(level.price()),
                    String.format("%,d", level.amount()), level.orders()));
        }
        return lines;
    }

    private void setEnchantmentBack(BazaarCategories category, BazaarItemSet set, EnchantmentProduct enchantment) {
        BazaarProductGroup group = BazaarProducts.groupOf(enchantment);
        int index = Math.max(0, set.groups().indexOf(group));
        int page = index / BazaarSetLayout.PAGE_CAPACITY;
        Supplier<HypixelInventoryGUI> list = () -> new GUIBazaarEnchantments(category, set, page);
        if (group == null || group.isSingle()) {
            set(BazaarMenuButtons.back(30, set.name(), list));
            return;
        }
        set(BazaarMenuButtons.back(30, group.label(), () -> new GUIBazaarItemSet(category,
                BazaarItemSet.ofProducts(group.first(), group.label(), null, group.products()), 0, set.name(), set.name(), list)));
    }

    private static String groupName(BazaarProduct product) {
        var entry = BazaarCategories.getFromProduct(product);
        return entry == null ? "Bazaar" : entry.getValue().name();
    }

    private static String backLabel(BazaarItemSet set) {
        return set.nestedGroup == null ? "Bazaar" : set.nestedGroup;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (player.isIronman() && product.itemType() != ItemType.BOOSTER_COOKIE) {
            player.openView(new GUISpecialBazaar());
            return;
        }
        loadAndUpdate(player);
    }

    private void loadAndUpdate(SkyBlockPlayer player) {
        player.getBazaarConnector().getPendingOrders().thenCombine(player.getBazaarConnector().getItemView(product), (pending, view) -> {
            this.ownOrders = (int) pending.stream().filter(order -> product.id().equals(order.itemName())).count();
            this.currentData = view.data();
            this.currentStats = view.statistics();
            return view.statistics();
        }).thenAccept(this::updateItems);
    }

    private void updateItems(BazaarConnector.BazaarStatistics stats) {
        set(new GUIClickableItem(10) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                if (stats.bestAsk() <= 0) {
                    p.sendMessage(Text.key("gui_bazaar.item.buy_no_offers_message"));
                    return;
                }

                int maxSpace = product.room(player);
                if (maxSpace <= 0) {
                    p.sendMessage(Text.key("gui_bazaar.item.buy_inventory_full"));
                    return;
                }

                double priceWithFee = stats.bestAsk() * 1.04;

                new GUIBazaarOrderAmountSelection(GUIBazaarItem.this, product, true, true, maxSpace, priceWithFee)
                        .openAmountSelection(player)
                        .thenAccept(amount -> {
                            if (amount <= 0) return;

                            double totalCost = priceWithFee * amount;
                            if (totalCost > player.getCoins()) {
                                p.sendMessage(Text.key("gui_bazaar.item.buy_need_coins", FORMATTER.format(totalCost)));
                                return;
                            }

                            player.getBazaarConnector().instantBuy(product, amount)
                                    .thenAccept(result -> {
                                        player.sendMessage(Text.key("gui_bazaar.item.bazaar_result_prefix")
                                                .append(result.success() ? "<a> {}" : "<c> {}", result.message()));
                                        if (result.success()) p.closeInventory();
                                    });
                        });
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_bazaar.item.product_line", product.name()));
                lore.add(Text.literal(" "));

                if (stats.bestAsk() <= 0) {
                    lore.add(Text.key("gui_bazaar.item.buy_no_offers"));
                } else {
                    double unit = stats.bestAsk() * 1.04;
                    int stack = product.material().maxStackSize();
                    lore.add(Text.key("gui_bazaar.item.price_per_unit", BazaarTooltips.coins(unit)));
                    lore.add(stack > 1
                            ? Text.key("gui_bazaar.item.stack_price", BazaarTooltips.coins(unit * stack))
                            : Text.key("gui_bazaar.item.stack_price_na"));
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_bazaar.item.pick_amount"));
                }

                return ItemStacks.item(Material.GOLDEN_HORSE_ARMOR, 1, Text.key("gui_bazaar.item.buy_instantly"), lore);
            }
        });

        if (specialBazaar) {
            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        set(new GUIClickableItem(11) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                int have = product.held(player);
                if (have <= 0) {
                    p.sendMessage(Text.key("gui_bazaar.item.sell_no_items", product.name()));
                    return;
                }

                if (stats.bestBid() <= 0) {
                    p.sendMessage(Text.key("gui_bazaar.item.sell_no_orders_message"));
                    return;
                }

                new GUIBazaarOrderAmountSelection(GUIBazaarItem.this, product, false, true, have, stats.bestBid())
                        .openAmountSelection(player)
                        .thenAccept(amount -> {
                            if (amount <= 0) return;

                            player.getBazaarConnector().instantSell(product)
                                    .thenAccept(result -> {
                                        p.sendMessage(Text.key("gui_bazaar.item.bazaar_result_prefix")
                                                .append(result.success() ? "<a> {}" : "<c> {}", result.message()));
                                        if (result.success()) p.closeInventory();
                                    });
                        });
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_bazaar.item.product_line", product.name()));
                lore.add(Text.literal(" "));

                int have = product.held(player);
                lore.add(have <= 0
                        ? Text.key("gui_bazaar.item.sell_inventory_none")
                        : Text.key("gui_bazaar.item.sell_inventory", String.format("%,d", have)));
                lore.add(stats.bestBid() <= 0
                        ? Text.key("gui_bazaar.item.sell_no_orders")
                        : Text.key("gui_bazaar.item.price_per_unit", BazaarTooltips.coins(stats.bestBid())));
                lore.add(Text.literal(" "));
                lore.add(have <= 0
                        ? Text.key("gui_bazaar.item.sell_none_to_sell")
                        : Text.key("gui_bazaar.item.pick_amount"));

                return ItemStacks.item(Material.HOPPER, 1, Text.key("gui_bazaar.item.sell_instantly"), lore);
            }
        });

        set(new GUIClickableItem(15) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;

                orderSlotFree(player).thenCompose(free -> !free ? java.util.concurrent.CompletableFuture.completedFuture(0)
                        : new GUIBazaarOrderAmountSelection(GUIBazaarItem.this, product, true, false, 71680, player.getCoins())
                        .openAmountSelection(player))
                        .thenAccept(amount -> {
                            if (amount <= 0) return;

                            new GUIBazaarPriceSelection(
                                    GUIBazaarItem.this, amount, stats.bestAsk(), stats.worstAsk(), product, false
                            ).openPriceSelection(player).thenAccept(price -> {
                                if (price <= 0) return;

                                double totalCost = price * amount;
                                if (totalCost > player.getCoins()) {
                                    p.sendMessage(Text.key("gui_bazaar.item.buy_order_need_coins", FORMATTER.format(totalCost)));
                                    new GUIBazaarItem(product).open(p);
                                    return;
                                }

                                player.removeCoins(totalCost);
                                p.sendMessage(Text.key("gui_bazaar.item.buy_order_escrow", FORMATTER.format(totalCost)));

                                player.getBazaarConnector().createBuyOrder(product, price, amount)
                                        .thenAccept(result -> {
                                            if (result.success()) {
                                                p.sendMessage(Text.key("gui_bazaar.item.buy_order_created", amount, FORMATTER.format(price)));
                                                p.closeInventory();
                                            } else {
                                                player.addCoins(totalCost);
                                                p.sendMessage(Text.key("gui_bazaar.item.buy_order_failed", FORMATTER.format(totalCost)));
                                                new GUIBazaarItem(product).open(p);
                                            }
                                        });
                            });
                        });
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_bazaar.item.create_buy_order_map"));
                lore.add(Text.key("gui_bazaar.item.product_line", product.name()));
                lore.add(Text.literal(" "));
                lore.addAll(bookLines(currentData == null ? List.of() : currentData.buyOrders(), true, "gui_bazaar.item.top_orders"));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.item.create_buy_order_click"));

                return ItemStacks.item(Material.FILLED_MAP, 1, Text.key("gui_bazaar.item.create_buy_order"), lore);
            }
        });

        set(new GUIClickableItem(16) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                int have = product.held(player);
                if (have <= 0) {
                    p.sendMessage(Text.key("gui_bazaar.item.sell_order_no_items", product.name()));
                    return;
                }

                orderSlotFree(player).thenCompose(free -> !free ? java.util.concurrent.CompletableFuture.completedFuture(0)
                        : new GUIBazaarOrderAmountSelection(GUIBazaarItem.this, product, false, false, have, 0)
                        .openAmountSelection(player))
                        .thenAccept(amount -> {
                            if (amount <= 0) return;

                            new GUIBazaarPriceSelection(
                                    GUIBazaarItem.this, amount, stats.bestBid(), stats.worstBid(), product, true
                            ).openPriceSelection(player).thenAccept(price -> {
                                if (price <= 0) return;

                                Runnable undo = product.take(player, amount);
                                if (undo == null) {
                                    p.sendMessage(Text.key("gui_bazaar.item.sell_order_remove_fail"));
                                    new GUIBazaarItem(product).open(p);
                                    return;
                                }

                                p.sendMessage(Text.key("gui_bazaar.item.sell_order_escrow", amount, product.name()));

                                player.getBazaarConnector().createSellOrder(product, price, amount)
                                        .thenAccept(result -> {
                                            if (result.success()) {
                                                p.sendMessage(Text.key("gui_bazaar.item.sell_order_created", amount, FORMATTER.format(price)));
                                                p.closeInventory();
                                            } else {
                                                undo.run();
                                                p.sendMessage(Text.key("gui_bazaar.item.sell_order_failed"));
                                                new GUIBazaarItem(product).open(p);
                                            }
                                        });
                            });
                        });
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_bazaar.item.product_line", product.name()));
                lore.add(Text.literal(" "));
                lore.addAll(bookLines(currentData == null ? List.of() : currentData.sellOrders(), false, "gui_bazaar.item.top_offers"));
                lore.add(Text.literal(" "));
                int have = product.held(player);
                lore.add(have <= 0
                        ? Text.key("gui_bazaar.item.none_in_inventory")
                        : Text.key("gui_bazaar.item.create_sell_order_click"));

                return ItemStacks.item(Material.MAP, 1, Text.key("gui_bazaar.item.create_sell_order"), lore);
            }
        });

        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public void refreshItems(HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        if (currentStats != null) {
            loadAndUpdate(player);
        }
    }

    @Override
    public int refreshRate() {
        return 10;
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }

    private java.util.concurrent.CompletableFuture<Boolean> orderSlotFree(SkyBlockPlayer player) {
        return player.getBazaarConnector().hasFreeOrderSlot().thenApply(free -> {
            if (!free) player.sendMessage("<gold>[Bazaar] <red>You have reached your maximum number of open orders! Cancel one or upgrade Bazaar Flipper with Elizabeth.");
            return free;
        });
    }
}
