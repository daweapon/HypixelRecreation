package net.swofty.type.skyblockgeneric.gifts;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeSnowballAmmo;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Frosty the Snow Cannon, Snow Blaster and Howitzer: each holds Snowballs as ammo (stored on the
 * item), shoots one per right-click on Jerry's Workshop, refills from picked-up Snowballs or from
 * Frosty at 1 coin per Snowball, and never runs dry for a wearer of the full Snow Suit.
 */
public final class SnowCannon {
    public static final String HANDLER_ID = "SNOW_CANNON";
    public static final int COINS_PER_SNOWBALL = 1;
    private static final long SHOT_COOLDOWN_MS = 250;
    private static final int INVENTORY_SLOTS = 36;
    private static final Map<UUID, Long> lastShot = new ConcurrentHashMap<>();

    public enum Tier {
        CANNON(ItemType.SNOW_CANNON, 1_000, 1_000, 1.4, "Snow Cannon"),
        BLASTER(ItemType.SNOW_BLASTER, 2_000, 0, 2.0, "Snow Blaster"),
        HOWITZER(ItemType.SNOW_HOWITZER, 10_000, 0, 2.6, "Howitzer");

        public final ItemType type;
        public final int capacity;
        public final int startingAmmo;
        public final double speed;
        public final String shortName;

        Tier(ItemType type, int capacity, int startingAmmo, double speed, String shortName) {
            this.type = type;
            this.capacity = capacity;
            this.startingAmmo = startingAmmo;
            this.speed = speed;
            this.shortName = shortName;
        }

        public static @Nullable Tier of(@Nullable ItemType type) {
            for (Tier tier : values()) if (tier.type == type) return tier;
            return null;
        }
    }

    private SnowCannon() {
    }

    public static void registerLore() {
        LoreRegistry.register(HANDLER_ID, new LoreConfig(SnowCannon::lore, null));
    }

    public static void registerInteractables() {
        InteractableRegistry.register(HANDLER_ID, InteractableItemConfig.builder()
                .rightClickHandler(SnowCannon::shoot)
                .leftClickHandler(SnowCannon::showAmmo)
                .build());
    }

    public static int ammo(SkyBlockItem item) {
        Tier tier = Tier.of(item.getAttributeHandler().getPotentialType());
        if (tier == null) return 0;
        int stored = item.getAttributeHandler().getSnowballAmmo();
        return stored == ItemAttributeSnowballAmmo.UNSET ? tier.startingAmmo : stored;
    }

    public static int ammoAfterShot(int ammo, boolean unlimited) {
        return unlimited ? ammo : Math.max(0, ammo - 1);
    }

    public static int acceptedSnowballs(int ammo, int capacity, int offered) {
        return Math.max(0, Math.min(offered, capacity - ammo));
    }

    public static int affordableRefill(int missing, double coins) {
        return (int) Math.max(0, Math.min(missing, Math.floor(coins / COINS_PER_SNOWBALL)));
    }

    public static boolean isOnJerrysWorkshop() {
        return HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_JERRYS_WORKSHOP;
    }

    public static boolean hasAnyCannon(SkyBlockPlayer player) {
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            if (tierAt(player, slot) != null) return true;
        }
        return false;
    }

    public static int absorbSnowballs(SkyBlockPlayer player, int amount) {
        if (!isOnJerrysWorkshop()) return amount;
        int remaining = amount;
        for (int slot = 0; slot < INVENTORY_SLOTS && remaining > 0; slot++) {
            Tier tier = tierAt(player, slot);
            if (tier == null) continue;
            SkyBlockItem cannon = new SkyBlockItem(player.getInventory().getItemStack(slot));
            int ammo = ammo(cannon);
            int accepted = acceptedSnowballs(ammo, tier.capacity, remaining);
            if (accepted == 0) continue;
            setAmmo(player, slot, cannon, ammo + accepted);
            remaining -= accepted;
        }
        return remaining;
    }

    public static int refill(SkyBlockPlayer player) {
        int refilled = 0;
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            Tier tier = tierAt(player, slot);
            if (tier == null) continue;
            SkyBlockItem cannon = new SkyBlockItem(player.getInventory().getItemStack(slot));
            int ammo = ammo(cannon);
            int bought = affordableRefill(tier.capacity - ammo, player.getCoins());
            if (bought == 0) continue;
            player.removeCoins(bought * COINS_PER_SNOWBALL);
            setAmmo(player, slot, cannon, ammo + bought);
            refilled += bought;
        }
        return refilled;
    }

    private static void shoot(SkyBlockPlayer player, SkyBlockItem item) {
        Tier tier = Tier.of(item.getAttributeHandler().getPotentialType());
        if (tier == null) return;
        if (!isOnJerrysWorkshop()) {
            player.sendMessage("<c>You can only use this item on Jerry's Workshop!");
            return;
        }
        long now = System.currentTimeMillis();
        Long last = lastShot.get(player.getUuid());
        if (last != null && now - last < SHOT_COOLDOWN_MS) return;

        boolean unlimited = SnowSuit.hasColdThumb(player);
        int ammo = ammo(item);
        if (ammo <= 0 && !unlimited) {
            player.sendMessage("<c>Your Frosty the " + tier.shortName + " is out of Snowballs! <7>Pick some up or visit <c>Frosty<7>.");
            return;
        }
        lastShot.put(player.getUuid(), now);
        SnowballProjectile.throwAlongLook(player, tier.speed);
        if (!unlimited) setAmmo(player, player.getHeldSlot(), item, ammoAfterShot(ammo, false));
    }

    private static void showAmmo(SkyBlockPlayer player, SkyBlockItem item) {
        Tier tier = Tier.of(item.getAttributeHandler().getPotentialType());
        if (tier == null) return;
        player.sendMessage("<7>Snowballs: <f>" + String.format("%,d", ammo(item)) + "<7>/<f>" + String.format("%,d", tier.capacity)
                + (SnowSuit.hasColdThumb(player) ? " <b>(Unlimited - Cold Thumb)" : ""));
    }

    private static @Nullable Tier tierAt(SkyBlockPlayer player, int slot) {
        ItemStack stack = player.getInventory().getItemStack(slot);
        if (stack.isAir()) return null;
        return Tier.of(new SkyBlockItem(stack).getAttributeHandler().getPotentialType());
    }

    private static void setAmmo(SkyBlockPlayer player, int slot, SkyBlockItem cannon, int ammo) {
        cannon.getAttributeHandler().setSnowballAmmo(ammo);
        player.getInventory().setItemStack(slot, PlayerItemUpdater.playerUpdate(player, cannon.getItemStack()).build());
    }

    private static List<String> lore(SkyBlockItem item, @Nullable SkyBlockPlayer player) {
        Tier tier = Tier.of(item.getAttributeHandler().getPotentialType());
        if (tier == null) return List.of();
        List<String> lore = new ArrayList<>(List.of(
                "<7>Holds and shoots Snowballs. When",
                "<7>you pickup Snowballs, they increase",
                "<7>the ammo held by this item."));
        switch (tier) {
            case BLASTER -> {
                lore.set(2, "<7>the ammo held by this item. Blasts");
                lore.add("<7>Snowballs at a higher velocity than a");
                lore.add("<7>Snow Cannon!");
            }
            case HOWITZER -> {
                lore.set(2, "<7>the ammo held by this item. Blasts");
                lore.add("<7>Snowballs at a higher velocity than a");
                lore.add("<7>Snow Blaster!");
            }
            default -> {
            }
        }
        lore.add("");
        lore.add("<7>Snowballs: <f>" + String.format("%,d", ammo(item)) + "<7>/<f>" + String.format("%,d", tier.capacity));
        lore.add("");
        lore.add("<e>Right click to shoot!");
        return lore;
    }
}
