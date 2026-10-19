package dev.daweapon.type.hub.gui.elizabeth;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.gui.inventory.item.GUIItem;
import dev.daweapon.type.skyblockgeneric.firesale.ActiveFireSale;
import dev.daweapon.type.skyblockgeneric.firesale.FireSale;
import dev.daweapon.type.skyblockgeneric.firesale.FireSaleService;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.commons.StringUtility;

import dev.daweapon.type.generic.user.HypixelPlayer;

public class GUIFIRESales extends HypixelInventoryGUI {
    public GUIFIRESales() {
        super("Community Shop", InventoryType.CHEST_6_ROW);
    }

    private final int[] categoriesItemsSlots = {
            10, 11, 12, 13, 14, 16
    };
    private final int[] tabSlots = {
            1, 2, 3, 4, 5, 7
    };

    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));

        GUIAccountAndProfileUpgrades.ShopCategorys[] allShopCategorys = GUIAccountAndProfileUpgrades.ShopCategorys.values();
        int index = 0;
        for (int slot : tabSlots) {
            GUIAccountAndProfileUpgrades.ShopCategorys shopCategorys = allShopCategorys[index];
            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    if (slot != 5) {
                        shopCategorys.gui.open(p);
                    }
                }
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return shopCategorys.buildStack(slot == 5, p);
                }
            });
            index++;
        }

        for (int slot : categoriesItemsSlots) {
            set(new GUIItem(slot) {
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(slot != 14
                            ? Material.GRAY_STAINED_GLASS_PANE
                            : Material.GREEN_STAINED_GLASS_PANE, """
                            <8>▲ <7>Categories
                            <8>▼ <7>Items""");
                }
            });
        }
        FireSaleService.latest().ifPresentOrElse(this::showSale, this::showNoSale);
        set(new GUIClickableItem(49) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                p.sendMessage("<a>Gems are handed out by staff on this server.");
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.enchanted(ItemStacks.item(Material.EMERALD, """
                        <a>Community Shop
                        <8>Elizabeth

                        <7>Gems: <a>{:,}
                        <8>Handed out by staff!

                        <7>Bits: <b>{:,}
                        <8>Earn from Booster Cookies!

                        <7>Fame Rank: <e>
                        <8>Rank up by spending gems & bits!
                        """, player.getGems(), player.getBits()));
            }
        });
        updateItemStacks(getInventory(), getPlayer());
    }
    private void showNoSale() {
        set(new GUIItem(22) {
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.BARRIER, """
                        <c>No Fire Sale
                        <7>There is no Fire Sale running
                        <7>right now. <a>Elizabeth <7>will call you
                        <7>when the next one starts!""");
            }
        });
    }

    private void showSale(ActiveFireSale active) {
        FireSale sale = active.sale();
        int[] slots = saleSlots(sale.items().size());
        boolean over = active.ended(System.currentTimeMillis());
        for (int index = 0; index < slots.length; index++) {
            String itemId = sale.items().get(index);
            set(new GUIClickableItem(slots[index]) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    player.sendMessage(FireSaleService.purchase(player, active, itemId));
                    new GUIFIRESales().open(player);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    ItemStack.Builder stack = new NonPlayerItemUpdater(FireSaleService.buildItem(itemId)).getUpdatedItem();
                    String stock = sale.unlimited() ? "Unlimited" : StringUtility.commaify((int) active.remaining()) + " <7>of <a>" + StringUtility.commaify((int) sale.stock());
                    String unavailable = sale.unavailableReason(itemId);
                    String action = over ? "<c>This Fire Sale has ended!"
                            : unavailable != null ? "<c>" + unavailable
                            : player.getGems() < sale.gems() ? "<c>You cannot afford this!"
                            : "<e>Click to purchase!";
                    return ItemStacks.appendLore(stack, " \n<7>Cost\n<a>" + StringUtility.commaify(sale.gems()) + " Gems\n \n<7>Stock left: <a>" + stock
                            + "\n<7>Ends in: <e>" + remainingText(active) + "\n \n<7>You have: <a>" + StringUtility.commaify(player.getGems()) + " Gems\n \n" + action);
                }
            });
        }
    }

    private static String remainingText(ActiveFireSale active) {
        long seconds = Math.max(0, (active.endsAt() - System.currentTimeMillis()) / 1000);
        return seconds >= 3600 ? seconds / 3600 + "h " + seconds % 3600 / 60 + "m" : seconds / 60 + "m " + seconds % 60 + "s";
    }

    private static int[] saleSlots(int count) {
        return switch (count) {
            case 1 -> new int[]{22};
            case 2 -> new int[]{21, 23};
            case 3 -> new int[]{20, 22, 24};
            default -> java.util.stream.IntStream.range(19, 19 + Math.min(count, 7)).toArray();
        };
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
    }
}
