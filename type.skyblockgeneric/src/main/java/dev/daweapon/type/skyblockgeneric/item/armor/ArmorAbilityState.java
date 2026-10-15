package dev.daweapon.type.skyblockgeneric.item.armor;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ArmorAbilityState {
    private static final Map<UUID, Long> LAST_COMBAT = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> STRIKES = new ConcurrentHashMap<>();
    private static final Map<UUID, List<Long>> HOWL = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> DARKNESS_PAID_UNTIL = new ConcurrentHashMap<>();

    private ArmorAbilityState() {
    }

    public static void touchCombat(SkyBlockPlayer player) {
        LAST_COMBAT.put(player.getUuid(), System.currentTimeMillis());
    }

    public static boolean inCombat(SkyBlockPlayer player, long now) {
        return inCombat(LAST_COMBAT.getOrDefault(player.getUuid(), Long.MIN_VALUE / 2), now);
    }

    public static boolean inCombat(long lastCombat, long now) {
        return now - lastCombat <= ArmorAbilityMath.COMBAT_WINDOW_MILLIS;
    }

    public static int nextStrike(SkyBlockPlayer player) {
        return STRIKES.merge(player.getUuid(), 1, Integer::sum);
    }

    public static void recordHowl(SkyBlockPlayer player, long now) {
        List<Long> expiries = HOWL.computeIfAbsent(player.getUuid(), _ -> new ArrayList<>());
        synchronized (expiries) {
            expiries.removeIf(expiry -> expiry <= now);
            expiries.add(now + ArmorAbilityMath.HOWL_MILLIS);
            while (expiries.size() > ArmorAbilityMath.HOWL_MAX_STACKS) expiries.removeFirst();
        }
    }

    public static int howlStacks(SkyBlockPlayer player, long now) {
        List<Long> expiries = HOWL.get(player.getUuid());
        if (expiries == null) return 0;
        synchronized (expiries) {
            expiries.removeIf(expiry -> expiry <= now);
            return ArmorAbilityMath.howlStacks(expiries.size());
        }
    }

    public static boolean darknessPaid(SkyBlockPlayer player, long now) {
        return DARKNESS_PAID_UNTIL.getOrDefault(player.getUuid(), 0L) > now;
    }

    public static void payDarkness(SkyBlockPlayer player, long now) {
        DARKNESS_PAID_UNTIL.put(player.getUuid(), now + ArmorAbilityMath.DARKNESS_INTERVAL_MILLIS);
    }

    public static boolean darknessActive(boolean sneaking, boolean inCombat, boolean paid) {
        return sneaking && inCombat && paid;
    }

    public static boolean darknessActive(SkyBlockPlayer player, long now) {
        return darknessActive(player.isSneaking(), inCombat(player, now), darknessPaid(player, now));
    }
}
