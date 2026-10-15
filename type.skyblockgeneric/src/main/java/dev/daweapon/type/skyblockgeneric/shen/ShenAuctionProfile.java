package dev.daweapon.type.skyblockgeneric.shen;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** A profile's bids in Shen's auctions, and the items and refunds waiting to be claimed. */
@Getter
public class ShenAuctionProfile {
    private static final int KEPT_ENTRIES = 48;

    private final List<Entry> entries = new ArrayList<>();

    public Optional<Entry> entryFor(ShenAuction auction, ShenAuctionItem item) {
        return entries.stream().filter(entry -> entry.year == auction.year() && entry.item == item).findFirst();
    }

    public Entry bidOn(ShenAuction auction, ShenAuctionItem item, boolean ironman) {
        return entryFor(auction, item).orElseGet(() -> {
            Entry entry = new Entry(auction.year(), item, ironman, 0, null, false);
            entries.add(entry);
            return entry;
        });
    }

    public List<Entry> claimable(Outcome outcome) {
        return entries.stream().filter(entry -> entry.outcome == outcome && !entry.claimed).toList();
    }

    public void trimHistory() {
        while (entries.size() > KEPT_ENTRIES) {
            Entry oldestClaimed = entries.stream().filter(Entry::isClaimed).findFirst().orElse(null);
            if (oldestClaimed == null) return;
            entries.remove(oldestClaimed);
        }
    }

    public enum Outcome {
        WON,
        LOST
    }

    @Getter
    @Setter
    public static class Entry {
        private final int year;
        private final ShenAuctionItem item;
        private final boolean ironman;
        private long bid;
        private @Nullable Outcome outcome;
        private boolean claimed;

        public Entry(int year, ShenAuctionItem item, boolean ironman, long bid, @Nullable Outcome outcome, boolean claimed) {
            this.year = year;
            this.item = item;
            this.ironman = ironman;
            this.bid = bid;
            this.outcome = outcome;
            this.claimed = claimed;
        }

        public ShenAuction auction() {
            return new ShenAuction(year);
        }

        public boolean isPending() {
            return outcome == null;
        }

        JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("year", year);
            json.put("item", item.name());
            json.put("ironman", ironman);
            json.put("bid", bid);
            if (outcome != null) json.put("outcome", outcome.name());
            json.put("claimed", claimed);
            return json;
        }

        static @Nullable Entry fromJson(JSONObject json) {
            try {
                return new Entry(json.getInt("year"), ShenAuctionItem.valueOf(json.getString("item")),
                        json.optBoolean("ironman", false), json.optLong("bid", 0),
                        json.has("outcome") ? Outcome.valueOf(json.getString("outcome")) : null,
                        json.optBoolean("claimed", false));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        Entry copy() {
            return new Entry(year, item, ironman, bid, outcome, claimed);
        }
    }

    public String serialize() {
        JSONArray array = new JSONArray();
        entries.forEach(entry -> array.put(entry.toJson()));
        return new JSONObject().put("entries", array).toString();
    }

    public static ShenAuctionProfile deserialize(@Nullable String serialized) {
        ShenAuctionProfile profile = new ShenAuctionProfile();
        if (serialized == null || serialized.isEmpty() || serialized.equals("null")) return profile;
        JSONArray array = new JSONObject(serialized).optJSONArray("entries");
        if (array == null) return profile;
        for (int i = 0; i < array.length(); i++) {
            Entry entry = Entry.fromJson(array.getJSONObject(i));
            if (entry != null) profile.entries.add(entry);
        }
        return profile;
    }

    public ShenAuctionProfile copy() {
        ShenAuctionProfile copy = new ShenAuctionProfile();
        entries.forEach(entry -> copy.entries.add(entry.copy()));
        return copy;
    }
}
