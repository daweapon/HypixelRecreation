package dev.daweapon.type.skyblockgeneric.garden.visitor;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Getter
public final class VisitorData {
    public static final int MAX_VISIBLE = 5;
    public static final int MAX_PRESENT = 6;

    @Getter
    public static final class Present {
        private final String id;
        private final VisitorOffer offer;
        private final long arrivedAt;
        @Setter
        private boolean hidden;

        public Present(String id, VisitorOffer offer, long arrivedAt, boolean hidden) {
            this.id = id;
            this.offer = offer;
            this.arrivedAt = arrivedAt;
            this.hidden = hidden;
        }
    }

    private final List<Present> present = new ArrayList<>();
    private final Map<String, int[]> log = new TreeMap<>();
    private final Set<String> talkedTo = new LinkedHashSet<>();
    @Setter
    private long nextVisitorAt;
    @Setter
    private int uniqueTier;
    @Setter
    private int acceptedTier;
    @Setter
    private @Nullable String forcedNext;

    public List<Present> visible() {
        return present.stream().filter(visitor -> !visitor.isHidden()).toList();
    }

    public @Nullable Present find(String id) {
        return present.stream().filter(visitor -> visitor.getId().equals(id)).findFirst().orElse(null);
    }

    public int timesVisited(String id) {
        return log.getOrDefault(id, new int[2])[0];
    }

    public int offersAccepted(String id) {
        return log.getOrDefault(id, new int[2])[1];
    }

    public void recordVisit(String id) {
        log.computeIfAbsent(id, key -> new int[2])[0]++;
    }

    public void recordAccepted(String id) {
        log.computeIfAbsent(id, key -> new int[2])[1]++;
    }

    public void markServed(String id) {
        int[] counts = log.computeIfAbsent(id, key -> new int[2]);
        counts[0] = Math.max(counts[0], 1);
        counts[1] = Math.max(counts[1], 1);
    }

    public int uniqueServed() {
        return (int) log.values().stream().filter(counts -> counts[1] > 0).count();
    }

    public int totalAccepted() {
        return log.values().stream().mapToInt(counts -> counts[1]).sum();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        JSONArray visitors = new JSONArray();
        for (Present visitor : present) {
            visitors.put(new JSONObject().put("id", visitor.getId()).put("offer", visitor.getOffer().toJson())
                    .put("arrivedAt", visitor.getArrivedAt()).put("hidden", visitor.isHidden()));
        }
        JSONObject logJson = new JSONObject();
        log.forEach((id, counts) -> logJson.put(id, new JSONArray(new int[]{counts[0], counts[1]})));
        json.put("present", visitors).put("log", logJson).put("talkedTo", new JSONArray(talkedTo))
                .put("nextVisitorAt", nextVisitorAt).put("uniqueTier", uniqueTier).put("acceptedTier", acceptedTier);
        if (forcedNext != null) json.put("forcedNext", forcedNext);
        return json;
    }

    public static VisitorData fromJson(@Nullable JSONObject json) {
        VisitorData data = new VisitorData();
        if (json == null) return data;
        for (Object entry : json.optJSONArray("present") == null ? new JSONArray() : json.getJSONArray("present")) {
            JSONObject visitor = (JSONObject) entry;
            data.present.add(new Present(visitor.getString("id"), VisitorOffer.fromJson(visitor.getJSONObject("offer")),
                    visitor.getLong("arrivedAt"), visitor.optBoolean("hidden", false)));
        }
        JSONObject logJson = json.optJSONObject("log");
        if (logJson != null) {
            for (String id : logJson.keySet()) {
                JSONArray counts = logJson.getJSONArray(id);
                data.log.put(id, new int[]{counts.getInt(0), counts.getInt(1)});
            }
        }
        if (json.optJSONArray("talkedTo") != null) {
            json.getJSONArray("talkedTo").forEach(name -> data.talkedTo.add((String) name));
        }
        data.nextVisitorAt = json.optLong("nextVisitorAt", 0);
        data.uniqueTier = json.optInt("uniqueTier", 0);
        data.acceptedTier = json.optInt("acceptedTier", 0);
        data.forcedNext = json.optString("forcedNext", null);
        return data;
    }
}
