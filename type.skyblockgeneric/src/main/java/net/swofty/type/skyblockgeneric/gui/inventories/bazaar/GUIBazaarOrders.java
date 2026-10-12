package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.bazaar.BazaarConnector;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCompletedBazaarTransactions;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

// GUI listing a player's active bazaar buy and sell orders
public class GUIBazaarOrders extends HypixelInventoryGUI {
    private static final int[] SELL_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int[] BUY_SLOTS = {19, 20, 21, 22, 23, 24, 25};
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");

    public GUIBazaarOrders() {
        super(Text.key("gui_bazaar.orders.title"), InventoryType.CHEST_4_ROW);
        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        clearSlots();

        set(BazaarMenuButtons.back(30, "Bazaar", () -> new GUIBazaar(BazaarCategories.FARMING)));
        set(GUIClickableItem.getCloseItem(31));
        set(new GUIClickableItem(32) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                claimAllCoins((SkyBlockPlayer) p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.HOPPER, 1, Text.key("gui_bazaar.orders.claim_all"),
                        Text.keyLines("gui_bazaar.orders.claim_all.lore"));
            }
        });
    }

    private void claimAllCoins(SkyBlockPlayer player) {
        var completed = player.getSkyblockDataHandler().get(
                SkyBlockDataHandler.Data.COMPLETED_BAZAAR_TRANSACTIONS,
                DatapointCompletedBazaarTransactions.class
        ).getValue();
        var sold = completed.getUnclaimedTransactions().stream()
                .filter(tx -> tx.getType() == DatapointCompletedBazaarTransactions.TransactionType.SELL_COMPLETED)
                .toList();
        if (sold.isEmpty()) {
            player.sendMessage(Text.key("gui_bazaar.order_completed.no_rewards"));
            return;
        }
        double coins = sold.stream().mapToDouble(tx -> Math.abs(tx.getTotalValue())).sum();
        player.addCoins(coins);
        completed.claimTransactions(sold.stream().map(DatapointCompletedBazaarTransactions.CompletedBazaarTransaction::getId).toList());
        player.sendMessage(Text.key("gui_bazaar.order_completed.received_coins", FORMATTER.format(coins)));
        player.playSuccessSound();
        new GUIBazaarOrders().open(player);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        if (!((SkyBlockPlayer) e.player()).isCoop()) {
            setTitle(Text.key("gui_bazaar.orders.title_solo"));
        }
        ((SkyBlockPlayer) e.player()).getBazaarConnector().getPendingOrders()
                .thenAccept(orders -> loadAndDisplayOrders(((SkyBlockPlayer) e.player()), orders));
    }

    private void loadAndDisplayOrders(SkyBlockPlayer player, List<BazaarConnector.BazaarOrder> activeOrders) {
        var completedTransactions = player.getSkyblockDataHandler().get(
                SkyBlockDataHandler.Data.COMPLETED_BAZAAR_TRANSACTIONS,
                DatapointCompletedBazaarTransactions.class
        ).getValue();

        var unclaimedTransactions = completedTransactions.getUnclaimedTransactions();

        Map<UUID, List<DatapointCompletedBazaarTransactions.CompletedBazaarTransaction>> groupedCompletions =
                unclaimedTransactions.stream()
                        .filter(tx -> tx.getRelatedOrderId() != null)
                        .collect(Collectors.groupingBy(DatapointCompletedBazaarTransactions.CompletedBazaarTransaction::getRelatedOrderId));

        List<OrderDisplayItem> displayItems = new ArrayList<>();

        for (var activeOrder : activeOrders) {
            var completions = groupedCompletions.get(activeOrder.orderId());
            if (completions != null && !completions.isEmpty()) {
                displayItems.add(new OrderDisplayItem(activeOrder, completions, true));
                displayItems.add(new OrderDisplayItem(activeOrder, null, false));
            } else {
                displayItems.add(new OrderDisplayItem(activeOrder, null, false));
            }
        }

        for (var entry : groupedCompletions.entrySet()) {
            UUID orderId = entry.getKey();
            var completions = entry.getValue();

            boolean hasActiveOrder = activeOrders.stream()
                    .anyMatch(order -> order.orderId().equals(orderId));

            if (!hasActiveOrder) {
                displayItems.add(new OrderDisplayItem(null, completions, true));
            }
        }

        updateOrderDisplay(displayItems);
    }

    private void updateOrderDisplay(List<OrderDisplayItem> items) {
        clearSlots();

        int sellIndex = 0, buyIndex = 0;

        for (OrderDisplayItem item : items) {
            boolean isSell = item.isSellOrder();
            int[] slots = isSell ? SELL_SLOTS : BUY_SLOTS;
            int index = isSell ? sellIndex++ : buyIndex++;

            if (index >= slots.length) break;

            int slot = slots[index];
            set(createOrderItem(slot, item));
        }

        updateItemStacks(getInventory(), getPlayer());
    }

    private GUIClickableItem createOrderItem(int slot, OrderDisplayItem item) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                if (item.isCompleted()) {
                    new GUIBazaarOrderCompletedOptions(item.getCompletions(), item.getActiveOrder()).open(p);
                } else {
                    new GUIBazaarOrderOptions(item.getActiveOrder()).open(p);
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return item.createDisplayItem();
            }
        };
    }

    private void clearSlots() {
        for (int slot : SELL_SLOTS) {
            set(new GUIItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStack.builder(Material.AIR);
                }
            });
        }
        for (int slot : BUY_SLOTS) {
            set(new GUIItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStack.builder(Material.AIR);
                }
            });
        }
    }

    private static class OrderDisplayItem {
        private final BazaarConnector.BazaarOrder activeOrder;
        private final List<DatapointCompletedBazaarTransactions.CompletedBazaarTransaction> completions;
        private final boolean isCompleted;

        public OrderDisplayItem(BazaarConnector.BazaarOrder activeOrder,
                                List<DatapointCompletedBazaarTransactions.CompletedBazaarTransaction> completions,
                                boolean isCompleted) {
            this.activeOrder = activeOrder;
            this.completions = completions;
            this.isCompleted = isCompleted;
        }

        public boolean isSellOrder() {
            if (activeOrder != null) {
                return activeOrder.side() == BazaarConnector.OrderSide.SELL;
            } else if (completions != null && !completions.isEmpty()) {
                var firstCompletion = completions.getFirst();
                return firstCompletion.getType() == DatapointCompletedBazaarTransactions.TransactionType.SELL_COMPLETED ||
                        firstCompletion.getType() == DatapointCompletedBazaarTransactions.TransactionType.SELL_ORDER_EXPIRED;
            }
            return false;
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        public BazaarConnector.BazaarOrder getActiveOrder() {
            return activeOrder;
        }

        public List<DatapointCompletedBazaarTransactions.CompletedBazaarTransaction> getCompletions() {
            return completions;
        }

        public ItemStack.Builder createDisplayItem() {
            if (isCompleted && completions != null && !completions.isEmpty()) {
                return createCompletedOrderDisplay();
            } else if (activeOrder != null) {
                return createActiveOrderDisplay();
            }
            return ItemStack.builder(Material.AIR);
        }

        private ItemStack.Builder createCompletedOrderDisplay() {
            var firstCompletion = completions.getFirst();
            String itemName = firstCompletion.getItemName();
            ItemType itemType;

            try {
                itemType = ItemType.valueOf(itemName);
            } catch (IllegalArgumentException e) {
                itemType = ItemType.STONE;
            }

            List<Text> lore = new ArrayList<>();
            boolean isSell = isSellOrder();

            double totalQuantity = 0;
            double totalValue = 0;
            double totalRefund = 0;
            for (var completion : completions) {
                totalQuantity += completion.getQuantity();
                totalValue += completion.getTotalValue();
                totalRefund += completion.getSecondaryAmount();
            }

            lore.add(Text.key("gui_bazaar.orders.completed_label"));
            lore.add(Text.key("gui_bazaar.orders.completed_ready"));
            lore.add(Text.literal(" "));
            lore.add(Text.key("gui_bazaar.orders.completed_amount", (int) totalQuantity));
            lore.add(Text.key("gui_bazaar.orders.completed_value", FORMATTER.format(Math.abs(totalValue))));
            lore.add(Text.literal(" "));

            lore.add(Text.key("gui_bazaar.orders.completed_receive"));
            if (isSell) {
                lore.add(Text.of("  ").append(Text.key("gui_bazaar.orders.completed_receive_coins", FORMATTER.format(Math.abs(totalValue)))));
            } else {
                lore.add(Text.of("  ").append(Text.key("gui_bazaar.orders.completed_receive_items", (int) totalQuantity, itemType.getDisplayName())));
                if (totalValue > 0) {
                    lore.add(Text.of("  ").append(Text.key("gui_bazaar.orders.completed_receive_refund", FORMATTER.format(totalRefund))));
                }
            }

            lore.add(Text.literal(" "));
            lore.add(Text.key("gui_bazaar.orders.completed_click"));

            return ItemStacks.item(
                    itemType.material,
                    Math.max(1, (int) totalQuantity),
                    Text.of("<a><l>{} </l><f>{}", isSell ? "SELL" : "BUY", itemType.getDisplayName()),
                    lore
            );
        }

        private ItemStack.Builder createActiveOrderDisplay() {
            List<Text> lore = new ArrayList<>();
            boolean isSell = activeOrder.side() == BazaarConnector.OrderSide.SELL;
            ItemType itemType = activeOrder.getItemType();

            lore.add(Text.key("gui_bazaar.orders.active_worth", FORMATTER.format(activeOrder.getTotalValue())));
            lore.add(Text.literal(" "));
            lore.add(Text.key("gui_bazaar.orders.active_order_amount", (int) activeOrder.amount()));
            lore.add(Text.literal(" "));
            lore.add(Text.key("gui_bazaar.orders.active_price_per_unit", FORMATTER.format(activeOrder.price())));
            lore.add(Text.literal(" "));
            lore.add(Text.key("gui_bazaar.orders.active_click"));

            return ItemStacks.item(
                    itemType.material,
                    1,
                    Text.of(isSell ? "<6><l>{} </l><f>{}" : "<a><l>{} </l><f>{}",
                            activeOrder.side(), itemType.getDisplayName()),
                    lore
            );
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
}
