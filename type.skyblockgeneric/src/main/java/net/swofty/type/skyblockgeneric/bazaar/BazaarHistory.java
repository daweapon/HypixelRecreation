package net.swofty.type.skyblockgeneric.bazaar;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBazaarHistory;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// The instant buys and instant sells made on a profile, newest first, shown by the Bazaar History button.
public final class BazaarHistory {
    public static final int CAPACITY = 20;

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

    public static void record(SkyBlockPlayer player, Kind kind, BazaarProduct item, int amount, double coins, Text name) {
        record(player, new Entry(System.currentTimeMillis(), kind, item, amount, coins, name));
    }

    public static void record(SkyBlockPlayer player, Entry entry) {
        DatapointBazaarHistory history = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.BAZAAR_HISTORY, DatapointBazaarHistory.class);
        history.setValue(withLatest(history.getValue(), entry));
    }

    public static List<Entry> entries(SkyBlockPlayer player) {
        return List.copyOf(player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.BAZAAR_HISTORY, DatapointBazaarHistory.class).getValue());
    }

    public static List<Entry> withLatest(List<Entry> entries, Entry entry) {
        List<Entry> updated = new ArrayList<>(Math.min(entries.size() + 1, CAPACITY));
        updated.add(entry);
        updated.addAll(entries);
        return new ArrayList<>(updated.subList(0, Math.min(updated.size(), CAPACITY)));
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
