package dev.daweapon.type.skyblockgeneric.hunting.creature.trigger;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.consumable.TimedConsumables;
import dev.daweapon.type.skyblockgeneric.foraging.ForagingTreeManager;
import dev.daweapon.type.skyblockgeneric.hotf.HotfBeekeeper;
import dev.daweapon.type.skyblockgeneric.foraging.ForagingTreeManager.TreeRef;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;

public final class HoneycombPots {
    public static final String HANDLER_ID = "HONEYCOMB_POT_LATHER";
    private static final int TARGET_RANGE = 6;

    private HoneycombPots() {
    }

    static int minutesFor(ItemType type) {
        if (type == ItemType.FUN_SIZED_POT_OF_HONEYCOMB) return 60;
        if (type == ItemType.FAMILY_SIZED_POT_OF_HONEYCOMB) return 30;
        if (type == ItemType.JUMBO_POT_OF_HONEYCOMB) return 15;
        if (type == ItemType.BEHEMOTH_POT_OF_HONEYCOMB) return 0;
        return -1;
    }

    public static void registerInteractables() {
        InteractableRegistry.register(HANDLER_ID, InteractableItemConfig.builder()
                .rightClickHandler(HoneycombPots::lather)
                .build());
    }

    private static void lather(SkyBlockPlayer player, SkyBlockItem item) {
        int minutes = minutesFor(item.getAttributeHandler().getPotentialType());
        if (minutes < 0 || player.getInstance() == null) return;
        Point target = player.getTargetBlockPosition(TARGET_RANGE);
        TreeRef tree = target == null ? null : ForagingTreeManager.treeAt(player.getInstance(), target);
        if (tree == null || TreeProtectionOrders.attracted(tree.kind()).isEmpty()) {
            player.sendMessage("<c>Aim at a Fig, Mangrove or Helix Tree on this island to lather it with Honeycomb!");
            return;
        }
        if (TreeProtectionOrders.isActive(tree)) {
            player.sendMessage("<c>This tree is already lathered with Honeycomb!");
            return;
        }
        if (!TreeProtectionOrders.place(player, tree, minutes)) return;
        TimedConsumables.consumeHeld(player, item);
        if (HotfBeekeeper.active(player, HotfBeekeeper.EXTRA_HONEYCOMB)) {
            player.addAndUpdateItem(ItemType.HONEYCOMB, HotfBeekeeper.EXTRA_HONEYCOMB_AMOUNT);
            player.sendMessage("<6>Beekeeper<7> gave you <6>{} Honeycomb<7>!", HotfBeekeeper.EXTRA_HONEYCOMB_AMOUNT);
        }
        dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers.fire(player, "pot.attract");
        if (minutes == 0) player.sendMessage("<a>You lathered the tree with <6>Honeycomb<a>!");
        else player.sendMessage("<a>You lathered the tree with <6>Honeycomb<a>! A <a>Critter <a>will be attracted in <e>{}m<a>.", minutes);
    }
}
