package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.bazaar.BazaarConnector;
import net.swofty.type.skyblockgeneric.bazaar.BazaarItemSet;
import net.swofty.type.skyblockgeneric.bazaar.BazaarProduct;
import net.swofty.type.skyblockgeneric.bazaar.BazaarProductGroup;
import net.swofty.type.skyblockgeneric.bazaar.BazaarProducts;
import net.swofty.type.skyblockgeneric.bazaar.EnchantmentProduct;
import net.swofty.type.skyblockgeneric.gui.inventories.bazaar.selections.GUIBazaarOrderAmountSelection;
import net.swofty.type.skyblockgeneric.gui.inventories.bazaar.selections.GUIBazaarPriceSelection;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
                return ItemStacks.item(Material.BOOK, """
                        <key:gui_bazaar.item.manage_orders_button>
                        <key:gui_bazaar.item.manage_orders_button.lore.1>
                        <key:gui_bazaar.item.manage_orders_button.lore.2>""");
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
        player.getBazaarConnector().getItemStatistics(product)
                .thenAccept(stats -> {
                    this.currentStats = stats;
                    updateItems(stats);
                });
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
                lore.add(Text.key("gui_bazaar.item.buy_instantly_subtitle"));
                lore.add(Text.literal(" "));

                if (stats.bestAsk() <= 0) {
                    lore.add(Text.key("gui_bazaar.item.buy_no_offers"));
                } else {
                    double priceWithFee = stats.bestAsk() * 1.04;
                    lore.add(Text.key("gui_bazaar.item.buy_price", FORMATTER.format(priceWithFee)));
                    lore.add(Text.key("gui_bazaar.item.buy_max_space", product.room(player)));
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_bazaar.item.buy_click"));
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
                lore.add(Text.key("gui_bazaar.item.sell_instantly_subtitle"));
                lore.add(Text.literal(" "));

                int have = product.held(player);
                if (have <= 0) {
                    lore.add(Text.key("gui_bazaar.item.sell_have_zero"));
                } else if (stats.bestBid() <= 0) {
                    lore.add(Text.key("gui_bazaar.item.sell_have", have));
                    lore.add(Text.key("gui_bazaar.item.sell_no_orders"));
                } else {
                    lore.add(Text.key("gui_bazaar.item.sell_have", have));
                    lore.add(Text.key("gui_bazaar.item.sell_price", FORMATTER.format(stats.bestBid())));
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_bazaar.item.sell_click"));
                }

                return ItemStacks.item(Material.HOPPER, 1, Text.key("gui_bazaar.item.sell_instantly"), lore);
            }
        });

        set(new GUIClickableItem(15) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;

                new GUIBazaarOrderAmountSelection(GUIBazaarItem.this, product, true, false, 71680, player.getCoins())
                        .openAmountSelection(player)
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
                lore.add(Text.key("gui_bazaar.item.create_buy_order_subtitle"));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.item.create_buy_order_max"));
                if (stats.bestAsk() > 0) {
                    lore.add(Text.key("gui_bazaar.item.create_buy_order_best_ask", FORMATTER.format(stats.bestAsk())));
                }
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

                new GUIBazaarOrderAmountSelection(GUIBazaarItem.this, product, false, false, have, 0)
                        .openAmountSelection(player)
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
                lore.add(Text.key("gui_bazaar.item.create_sell_order_subtitle"));
                lore.add(Text.literal(" "));
                int have = product.held(player);
                lore.add(Text.key("gui_bazaar.item.create_sell_order_have", have));
                if (stats.bestBid() > 0) {
                    lore.add(Text.key("gui_bazaar.item.create_sell_order_best_bid", FORMATTER.format(stats.bestBid())));
                }
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.item.create_sell_order_click"));

                return ItemStacks.item(Material.MAP, 1, Text.key("gui_bazaar.item.create_sell_order"), lore);
            }
        });

        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public void refreshItems(HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        if (currentStats != null) {
            player.getBazaarConnector().getItemStatistics(product)
                    .thenAccept(stats -> {
                        this.currentStats = stats;
                        updateItems(stats);
                    });
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
}
