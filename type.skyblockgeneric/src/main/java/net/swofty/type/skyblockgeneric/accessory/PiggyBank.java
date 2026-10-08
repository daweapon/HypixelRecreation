package net.swofty.type.skyblockgeneric.accessory;

import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryBag;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

/**
 * Piggy Banks keep coins a death would take, once the loss reaches 20,000 coins: the Piggy Bank keeps
 * all of them and cracks, the Cracked Piggy Bank keeps 75% and breaks. The share is the effect's
 * {@code percent}.
 */
public final class PiggyBank {
    public static final String HANDLER = "PIGGY_BANK";
    public static final double MIN_LOSS = 20_000;
    private static final int MAX_BAG_SLOTS = 400;
    private static final Map<ItemType, ItemType> DAMAGED = Map.of(
            ItemType.PIGGY_BANK, ItemType.CRACKED_PIGGY_BANK,
            ItemType.CRACKED_PIGGY_BANK, ItemType.BROKEN_PIGGY_BANK);

    private PiggyBank() {
    }

    /** Coins the player keeps out of {@code loss}; the bank that saved them is damaged. */
    public static double save(SkyBlockPlayer player, double loss) {
        if (loss < MIN_LOSS) return 0;
        for (AccessoryHandlerEffects.Active active : AccessoryHandlerEffects.of(player, HANDLER)) {
            ItemType bank = active.accessory().type();
            ItemType damaged = DAMAGED.get(bank);
            if (damaged == null) continue;
            replace(player, bank, damaged, active.accessory().inBag());
            AccessoryOwnership.invalidate(player);
            return loss * Math.min(100, active.effect().percent()) / 100D;
        }
        return 0;
    }

    private static void replace(SkyBlockPlayer player, ItemType from, ItemType to, boolean inBag) {
        if (inBag) {
            DatapointAccessoryBag.PlayerAccessoryBag bag = player.getAccessoryBag();
            for (int slot = 0; slot < MAX_BAG_SLOTS; slot++) {
                SkyBlockItem item = bag.getInSlot(slot);
                if (item == null || item.getAttributeHandler().getPotentialType() != from) continue;
                bag.setInSlot(slot, new SkyBlockItem(to));
                AccessoryBag.save(player, bag);
                return;
            }
        }
        for (int slot = 0; slot < PlayerInventory.INNER_INVENTORY_SIZE; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) continue;
            if (new SkyBlockItem(stack).getAttributeHandler().getPotentialType() != from) continue;
            player.getInventory().setItemStack(slot, PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(to).getItemStack()).build());
            return;
        }
    }
}
