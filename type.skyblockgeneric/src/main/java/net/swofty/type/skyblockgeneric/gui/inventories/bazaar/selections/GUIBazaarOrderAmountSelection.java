package net.swofty.type.skyblockgeneric.gui.inventories.bazaar.selections;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.bazaar.BazaarProduct;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIQueryItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// GUI for choosing the quantity when placing an instant or limit bazaar order
public class GUIBazaarOrderAmountSelection extends HypixelInventoryGUI {
    private static final DecimalFormat F = new DecimalFormat("#,###.##");

    private final boolean isBuy;
    private final boolean isInstant;
    private final BazaarProduct itemType;
    private final int maxAmount;
    private final double unitPrice;     // for instant this includes fee; for limit this is bestAsk or bestBid
    private CompletableFuture<Integer> future;

    public GUIBazaarOrderAmountSelection(
            HypixelInventoryGUI previous,
            BazaarProduct itemType,
            boolean isBuy,
            boolean isInstant,
            int maxAmount,
            double unitPrice
    ) {
        super(isInstant
                        ? Text.key(isBuy ? "gui_bazaar.amount_selection.title_instant_buy" : "gui_bazaar.amount_selection.title_instant_sell", itemType.name())
                        : Text.key(isBuy ? "gui_bazaar.amount_selection.title_buy" : "gui_bazaar.amount_selection.title_sell"),
                InventoryType.CHEST_4_ROW);

        this.isBuy = isBuy;
        this.isInstant = isInstant;
        this.itemType = itemType;
        this.maxAmount = maxAmount;
        this.unitPrice = unitPrice;

        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        if (isInstant) {
            set(GUIClickableItem.getGoBackItem(30, previous));
            set(GUIClickableItem.getCloseItem(31));
        } else {
            set(GUIClickableItem.getGoBackItem(31, previous));
        }
    }

    public CompletableFuture<Integer> openAmountSelection(SkyBlockPlayer player) {
        future = new CompletableFuture<>();
        open(player);

        if (isInstant) {
            buildInstantUI(player);
        } else {
            buildLimitUI(player);
        }

        updateItemStacks(getInventory(), getPlayer());
        return future;
    }

    private void buildInstantUI(SkyBlockPlayer p) {
        String verb = isBuy ? "buy" : "sell";
        addInstantButton(10, 1, Material.AIR, "gui_bazaar.amount_selection.instant_one_" + verb);
        addInstantButton(12, Math.min(itemType.material().maxStackSize(), maxAmount), Material.AIR,
                "gui_bazaar.amount_selection.instant_stack_" + verb);
        addInstantButton(14, maxAmount, Material.CHEST, "gui_bazaar.amount_selection.instant_all_" + verb);
        addCustom(16);
    }

    private void buildLimitUI(SkyBlockPlayer p) {
        // matches your screenshots: “Buy a stack!”, “Buy a big stack!”, “Buy a thousand!”, “Custom Amount”
        int small  = Math.min(64, maxAmount);
        int medium = Math.min(160, maxAmount);
        int large  = Math.min(1024,maxAmount);

        addLimitButton(9, small, "Buy a stack!",      "Amount: " + small + "×", p);
        addLimitButton(11, medium, "Buy a big stack!","Amount: " + medium + "×", p);
        addLimitButton(13, large, "Buy a thousand!",  "Amount: " + large + "×", p);
        addCustom(15);
    }

    private void addInstantButton(int slot, int qty, Material icon, String titleKey) {
        set(new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                future.complete(qty);
                p.closeInventory();
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<7>{}", itemType.name()));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.amount_selection.amount_line", F.format(qty)));
                lore.add(Text.key("gui_bazaar.amount_selection.price_line", F.format(unitPrice * qty)));
                lore.add(Text.literal(" "));
                lore.add(Text.key(isBuy
                        ? "gui_bazaar.amount_selection.click_buy"
                        : "gui_bazaar.amount_selection.click_sell"));
                return ItemStacks.item(
                        icon == Material.AIR ? itemType.material() : icon, Math.max(1, Math.min(qty, 64)),
                        Text.key(titleKey),
                        lore
                );
            }
        });
    }

    private void addLimitButton(int slot, int qty, String title, String amountLine, SkyBlockPlayer p) {
        set(new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                future.complete(qty);
                // *don’distance* close—so your price‐selection GUI will open next
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_bazaar.amount_selection.limit_subtitle"));
                lore.add(Text.of("<7>{}", amountLine));
                lore.add(Text.key("gui_bazaar.amount_selection.per_unit", F.format(unitPrice)));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.amount_selection.limit_click"));
                return ItemStacks.item(
                        slot == 13 ? Material.CHEST : itemType.material(),
                        qty,
                        Text.of("<b>{}", title),
                        lore
                );
            }
        });
    }

    private void addCustom(int slot) {
        set(new GUIQueryItem(slot) {
            @Override
            public HypixelInventoryGUI onQueryFinish(String q, HypixelPlayer pl) {
                try {
                    int v = Integer.parseInt(q);
                    if (v < 1 || v > maxAmount) {
                        pl.sendMessage(Text.key("gui_bazaar.amount_selection.invalid_range", maxAmount));
                        return null;
                    }
                    future.complete(v);
                } catch (NumberFormatException ex) {
                    pl.sendMessage(Text.key("gui_bazaar.amount_selection.invalid_number"));
                }
                return null;
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>();
                lore.add(Text.key("gui_bazaar.amount_selection.custom_limit"));
                lore.add(Text.key(isInstant
                        ? (isBuy ? "gui_bazaar.amount_selection.custom_max_buy" : "gui_bazaar.amount_selection.custom_max_sell")
                        : "gui_bazaar.amount_selection.custom_max", F.format(maxAmount)));
                lore.add(Text.literal(" "));
                lore.add(Text.key("gui_bazaar.amount_selection.custom_click"));
                return ItemStacks.item(
                        Material.OAK_SIGN, 1,
                        Text.key("gui_bazaar.amount_selection.custom_amount"),
                        lore
                );
            }
        });
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        if (!future.isDone()) future.complete(0);
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
