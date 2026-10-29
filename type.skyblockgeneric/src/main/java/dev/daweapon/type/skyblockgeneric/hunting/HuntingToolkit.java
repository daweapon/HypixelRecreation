package dev.daweapon.type.skyblockgeneric.hunting;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Hunting Toolkit: one slot each for Huntaxe, Pocket Black Hole, Lasso and Fishing Net, plus one Huntrap of any tier, unlocked by consuming the item.
public final class HuntingToolkit {
    public static final String HANDLER = "HUNTING_TOOLKIT";
    public static final String UNLOCKED_COUNTER = "hunting_toolkit";
    public static final double SKYBLOCK_XP = 2;

    public enum Group {HUNTAXE, BLACK_HOLE, LASSO, FISHING_NET, HUNTRAP}

    public record Slot(Group group, List<ItemType> types, String name) {
        public String key() {
            return group.name();
        }

        public boolean isTrap() {
            return group == Group.HUNTRAP;
        }

        public ItemType type() {
            return types.getFirst();
        }

        public ItemType best() {
            return types.getLast();
        }
    }

    public static final List<Slot> SLOTS = slots();

    private HuntingToolkit() {
    }

    private static List<Slot> slots() {
        List<Slot> slots = new ArrayList<>();
        slots.add(new Slot(Group.HUNTAXE, List.of(ItemType.VENATOR_GENESIS, ItemType.SILVA_DOMINUS, ItemType.CURSUS_FERAE,
                ItemType.APEX_PRAEDATOR, ItemType.NEX_TITANUM), "Huntaxe"));
        slots.add(new Slot(Group.BLACK_HOLE, List.of(ItemType.SMALL_POCKET_BLACK_HOLE, ItemType.MEDIUM_POCKET_BLACK_HOLE,
                ItemType.LARGE_POCKET_BLACK_HOLE), "Pocket Black Hole"));
        slots.add(new Slot(Group.LASSO, List.of(ItemType.ABYSMAL_LASSO, ItemType.VINERIP_LASSO, ItemType.ENTANGLER_LASSO,
                ItemType.EVERSTRETCH_LASSO), "Lasso"));
        slots.add(new Slot(Group.FISHING_NET, List.of(ItemType.BASIC_FISHING_NET, ItemType.MEDIUM_FISHING_NET,
                ItemType.TURBO_FISHING_NET, ItemType.GIGANTIC_FISHING_NET), "Fishing Net"));
        slots.add(new Slot(Group.HUNTRAP, Arrays.stream(HuntrapTier.values()).map(HuntrapTier::itemType).toList(), "Huntrap"));
        return List.copyOf(slots);
    }

    public static void registerInteractables() {
        InteractableRegistry.register(HANDLER, InteractableItemConfig.builder()
                .rightClickHandler((player, _) -> consume(player))
                .build());
    }

    public static boolean isUnlocked(SkyBlockPlayer player) {
        return player.getHuntingData().counter(UNLOCKED_COUNTER) > 0;
    }

    public static void unlock(SkyBlockPlayer player) {
        player.getHuntingData().setCounter(UNLOCKED_COUNTER, 1);
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.HUNTING_TOOLKIT, SKYBLOCK_XP);
    }

    public static boolean consume(SkyBlockPlayer player) {
        if (isUnlocked(player)) {
            player.sendMessage("<c>You have already unlocked this toolkit!");
            return false;
        }
        if (!player.removeItemFromPlayer(ItemType.HUNTING_TOOLKIT, 1)) return false;
        unlock(player);
        player.sendMessage("<a>You have unlocked the Hunting Toolkit! Access it with <e>/huntingtoolkit");
        return true;
    }

    public static @Nullable Slot slotOf(@Nullable String itemId) {
        if (itemId == null) return null;
        return SLOTS.stream().filter(slot -> slot.types().stream().anyMatch(type -> type.name().equals(itemId))).findFirst().orElse(null);
    }

    public static boolean store(SkyBlockPlayer player, int inventorySlot) {
        ItemStack stack = player.getInventory().getItemStack(inventorySlot);
        if (stack.isAir()) return false;
        SkyBlockItem item = new SkyBlockItem(stack);
        Slot slot = slotOf(item.getAttributeHandler().getTypeAsString());
        if (slot == null) {
            player.sendMessage("<c>That is not a Hunting Tool the toolkit can hold!");
            return false;
        }
        if (stored(player, slot) != null) {
            retrieve(player, slot);
        }
        if (slot.isTrap() && stack.amount() > 1) {
            player.getInventory().setItemStack(inventorySlot, stack.withAmount(stack.amount() - 1));
            player.getHuntingData().storeToolkitItem(slot.key(), new SkyBlockItem(item.getAttributeHandler().getPotentialType()).toUnderstandable().serialize());
            return true;
        }
        player.getHuntingData().storeToolkitItem(slot.key(), item.toUnderstandable().serialize());
        player.getInventory().setItemStack(inventorySlot, ItemStack.AIR);
        return true;
    }

    public static boolean retrieve(SkyBlockPlayer player, Slot slot) {
        String serialized = player.getHuntingData().takeToolkitItem(slot.key());
        if (serialized == null) return false;
        player.addAndUpdateItem(new SkyBlockItem(UnderstandableSkyBlockItem.deserialize(serialized)));
        return true;
    }

    public static @Nullable SkyBlockItem stored(SkyBlockPlayer player, Slot slot) {
        String serialized = player.getHuntingData().toolkitItem(slot.key());
        return serialized == null ? null : new SkyBlockItem(UnderstandableSkyBlockItem.deserialize(serialized));
    }

    public static void stockBest(SkyBlockPlayer player) {
        for (Slot slot : SLOTS) {
            if (slot.isTrap()) continue;
            player.getHuntingData().storeToolkitItem(slot.key(), new SkyBlockItem(slot.best()).toUnderstandable().serialize());
        }
    }

    public static boolean returnTrap(SkyBlockPlayer player, HuntrapTier tier) {
        Slot slot = SLOTS.getLast();
        if (!isUnlocked(player) || stored(player, slot) != null) return false;
        player.getHuntingData().storeToolkitItem(slot.key(), new SkyBlockItem(tier.itemType()).toUnderstandable().serialize());
        return true;
    }

    public static @Nullable HuntrapTier storedTier(SkyBlockPlayer player, Slot slot) {
        SkyBlockItem item = stored(player, slot);
        if (item == null) return null;
        String type = item.getAttributeHandler().getTypeAsString();
        return Arrays.stream(HuntrapTier.values()).filter(tier -> tier.itemType().name().equals(type)).findFirst().orElse(null);
    }
}
