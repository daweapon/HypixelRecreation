package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.gui.inventory.item.GUIQueryItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.bazaar.BazaarHistory;
import net.swofty.type.skyblockgeneric.bazaar.BazaarSales;
import net.swofty.type.skyblockgeneric.bazaar.BazaarSettings;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Supplier;

final class BazaarMenuButtons {
    private BazaarMenuButtons() {
    }

    static GUIItem empty(int slot) {
        return new GUIItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStack.builder(Material.AIR);
            }
        };
    }

    static GUIItem pane(int slot, Material material) {
        return new GUIItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.named(material, "");
            }
        };
    }

    static GUIClickableItem back(int slot, String label, Supplier<HypixelInventoryGUI> target) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent event, HypixelPlayer player) {
                target.get().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ARROW, 1, Text.key("gui_bazaar.buttons.go_back"),
                        List.of(Text.key("gui_bazaar.buttons.go_back_to", label)));
            }
        };
    }

    static GUIQueryItem search(int slot, BiFunction<String, HypixelPlayer, HypixelInventoryGUI> onQuery) {
        return new GUIQueryItem(slot) {
            @Override
            public HypixelInventoryGUI onQueryFinish(String query, HypixelPlayer player) {
                if (query == null || query.isBlank()) {
                    return null;
                }
                return onQuery.apply(query.trim(), player);
            }

            @Override
            public String[] lines() {
                return new String[]{"Enter your", "search"};
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.OAK_SIGN, 1, Text.key("gui_bazaar.buttons.search"),
                        Text.keyLines("gui_bazaar.buttons.search.lore"));
            }
        };
    }

    static GUIClickableItem sellInventory(int slot, BazaarMenuState state) {
        return sell(slot, state, BazaarSales.Source.INVENTORY, Material.CHEST, "gui_bazaar.buttons.sell_inventory");
    }

    static GUIClickableItem sellSacks(int slot, BazaarMenuState state) {
        return sell(slot, state, BazaarSales.Source.SACKS, Material.CAULDRON, "gui_bazaar.buttons.sell_sacks");
    }

    private static GUIClickableItem sell(int slot, BazaarMenuState state, BazaarSales.Source source, Material material, String key) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent event, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                BazaarSales.Summary summary = source == BazaarSales.Source.INVENTORY ? state.inventory() : state.sacks();
                if (summary == null || summary.sellable().isEmpty()) {
                    player.sendMessage(Text.key("gui_bazaar.sale.nothing"));
                    return;
                }
                BazaarSales.sell(player, source).thenAccept(sold -> {
                    player.sendMessage(sold.products() == 0
                            ? Text.key("gui_bazaar.sale.nothing")
                            : Text.key("gui_bazaar.sale.sold", sold.items(), BazaarTooltips.coins(sold.coins())));
                    state.refresh(player);
                });
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                BazaarSales.Summary summary = source == BazaarSales.Source.INVENTORY ? state.inventory() : state.sacks();
                List<Text> lore = new ArrayList<>(Text.keyLines(key + ".lore"));
                lore.add(Text.literal(" "));
                if (summary == null) {
                    lore.add(Text.key("gui_bazaar.sale.loading"));
                } else if (summary.sellable().isEmpty()) {
                    lore.add(Text.key("gui_bazaar.sale.empty"));
                } else {
                    for (BazaarSales.Line line : summary.listed()) {
                        lore.add(Text.of("<green>{}x <color:{}>{} <gray>for <gold>{} coins",
                                String.format("%,d", line.amount()), line.product().rarity().getColor(),
                                line.product().name(), String.format("%,d", Math.round(line.coins()))));
                    }
                    if (summary.hasOthers()) {
                        lore.add(Text.of("<yellow>{}x <white>Other items <gray>for <gold>{} coins",
                                String.format("%,d", summary.otherAmount()), String.format("%,d", Math.round(summary.otherCoins()))));
                    }
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_bazaar.sale.earn", String.format("%,d", Math.round(summary.total()))));
                    if (summary.hasUnsellable()) {
                        lore.add(Text.key("gui_bazaar.sale.unsellable"));
                        lore.add(Text.key("gui_bazaar.sale.unsellable_reason"));
                    }
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_bazaar.sale.click"));
                }
                return ItemStacks.item(material, 1, Text.key(key), lore);
            }
        };
    }

    static GUIClickableItem manageOrders(int slot, BazaarMenuState state) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent event, HypixelPlayer player) {
                new GUIBazaarOrders().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                int orders = state.orders();
                Text status = orders == 0
                        ? Text.key("gui_bazaar.buttons.manage_orders.none")
                        : orders == 1
                        ? Text.key("gui_bazaar.buttons.manage_orders.one")
                        : Text.key("gui_bazaar.buttons.manage_orders.many", orders);
                return ItemStacks.item(Material.BOOK, 1, Text.key("gui_bazaar.buttons.manage_orders"),
                        List.of(status, Text.literal(" "), Text.key("gui_bazaar.buttons.manage_orders.click")));
            }
        };
    }

    static GUIItem history(int slot) {
        return new GUIItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<Text> lore = new ArrayList<>();
                List<BazaarHistory.Entry> entries = BazaarHistory.entries(player.getProfiles().getCurrentlySelected());
                if (entries.isEmpty()) {
                    lore.add(Text.key("gui_bazaar.buttons.history.empty"));
                }
                long now = System.currentTimeMillis();
                for (int i = 0; i < entries.size(); i++) {
                    BazaarHistory.Entry entry = entries.get(i);
                    if (i > 0) {
                        lore.add(Text.literal(" "));
                    }
                    lore.add(Text.of("<gray>{} ago <color:{}>{}<gray> of <green>{}x<gray> {}",
                            BazaarHistory.ago(now - entry.at()), entry.kind().colour(), entry.kind().label(),
                            String.format("%,d", entry.amount()), entry.item().name()));
                    lore.add(Text.of(" <gray>for <gold>{} coins<gray> by ", shortCoins(entry.coins())).append(entry.player()));
                }
                return ItemStacks.item(Material.MAP, 1, Text.key("gui_bazaar.buttons.history"), lore);
            }
        };
    }

    static String shortCoins(double coins) {
        if (coins >= 1_000_000) {
            return String.format(Locale.US, "%.1fM", coins / 1_000_000);
        }
        if (coins >= 1_000) {
            return String.format(Locale.US, "%.1fk", coins / 1_000);
        }
        return String.format(Locale.US, "%.1f", coins);
    }

    static GUIClickableItem settings(int slot) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent event, HypixelPlayer player) {
                new GUIBazaarSettings().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.REDSTONE_TORCH, 1, Text.key("gui_bazaar.buttons.settings"),
                        Text.keyLines("gui_bazaar.buttons.settings.lore"));
            }
        };
    }

    static GUIClickableItem viewMode(int slot) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent event, HypixelPlayer player) {
                BazaarSettings.toggleDirectMode(player.getUuid());
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return GUIBazaarSettings.viewModeIcon(player);
            }
        };
    }
}
