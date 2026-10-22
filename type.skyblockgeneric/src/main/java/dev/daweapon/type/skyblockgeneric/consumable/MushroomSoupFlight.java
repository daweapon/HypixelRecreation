package dev.daweapon.type.skyblockgeneric.consumable;

import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.data.datapoints.DatapointMapStringLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetEquipEffects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentHashMap;

public final class MushroomSoupFlight {
    public static final String MAGICAL = "MAGIC_MUSHROOM_SOUP";
    public static final String MYSTICAL = "SUPER_MAGIC_MUSHROOM_SOUP";
    public static final long MAGICAL_MILLIS = 2L * 60_000;
    public static final long MYSTICAL_MILLIS = 200L * 60_000;
    private static final String STORE_KEY = "MUSHROOM_SOUP_FLIGHT_REMAINING";
    private static final int[] WARNING_SECONDS = {60, 30, 10, 5, 4, 3, 2, 1};
    private static final String[] FLAVOURS = {"Hmm... tasty!", "I feel like I can fly!", "What was in that soup?"};

    private static final Map<UUID, Long> LAST_TICK = new ConcurrentHashMap<>();

    private MushroomSoupFlight() {
    }

    public static void registerInteractables() {
        register(MAGICAL, MAGICAL_MILLIS, "2");
        register(MYSTICAL, MYSTICAL_MILLIS, "200");
    }

    public static long remainingMillis(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return 0;
        return TimedConsumables.expirations(player).getOrDefault(STORE_KEY, 0L);
    }

    public static boolean grantsFlight(SkyBlockPlayer player) {
        return HypixelConst.isPrivateWorldServer() && remainingMillis(player) > 0;
    }

    public static boolean protectsFromFall(SkyBlockPlayer player) {
        return grantsFlight(player);
    }

    public static List<Integer> crossedWarnings(long previousMillis, long currentMillis) {
        List<Integer> crossed = new ArrayList<>();
        for (int seconds : WARNING_SECONDS) {
            long threshold = seconds * 1000L;
            if (previousMillis > threshold && currentMillis <= threshold) crossed.add(seconds);
        }
        return crossed;
    }

    public static void tick(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        Long last = LAST_TICK.put(player.getUuid(), now);
        long remaining = remainingMillis(player);
        if (remaining <= 0 || last == null || !HypixelConst.isPrivateWorldServer()) return;

        long updated = Math.max(0, remaining - (now - last));
        store(player, updated);
        for (int seconds : crossedWarnings(remaining, updated)) {
            player.sendMessage("<c>Mushroom Soup Flight expires in <e>" + seconds + "s");
        }
        if (updated == 0) {
            player.sendMessage("<c>Mushroom Soup Flight has expired!");
            PetEquipEffects.apply(player);
        }
    }

    public static void release(UUID player) {
        LAST_TICK.remove(player);
    }

    private static void register(String id, long millis, String minutes) {
        InteractableRegistry.register(id, InteractableItemConfig.builder().rightClickHandler((player, item) -> {
            if (!HypixelConst.isPrivateWorldServer()) {
                player.sendMessage("<c>You can only drink this on a Private Island or in the Garden!");
                return;
            }
            boolean extending = remainingMillis(player) > 0;
            store(player, remainingMillis(player) + millis);
            TimedConsumables.consumeHeld(player, item);
            LAST_TICK.put(player.getUuid(), System.currentTimeMillis());
            player.sendMessage("<e><o>" + FLAVOURS[ThreadLocalRandom.current().nextInt(FLAVOURS.length)]);
            player.sendMessage(extending
                    ? "<a>Your flight has been extended for <c>" + minutes + " <a>extra minutes."
                    : "<a>You can now fly for <c>" + minutes + " <a>minutes.");
            PetEquipEffects.apply(player);
        }).build());
    }

    private static void store(SkyBlockPlayer player, long remaining) {
        Map<String, Long> updated = new HashMap<>(TimedConsumables.expirations(player));
        if (remaining > 0) updated.put(STORE_KEY, remaining);
        else updated.remove(STORE_KEY);
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.TIMED_CONSUMABLES, DatapointMapStringLong.class).setValue(updated);
    }
}
