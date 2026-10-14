package net.swofty.type.skyblockgeneric.user;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointQuiver;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointStash;
import net.swofty.type.skyblockgeneric.event.actions.player.ActionPlayerChangeHypixelMenuDisplay;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.components.ArrowComponent;
import net.swofty.type.skyblockgeneric.item.components.ItemRequirementsComponent;
import net.swofty.type.skyblockgeneric.item.set.ArmorSetRegistry;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

class PlayerInventoryHandler {
    private final SkyBlockPlayer player;

    PlayerInventoryHandler(SkyBlockPlayer player) {
        this.player = player;
    }

    @Nullable SkyBlockItem getArrow() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItemStack(i);
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.hasComponent(ArrowComponent.class) && canUseArrow(item)) {
                return item;
            }
        }

        if (!player.hasCustomCollectionAward(CustomCollectionAward.QUIVER)) return null;

        DatapointQuiver.PlayerQuiver quiver = player.getQuiver();
        return quiver.getFirstItemInQuiver();
    }

    int getAmountOfEmptySlots() {
        int amountOfEmptySlots = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItemStack(i);
            if (stack.material() == Material.AIR) amountOfEmptySlots++;
        }
        return amountOfEmptySlots;
    }

    boolean hasEmptySlots(int amount) {
        return getAmountOfEmptySlots() >= amount;
    }

    private boolean canUseArrow(SkyBlockItem arrow) {
        return !arrow.hasComponent(ItemRequirementsComponent.class)
                || arrow.getComponent(ItemRequirementsComponent.class).canUse(player);
    }

    @Nullable SkyBlockItem peekArrow() {
        for (int i = 0; i < 36; i++) {
            SkyBlockItem item = new SkyBlockItem(player.getInventory().getItemStack(i));
            if (item.hasComponent(ArrowComponent.class) && canUseArrow(item)) return item;
        }
        if (!player.hasCustomCollectionAward(CustomCollectionAward.QUIVER)) return null;
        return player.getQuiver().getFirstItemInQuiver();
    }

    @Nullable SkyBlockItem getAndConsumeArrow() {
        // Check Inventory first
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItemStack(i);
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.hasComponent(ArrowComponent.class) && canUseArrow(item)) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                    player.getInventory().setItemStack(i, PlayerItemUpdater.playerUpdate(player, item.getItemStack()).build());
                    ActionPlayerChangeHypixelMenuDisplay.runCheck(player);
                } else {
                    player.getInventory().setItemStack(i, ItemStack.of(Material.AIR));
                }
                return item;
            }
        }

        if (!player.hasCustomCollectionAward(CustomCollectionAward.QUIVER)) return null;

        DatapointQuiver.PlayerQuiver quiver = player.getQuiver();
        SkyBlockItem item = quiver.getFirstItemInQuiver();
        if (item == null) return null;

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            quiver.setFirstItemInQuiver(item);
        } else {
            quiver.setFirstItemInQuiver(null);
        }
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.QUIVER, DatapointQuiver.class).setValue(quiver);
        ActionPlayerChangeHypixelMenuDisplay.runCheck(player);
        return item;
    }

    SkyBlockItem[] getAllInventoryItems() {
        return Stream.of(player.getInventory().getItemStacks())
                .map(SkyBlockItem::new)
                .toArray(SkyBlockItem[]::new);
    }

    Map<Integer, SkyBlockItem> getAllOfComponentInInventory(Class<? extends SkyBlockItemComponent> component) {
        Map<Integer, SkyBlockItem> map = new HashMap<>();

        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItemStack(i);
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.getAttributeHandler().getPotentialType() == null) continue;

            if (item.hasComponent(component)) {
                map.put(i, item);
            }
        }

        return map;
    }

    Map<Integer, Integer> getAllOfTypeInInventory(ItemType type) {
        Map<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItemStack(i);
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.getAttributeHandler().getPotentialType() == null) continue;

            if (item.getAttributeHandler().getPotentialType() == type) {
                map.put(i, stack.amount());
            }
        }

        return map;
    }

    void setItemInHand(@Nullable SkyBlockItem item) {
        if (item == null) {
            player.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.AIR));
            return;
        }

        player.setItemInHand(PlayerHand.MAIN, PlayerItemUpdater.playerUpdate(player, item.getItemStack()).build());
    }

    int getAmountInInventory(ItemType type) {
        return getAllOfTypeInInventory(type).values().stream().mapToInt(Integer::intValue).sum();
    }

    int getAmountAnywhere(ItemType type) {
        return getAmountInInventory(type) + player.getSackItems().getAmount(type) + StoredItems.count(player, type);
    }

    boolean removeItemFromPlayer(ItemType type, int amount) {
        int inventoryAmount = getAmountInInventory(type);
        int sacksAmount = player.getSackItems().getAmount(type);
        int stored = StoredItems.count(player, type);
        if (inventoryAmount + sacksAmount + stored < amount) return false;

        int left = amount;
        int fromInventory = Math.min(left, inventoryAmount);
        if (fromInventory > 0) takeItem(type, fromInventory);
        left -= fromInventory;
        int fromSacks = Math.min(left, sacksAmount);
        if (fromSacks > 0) player.getSackItems().decrease(type, fromSacks);
        left -= fromSacks;
        if (left > 0) StoredItems.take(player, type, left);
        return true;
    }

    boolean removeItemsFromPlayer(Map<ItemType, Integer> items) {
        for (Map.Entry<ItemType, Integer> item : items.entrySet()) {
            int amount = item.getValue();
            ItemType type = item.getKey();

            int inventoryAmount = getAmountInInventory(type);
            int sacksAmount = player.getSackItems().getAmount(type);

            if ((inventoryAmount + sacksAmount) < amount) return false;
        }
        for (Map.Entry<ItemType, Integer> item : items.entrySet()) {
            removeItemFromPlayer(item.getKey(), item.getValue());
        }
        return true;
    }

    @Nullable ArmorSetRegistry getArmorSet() {
        ItemType helmet = new SkyBlockItem(player.getHelmet()).getAttributeHandler().getPotentialType();
        ItemType chestplate = new SkyBlockItem(player.getChestplate()).getAttributeHandler().getPotentialType();
        ItemType leggings = new SkyBlockItem(player.getLeggings()).getAttributeHandler().getPotentialType();
        ItemType boots = new SkyBlockItem(player.getBoots()).getAttributeHandler().getPotentialType();

        return ArmorSetRegistry.getArmorSet(boots, leggings, chestplate, helmet);
    }

    boolean isWearingItem(SkyBlockItem item) {
        SkyBlockItem[] armor = getArmor();

        for (SkyBlockItem armorItem : armor) {
            if (armorItem == null) continue;
            if (armorItem.getAttributeHandler().getPotentialType() == item.getAttributeHandler().getPotentialType()) {
                return true;
            }
        }

        return false;
    }

    SkyBlockItem[] getArmor() {
        return new SkyBlockItem[] {
                new SkyBlockItem(player.getHelmet()),
                new SkyBlockItem(player.getChestplate()),
                new SkyBlockItem(player.getLeggings()),
                new SkyBlockItem(player.getBoots())
        };
    }

    SkyBlockItem updateItem(PlayerItemOrigin origin, Consumer<SkyBlockItem> update) {
        ItemStack toUpdate = origin.getStack(player);
        if (toUpdate == null) return null;

        SkyBlockItem item = new SkyBlockItem(toUpdate);
        update.accept(item);

        origin.setStack(player, PlayerItemUpdater.playerUpdate(player, item.getItemStack()).build());
        return item;
    }

    SkyBlockItem updateItemInSlot(int slot, Consumer<SkyBlockItem> update) {
        ItemStack toUpdate = player.getInventory().getItemStack(slot);
        if (toUpdate == null) return null;

        SkyBlockItem item = new SkyBlockItem(toUpdate);
        update.accept(item);

        player.getInventory().setItemStack(slot, PlayerItemUpdater.playerUpdate(player, item.getItemStack()).build());
        return item;
    }

    void addAndUpdateItem(@Nullable List<SkyBlockItem> items) {
        if (items == null) return;
        for (SkyBlockItem item : items) {
            addAndUpdateItem(item);
        }
    }

    void addAndUpdateItem(@Nullable SkyBlockItem item) {
        if (item == null) return;
        if (item.isNA()) return;

        ItemStack toAdd = PlayerItemUpdater.playerUpdate(
                player,
                item.getItemStack(),
                true).build();

        // Check if inventory can fit the item
        if (canFitItem(toAdd)) {
            player.getInventory().addItemStack(toAdd);
        } else {
            // Add to stash since inventory is full
            addToStash(item);
        }
    }

    void addAndUpdateItem(UnderstandableSkyBlockItem item) {
        addAndUpdateItem(new SkyBlockItem(item));
    }

    void addAndUpdateItem(ItemType item) {
        addAndUpdateItem(new SkyBlockItem(item));
    }

    void addAndUpdateItem(ItemType item, int amount) {
        for (int i = 0; i < amount; i++) {
            addAndUpdateItem(new SkyBlockItem(item));
        }
    }

    void addAndUpdateItem(ItemStack item) {
        addAndUpdateItem(new SkyBlockItem(item));
    }

    int countItem(ItemType item) {
        AtomicInteger count = new AtomicInteger();
        getAllOfTypeInInventory(item).forEach((stack, meow) -> count.addAndGet(meow));
        return count.get();
    }

    boolean takeItem(SkyBlockItem itemToTake) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItemStack(i);
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.isSimilar(itemToTake)) {
                player.getInventory().setItemStack(i, ItemStack.of(Material.AIR));
                return true;
            }
        }

        return false;
    }

    @Nullable List<SkyBlockItem> takeItem(ItemType type, int amount) {
        List<SkyBlockItem> consumedItems = new ArrayList<>();
        Map<Integer, Integer> map = getAllOfTypeInInventory(type);
        int total = map.values().stream().mapToInt(Integer::intValue).sum();
        if (total < amount) return null;

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (amount <= 0) break;

            int slot = entry.getKey();
            int currentAmount = entry.getValue();

            SkyBlockItem skyBlockItem = new SkyBlockItem(player.getInventory().getItemStack(slot));
            skyBlockItem.setAmount(Math.min(skyBlockItem.getAmount(), amount));

            consumedItems.add(skyBlockItem);
            ItemStack item = player.getInventory().getItemStack(slot).consume(amount);

            player.getInventory().setItemStack(slot, item);
            amount -= Math.min(amount, currentAmount);
        }
        return consumedItems;
    }

    /**
     * Check if the inventory can fit the given item.
     * Takes into account both empty slots and existing stackable stacks.
     */
    boolean canFitItem(ItemStack item) {
        if (item == null || item.isAir()) return true;

        int amount = item.amount();
        int maxStackSize = item.material().maxStackSize();

        // For non-stackable items, just check for empty slots
        if (maxStackSize == 1) {
            return hasEmptySlots(1);
        }

        // For stackable items, check existing stacks first
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItemStack(i);
            if (stack.isAir()) {
                // Empty slot can hold up to maxStackSize
                amount -= maxStackSize;
            } else if (stack.isSimilar(item) && stack.amount() < maxStackSize) {
                // Existing stack can hold more
                amount -= (maxStackSize - stack.amount());
            }
            if (amount <= 0) return true;
        }
        return false;
    }

    /**
     * Add an item to the player's stash when inventory is full.
     * Routes to item stash (non-stackable) or material stash (stackable).
     */
    void addToStash(SkyBlockItem item) {
        DatapointStash.PlayerStash stash = player.getStash();
        ItemType type = item.getAttributeHandler().getPotentialType();
        int maxStackSize = item.getMaterial().maxStackSize();

        boolean isStackable = maxStackSize > 1;
        String stashType;

        if (isStackable) {
            // Add to material stash (unlimited)
            stash.addToMaterialStash(type, item.getAmount());
            stashType = "material";
        } else {
            // Add to item stash (720 limit)
            boolean added = stash.addToItemStash(item);
            stashType = "item";

            if (!added) {
                // Stash is full - item is lost
                int overflowCount = 1;
                player.sendMessage("<c>Uh oh! <e>{} <c>item{} couldn't be stashed because you hit the item stash limit!",
                        overflowCount, overflowCount > 1 ? "s" : "");
                return;
            }

            // Check for near-full warning
            if (stash.isItemStashNearFull()) {
                player.sendMessage("<c>YOUR STASH IS ALMOST AT MAX CAPACITY!");
            }
        }

        // Send stash notification with clickable message
        sendStashNotification(stashType);
    }

    /**
     * Send a clickable notification that an item was added to the stash.
     */
    private void sendStashNotification(String stashType) {
        player.sendMessage("<e>One or more items didn't fit in your inventory and were added to your {0} stash! "
                + "<click:run:'/pickupstash {0}'><6>Click here <e>to pick them up!</click>", stashType);
    }

    int maxItemFit(ItemType targetType) {
        int fit = 0;
        int maxStack = targetType.material.maxStackSize();

        PlayerInventory inv = player.getInventory();
        int size = inv.getSize();

        for (int slot = 0; slot < size; slot++) {
            ItemStack stack = inv.getItemStack(slot);
            // Interpret the stack via ItemType
            ItemType slotType = ItemType.fromMaterial(stack.material());

            if (slotType == ItemType.AIR || slotType == null) {
                fit += maxStack;
            } else if (slotType.equals(targetType)) {
                fit += (maxStack - stack.amount());
            }
        }
        return fit;
    }
}
