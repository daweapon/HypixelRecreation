package dev.daweapon.type.skyblockgeneric.starlyn;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Getter
public class StarlynProfile {
    public static final int KEPT_ENTRIES = 112;

    private final Map<StarlynSister, SisterData> sisters = new EnumMap<>(StarlynSister.class);

    public SisterData sister(StarlynSister sister) {
        return sisters.computeIfAbsent(sister, key -> new SisterData());
    }

    @Getter
    @Setter
    public static class SisterData {
        private boolean met;
        private boolean participated;
        private long couponsEarned;
        private int milestoneGranted = 1;
        private long dailyDay = -1;
        private int dailyClaimed;
        private final Map<String, Integer> purchases = new LinkedHashMap<>();
        private final Map<StarlynTree, Long> personalBests = new EnumMap<>(StarlynTree.class);
        private final Set<StarlynBracket> reached = EnumSet.noneOf(StarlynBracket.class);
        private final List<Entry> entries = new ArrayList<>();

        public int purchased(String id) {
            return purchases.getOrDefault(id, 0);
        }

        public void setPurchased(String id, int level) {
            purchases.put(id, level);
        }

        public long personalBest(StarlynTree tree) {
            return personalBests.getOrDefault(tree, 0L);
        }

        public Optional<Entry> entryFor(long day) {
            return entries.stream().filter(entry -> entry.day == day).findFirst();
        }

        public List<Entry> claimable() {
            return entries.stream().filter(Entry::isClaimable).toList();
        }

        public List<Entry> finishedNewestFirst() {
            return entries.stream().filter(Entry::isFinalised)
                    .sorted((a, b) -> Long.compare(b.day, a.day)).toList();
        }

        public void trimHistory() {
            List<Entry> claimed = new ArrayList<>(entries.stream().filter(Entry::isClaimed).toList());
            claimed.sort((a, b) -> Long.compare(a.day, b.day));
            int excess = entries.size() - KEPT_ENTRIES;
            for (int i = 0; i < excess && i < claimed.size(); i++) entries.remove(claimed.get(i));
        }

        public boolean reachedBracket(StarlynBracket bracket) {
            return reached.stream().anyMatch(other -> other.atLeast(bracket));
        }

        SisterData copy() {
            SisterData copy = new SisterData();
            copy.met = met;
            copy.participated = participated;
            copy.couponsEarned = couponsEarned;
            copy.milestoneGranted = milestoneGranted;
            copy.dailyDay = dailyDay;
            copy.dailyClaimed = dailyClaimed;
            copy.purchases.putAll(purchases);
            copy.personalBests.putAll(personalBests);
            copy.reached.addAll(reached);
            entries.forEach(entry -> copy.entries.add(entry.copy()));
            return copy;
        }
    }

    @Getter
    @Setter
    public static class Entry {
        private final long day;
        private double points;
        private final Map<StarlynTree, Long> logs = new EnumMap<>(StarlynTree.class);
        private @Nullable StarlynBracket bracket;
        private boolean finalised;
        private boolean claimed;

        public Entry(long day) {
            this.day = day;
        }

        public long score() {
            return (long) Math.floor(points);
        }

        public long logs(StarlynTree tree) {
            return logs.getOrDefault(tree, 0L);
        }

        public boolean isClaimable() {
            return finalised && !claimed && bracket != null;
        }

        Entry copy() {
            Entry copy = new Entry(day);
            copy.points = points;
            copy.logs.putAll(logs);
            copy.bracket = bracket;
            copy.finalised = finalised;
            copy.claimed = claimed;
            return copy;
        }
    }

    public String serialize() {
        JSONObject json = new JSONObject();
        sisters.forEach((sister, data) -> json.put(sister.name(), serialize(data)));
        return json.toString();
    }

    private static JSONObject serialize(SisterData data) {
        JSONObject json = new JSONObject();
        json.put("met", data.met);
        json.put("participated", data.participated);
        json.put("couponsEarned", data.couponsEarned);
        json.put("milestoneGranted", data.milestoneGranted);
        json.put("dailyDay", data.dailyDay);
        json.put("dailyClaimed", data.dailyClaimed);
        json.put("purchases", new JSONObject(data.purchases));
        JSONObject bests = new JSONObject();
        data.personalBests.forEach((tree, best) -> bests.put(tree.name(), best));
        json.put("personalBests", bests);
        JSONArray reached = new JSONArray();
        data.reached.forEach(bracket -> reached.put(bracket.name()));
        json.put("reached", reached);
        JSONArray entries = new JSONArray();
        data.entries.forEach(entry -> entries.put(serialize(entry)));
        json.put("entries", entries);
        return json;
    }

    private static JSONObject serialize(Entry entry) {
        JSONObject json = new JSONObject();
        json.put("day", entry.day);
        json.put("points", entry.points);
        JSONObject logs = new JSONObject();
        entry.logs.forEach((tree, amount) -> logs.put(tree.name(), amount));
        json.put("logs", logs);
        if (entry.bracket != null) json.put("bracket", entry.bracket.name());
        json.put("finalised", entry.finalised);
        json.put("claimed", entry.claimed);
        return json;
    }

    public static StarlynProfile deserialize(@Nullable String serialized) {
        StarlynProfile profile = new StarlynProfile();
        if (serialized == null || serialized.isEmpty() || serialized.equals("null")) return profile;
        JSONObject json = new JSONObject(serialized);
        for (StarlynSister sister : StarlynSister.values()) {
            JSONObject data = json.optJSONObject(sister.name());
            if (data != null) profile.sisters.put(sister, deserializeSister(data));
        }
        return profile;
    }

    private static SisterData deserializeSister(JSONObject json) {
        SisterData data = new SisterData();
        data.met = json.optBoolean("met", false);
        data.participated = json.optBoolean("participated", false);
        data.couponsEarned = json.optLong("couponsEarned", 0);
        data.milestoneGranted = json.optInt("milestoneGranted", 1);
        data.dailyDay = json.optLong("dailyDay", -1);
        data.dailyClaimed = json.optInt("dailyClaimed", 0);
        JSONObject purchases = json.optJSONObject("purchases");
        if (purchases != null) purchases.keySet().forEach(key -> data.purchases.put(key, purchases.getInt(key)));
        JSONObject bests = json.optJSONObject("personalBests");
        if (bests != null) bests.keySet().forEach(key -> data.personalBests.put(StarlynTree.valueOf(key), bests.getLong(key)));
        JSONArray reached = json.optJSONArray("reached");
        if (reached != null) for (int i = 0; i < reached.length(); i++) data.reached.add(StarlynBracket.valueOf(reached.getString(i)));
        JSONArray entries = json.optJSONArray("entries");
        if (entries != null) for (int i = 0; i < entries.length(); i++) data.entries.add(deserializeEntry(entries.getJSONObject(i)));
        return data;
    }

    private static Entry deserializeEntry(JSONObject json) {
        Entry entry = new Entry(json.getLong("day"));
        entry.points = json.optDouble("points", 0);
        JSONObject logs = json.optJSONObject("logs");
        if (logs != null) logs.keySet().forEach(key -> entry.logs.put(StarlynTree.valueOf(key), logs.getLong(key)));
        String bracket = json.optString("bracket", null);
        entry.bracket = bracket == null ? null : StarlynBracket.valueOf(bracket);
        entry.finalised = json.optBoolean("finalised", false);
        entry.claimed = json.optBoolean("claimed", false);
        return entry;
    }

    public StarlynProfile copy() {
        StarlynProfile copy = new StarlynProfile();
        sisters.forEach((sister, data) -> copy.sisters.put(sister, data.copy()));
        return copy;
    }
}
