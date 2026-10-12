package net.swofty.type.skyblockgeneric.fishing.lava;

import net.swofty.type.skyblockgeneric.accessory.AccessoryEconomyHooks;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.hunting.AttributeId;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.Optional;

/**
 * Empty Thunder Bottles (wiki: Empty_Thunder_Bottle) fill to 50,000 charge from Thunder's attacks and sparks; only
 * the first one in the inventory charges, and a full bottle becomes Thunder in a Bottle. The Thunder Shard's
 * Thunder Speed attribute fills it faster. Empty Storm Bottles (wiki: Empty_Storm_Bottle) charge the same way up to
 * 500,000.
 */
public final class ThunderBottles {
    public static final int CAPACITY = 50_000;
    public static final String EMPTY_ID = "THUNDER_IN_A_BOTTLE_EMPTY";
    public static final String FULL_ID = "THUNDER_IN_A_BOTTLE";
    private static final int INVENTORY_SLOTS = 36;

    public enum Tier {
        THUNDER(EMPTY_ID, FULL_ID, CAPACITY),
        STORM("STORM_IN_A_BOTTLE_EMPTY", "STORM_IN_A_BOTTLE", 500_000);

        private final String emptyId;
        private final String fullId;
        private final int capacity;

        Tier(String emptyId, String fullId, int capacity) {
            this.emptyId = emptyId;
            this.fullId = fullId;
            this.capacity = capacity;
        }

        public String emptyId() {
            return emptyId;
        }

        public String fullId() {
            return fullId;
        }

        public int capacity() {
            return capacity;
        }
    }

    private ThunderBottles() {
    }

    public static Optional<Tier> tierOfEmpty(String itemId) {
        return Arrays.stream(Tier.values()).filter(tier -> tier.emptyId.equals(itemId)).findFirst();
    }

    public static int chargeAfter(int current, int amount, double speed, int capacity) {
        return (int) Math.min(capacity, current + Math.round(amount * speed));
    }

    public static void charge(SkyBlockPlayer player, int amount) {
        double speed = 1D + AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("R68")) / 100D
                + AccessoryEconomyHooks.thunderChargeBonus(player);
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir()) continue;
            SkyBlockItem item = new SkyBlockItem(stack);
            ItemType type = item.getAttributeHandler().getPotentialType();
            if (type == null) continue;
            Optional<Tier> tier = tierOfEmpty(type.name());
            if (tier.isEmpty()) continue;
            ItemType full = ItemType.get(tier.get().fullId());
            if (full == null) continue;

            int charge = chargeAfter(item.getAttributeHandler().getThunderCharge(), amount, speed, tier.get().capacity());
            if (charge >= tier.get().capacity()) {
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
