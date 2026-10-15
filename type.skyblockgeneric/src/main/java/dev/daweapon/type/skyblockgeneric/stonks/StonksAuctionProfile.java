package dev.daweapon.type.skyblockgeneric.stonks;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** A profile's bids in Richard's Stonks Auctions and the Stock of Stonks they have earned. */
@Getter
public class StonksAuctionProfile {
    private static final int KEPT_ENTRIES = 24;

    private final List<Entry> entries = new ArrayList<>();

    public Optional<Entry> entryFor(StonksAuction auction) {
        return entries.stream().filter(entry -> entry.auctionId.equals(auction.id())).findFirst();
    }

    public Entry bidOn(StonksAuction auction) {
        return entryFor(auction).orElseGet(() -> {
            Entry entry = new Entry(auction.id(), 0, 0, null, false);
            entries.add(entry);
            return entry;
        });
    }

    public List<Entry> claimable() {
        return entries.stream().filter(Entry::isClaimable).toList();
    }

    public int claimableStocks() {
        return claimable().stream().mapToInt(Entry::getStocks).sum();
    }

    public void trimHistory() {
        while (entries.size() > KEPT_ENTRIES) {
            Entry oldestClaimed = entries.stream().filter(Entry::isClaimed).findFirst().orElse(null);
            if (oldestClaimed == null) return;
            entries.remove(oldestClaimed);
        }
    }

    @Getter
    @Setter
    public static class Entry {
        private final String auctionId;
        private long bid;
        private int placement;
        private @Nullable Integer stocks;
        private boolean claimed;

        public Entry(String auctionId, long bid, int placement, @Nullable Integer stocks, boolean claimed) {
            this.auctionId = auctionId;
            this.bid = bid;
            this.placement = placement;
            this.stocks = stocks;
            this.claimed = claimed;
        }

        public StonksAuction auction() {
            return StonksAuction.fromId(auctionId).orElseThrow();
        }

        public boolean isFinalised() {
            return stocks != null;
        }

        public boolean isClaimable() {
            return isFinalised() && !claimed;
        }

        public int getStocks() {
            return stocks == null ? 0 : stocks;
        }

        JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("auction", auctionId);
            json.put("bid", bid);
            json.put("placement", placement);
            if (stocks != null) json.put("stocks", stocks.intValue());
            json.put("claimed", claimed);
            return json;
        }

        static Entry fromJson(JSONObject json) {
            return new Entry(json.getString("auction"), json.optLong("bid", 0), json.optInt("placement", 0),
                    json.has("stocks") ? json.getInt("stocks") : null, json.optBoolean("claimed", false));
        }

        Entry copy() {
            return new Entry(auctionId, bid, placement, stocks, claimed);
        }
    }

    public String serialize() {
        JSONArray array = new JSONArray();
        entries.forEach(entry -> array.put(entry.toJson()));
        return new JSONObject().put("entries", array).toString();
    }

    public static StonksAuctionProfile deserialize(@Nullable String serialized) {
        StonksAuctionProfile profile = new StonksAuctionProfile();
        if (serialized == null || serialized.isEmpty() || serialized.equals("null")) return profile;
        JSONArray array = new JSONObject(serialized).optJSONArray("entries");
        if (array == null) return profile;
        for (int i = 0; i < array.length(); i++) profile.entries.add(Entry.fromJson(array.getJSONObject(i)));
        return profile;
    }

    public StonksAuctionProfile copy() {
        StonksAuctionProfile copy = new StonksAuctionProfile();
        entries.forEach(entry -> copy.entries.add(entry.copy()));
        return copy;
    }
}
