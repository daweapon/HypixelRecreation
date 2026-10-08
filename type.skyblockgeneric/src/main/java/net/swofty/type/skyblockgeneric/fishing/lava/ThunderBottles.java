package net.swofty.type.skyblockgeneric.fishing.lava;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.hunting.AttributeId;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Empty Thunder Bottles (wiki: Empty_Thunder_Bottle) fill to 50,000 charge from Thunder's attacks and sparks; only
 * the first one in the inventory charges, and a full bottle becomes Thunder in a Bottle. The Thunder Shard's
 * Thunder Speed attribute fills it faster.
 */
public final class ThunderBottles {
    public static final int CAPACITY = 50_000;
    public static final String EMPTY_ID = "THUNDER_IN_A_BOTTLE_EMPTY";
    public static final String FULL_ID = "THUNDER_IN_A_BOTTLE";
    private static final int INVENTORY_SLOTS = 36;

    private ThunderBottles() {
    }

    public static void charge(SkyBlockPlayer player, int amount) {
        ItemType empty = ItemType.get(EMPTY_ID);
        ItemType full = ItemType.get(FULL_ID);
        if (empty == null || full == null) return;
        double speed = 1D + AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("R68")) / 100D;
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir()) continue;
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.getAttributeHandler().getPotentialType() != empty) continue;

            int charge = (int) Math.min(CAPACITY, item.getAttributeHandler().getThunderCharge() + Math.round(amount * speed));
            if (charge >= CAPACITY) {
                SkyBlockItem bottled = new SkyBlockItem(full);
                player.getInventory().setItemStack(slot, PlayerItemUpdater.playerUpdate(player, bottled.getItemStack()).build());
                player.sendMessage("<e>> Your bottle of thunder has fully charged!");
                return;
            }
            item.getAttributeHandler().setThunderCharge(charge);
            player.getInventory().setItemStack(slot, PlayerItemUpdater.playerUpdate(player, item.getItemStack()).build());
            return;
        }
    }
}
