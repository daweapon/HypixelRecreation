package net.swofty.type.skyblockgeneric.bazaar;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// The instant buys and instant sells made on a profile, newest first, shown by the Bazaar History button.
public final class BazaarHistory {
    public static final int CAPACITY = 20;

    private static final Map<UUID, Deque<Entry>> BY_PROFILE = new ConcurrentHashMap<>();

    private BazaarHistory() {
    }

    public enum Kind {
        INSTANT_BUY("Instant Buy", NamedTextColor.GREEN),
        INSTANT_SELL("Instant Sell", NamedTextColor.GOLD);

        private final String label;
        private final TextColor colour;

        Kind(String label, TextColor colour) {
            this.label = label;
            this.colour = colour;
        }

        public String label() {
            return label;
        }

        public TextColor colour() {
            return colour;
        }
    }

    public record Entry(long at, Kind kind, BazaarProduct item, int amount, double coins, Text player) {
        public Entry(long at, Kind kind, ItemType item, int amount, double coins, Text player) {
            this(at, kind, BazaarProducts.of(item), amount, coins, player);
        }
    }

    public static void record(UUID profile, Kind kind, ItemType item, int amount, double coins, Text player) {
        record(profile, kind, BazaarProducts.of(item), amount, coins, player);
    }

    public static void record(UUID profile, Kind kind, BazaarProduct item, int amount, double coins, Text player) {
        record(profile, new Entry(System.currentTimeMillis(), kind, item, amount, coins, player));
    }

    public static void record(UUID profile, Entry entry) {
        Deque<Entry> entries = BY_PROFILE.computeIfAbsent(profile, key -> new ArrayDeque<>());
        synchronized (entries) {
            entries.addFirst(entry);
            while (entries.size() > CAPACITY) {
                entries.removeLast();
            }
        }
    }

    public static List<Entry> entries(UUID profile) {
        Deque<Entry> entries = BY_PROFILE.get(profile);
        if (entries == null) {
            return List.of();
        }
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    public static String ago(long millis) {
        long seconds = Math.max(0, millis / 1000);
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "m";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "h";
        }
        return hours / 24 + "d";
    }
}
