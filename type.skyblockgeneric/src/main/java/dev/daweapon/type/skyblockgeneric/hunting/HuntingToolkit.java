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
import java.util.List;

// Hunting Toolkit: one of each Hunting tool and the five Huntrap tiers, unlocked by consuming the item.
public final class HuntingToolkit {
    public static final String HANDLER = "HUNTING_TOOLKIT";
    public static final String UNLOCKED_COUNTER = "hunting_toolkit";
    public static final double SKYBLOCK_XP = 2;

    public enum Group {HUNTAXE, BLACK_HOLE, LASSO, FISHING_NET, HUNTRAP}

    public record Slot(Group group, ItemType type, String name, @Nullable HuntrapTier trap) {
        public String key() {
            return type.name();
        }
    }

    public static final List<Slot> SLOTS = slots();

    private HuntingToolkit() {
    }

    private static List<Slot> slots() {
        List<Slot> slots = new ArrayList<>();
        slots.add(new Slot(Group.HUNTAXE, ItemType.VENATOR_GENESIS, "Worn Huntaxe - Genesis", null));
        slots.add(new Slot(Group.HUNTAXE, ItemType.SILVA_DOMINUS, "Sharpened Huntaxe - Dominus", null));
        slots.add(new Slot(Group.HUNTAXE, ItemType.CURSUS_FERAE, "Reinforced Huntaxe - Cursus", null));
        slots.add(new Slot(Group.HUNTAXE, ItemType.APEX_PRAEDATOR, "Savage Huntaxe - Praedator", null));
        slots.add(new Slot(Group.HUNTAXE, ItemType.NEX_TITANUM, "Prime Huntaxe - Nex Titanum", null));
        slots.add(new Slot(Group.BLACK_HOLE, ItemType.SMALL_POCKET_BLACK_HOLE, "Small Pocket Black Hole", null));
        slots.add(new Slot(Group.BLACK_HOLE, ItemType.MEDIUM_POCKET_BLACK_HOLE, "Medium Pocket Black Hole", null));
        slots.add(new Slot(Group.BLACK_HOLE, ItemType.LARGE_POCKET_BLACK_HOLE, "Large Pocket Black Hole", null));
        slots.add(new Slot(Group.LASSO, ItemType.ABYSMAL_LASSO, "Abysmal Lasso", null));
        slots.add(new Slot(Group.LASSO, ItemType.VINERIP_LASSO, "Vinerip Lasso", null));
        slots.add(new Slot(Group.LASSO, ItemType.ENTANGLER_LASSO, "Entangler Lasso", null));
        slots.add(new Slot(Group.LASSO, ItemType.EVERSTRETCH_LASSO, "Everstretch Lasso", null));
        slots.add(new Slot(Group.FISHING_NET, ItemType.BASIC_FISHING_NET, "Basic Fishing Net", null));
        slots.add(new Slot(Group.FISHING_NET, ItemType.MEDIUM_FISHING_NET, "Medium Fishing Net", null));
        slots.add(new Slot(Group.FISHING_NET, ItemType.TURBO_FISHING_NET, "Turbo Fishing Net", null));
        slots.add(new Slot(Group.FISHING_NET, ItemType.GIGANTIC_FISHING_NET, "Gigantic Fishing Net", null));
        for (HuntrapTier tier : HuntrapTier.values())
            slots.add(new Slot(Group.HUNTRAP, tier.itemType(), tier.displayName(), tier));
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
        return SLOTS.stream().filter(slot -> slot.key().equals(itemId)).findFirst().orElse(null);
    }

    public static @Nullable Slot slotOf(HuntrapTier tier) {
        return SLOTS.stream().filter(slot -> slot.trap() == tier).findFirst().orElse(null);
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
            player.sendMessage("<c>The toolkit already holds a " + slot.name() + "<c>!");
            return false;
        }
        if (slot.trap() != null && stack.amount() > 1) {
            player.getInventory().setItemStack(inventorySlot, stack.withAmount(stack.amount() - 1));
            player.getHuntingData().storeToolkitItem(slot.key(), new SkyBlockItem(slot.type()).toUnderstandable().serialize());
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

    public static boolean returnTrap(SkyBlockPlayer player, HuntrapTier tier) {
        Slot slot = slotOf(tier);
        if (slot == null || !isUnlocked(player) || stored(player, slot) != null) return false;
        player.getHuntingData().storeToolkitItem(slot.key(), new SkyBlockItem(tier.itemType()).toUnderstandable().serialize());
        return true;
    }
}
