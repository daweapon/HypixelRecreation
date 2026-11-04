package dev.daweapon.type.skyblockgeneric.theend;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The server-wide Zealot counter that wakes the End Stone Protector. Zealots killed while the Protector is up
 * don't count; each player's share (capped at 100) feeds their loot Quality.
 */
public final class ProtectorProgress {
    public static final int KILLS_TO_SUMMON = 5_000;
    public static final int KILLS_PER_STAGE = 1_000;
    public static final int MAX_QUALITY_ZEALOTS = 100;

    public enum Stage {
        RESTING("<8>Resting"),
        DORMANT("<7>Dormant"),
        AGITATED("<f>Agitated"),
        DISTURBED("<e>Disturbed"),
        AWAKENING("<6>Awakening"),
        SUMMONED("<c>Summoned");

        private final String display;

        Stage(String display) {
            this.display = display;
        }

        public String display() {
            return display;
        }
    }

    private static final AtomicInteger KILLS = new AtomicInteger();
    private static final Map<UUID, Integer> CONTRIBUTED = new ConcurrentHashMap<>();
    private static volatile boolean summoned;

    private ProtectorProgress() {
    }

    public static void recordZealot(UUID player) {
        if (summoned) return;
        KILLS.updateAndGet(kills -> Math.min(KILLS_TO_SUMMON, kills + 1));
        CONTRIBUTED.merge(player, 1, (old, add) -> Math.min(MAX_QUALITY_ZEALOTS, old + add));
    }

    public static int kills() {
        return KILLS.get();
    }

    public static Stage stage() {
        return stageFor(summoned ? KILLS_TO_SUMMON : KILLS.get());
    }

    public static Stage stageFor(int kills) {
        int index = Math.clamp(kills / KILLS_PER_STAGE, 0, Stage.values().length - 1);
        return Stage.values()[index];
    }

    public static boolean ready() {
        return !summoned && KILLS.get() >= KILLS_TO_SUMMON;
    }

    public static boolean summoned() {
        return summoned;
    }

    public static void markSummoned() {
        summoned = true;
    }

    public static int contributed(UUID player) {
        return CONTRIBUTED.getOrDefault(player, 0);
    }

    public static Map<UUID, Integer> contributions() {
        return Map.copyOf(CONTRIBUTED);
    }

    public static void forceKills(int kills) {
        KILLS.set(Math.clamp(kills, 0, KILLS_TO_SUMMON));
    }

    public static void reset() {
        summoned = false;
        KILLS.set(0);
        CONTRIBUTED.clear();
    }
}
