package dev.daweapon.type.skyblockgeneric.consumable;

import dev.daweapon.type.generic.data.datapoints.DatapointMapStringLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;

public final class SmolderingPolarization {
    public static final String ITEM_ID = "REHEATED_GUMMY_POLAR_BEAR";
    public static final long DURATION_MILLIS = 60L * 60 * 1000;

    private SmolderingPolarization() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register(ITEM_ID, InteractableItemConfig.builder().rightClickHandler((player, item) -> {
            extend(player);
            TimedConsumables.consumeHeld(player, item);
            player.sendMessage("<a>You gained <a>Smoldering Polarization I <a>for <b>1 hour<a>! <8>Consuming more stacks the duration.");
        }).build());
    }

    public static boolean isActive(SkyBlockPlayer player) {
        return TimedConsumables.isActive(player, ITEM_ID);
    }

    private static void extend(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        long base = Math.max(now, TimedConsumables.expiration(player, ITEM_ID));
        Map<String, Long> updated = new HashMap<>(TimedConsumables.expirations(player));
        updated.put(ITEM_ID, base + DURATION_MILLIS);
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.TIMED_CONSUMABLES, DatapointMapStringLong.class).setValue(updated);
    }
}
