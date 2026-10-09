package net.swofty.type.skyblockgeneric.crimson.kuudra.reward;

import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A won Kuudra run's two reward chests, rolled when the run ends. Only one of them can ever be opened. */
public record KuudraRun(String id, KuudraTier tier, long completedAt, List<KuudraReward> free, List<KuudraReward> paid,
                        @Nullable KuudraChest opened) {
    public static final Duration LIFETIME = Duration.ofHours(72);

    public static KuudraRun create(KuudraTier tier, long completedAt, List<KuudraReward> free, List<KuudraReward> paid) {
        return new KuudraRun(UUID.randomUUID().toString(), tier, completedAt, List.copyOf(free), List.copyOf(paid), null);
    }

    public List<KuudraReward> rewards(KuudraChest chest) {
        return chest == KuudraChest.FREE ? free : paid;
    }

    public boolean isOpened() {
        return opened != null;
    }

    public long expiresAt() {
        return completedAt + LIFETIME.toMillis();
    }

    public boolean isExpired(long now) {
        return now >= expiresAt();
    }

    public KuudraRun open(KuudraChest chest) {
        return new KuudraRun(id, tier, completedAt, free, paid, chest);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject()
                .put("id", id)
                .put("tier", tier.name())
                .put("completedAt", completedAt)
                .put("free", rewardsJson(free))
                .put("paid", rewardsJson(paid));
        if (opened != null) json.put("opened", opened.name());
        return json;
    }

    public static KuudraRun fromJson(JSONObject json) {
        return new KuudraRun(json.getString("id"), KuudraTier.valueOf(json.getString("tier")), json.getLong("completedAt"),
                rewards(json.optJSONArray("free")), rewards(json.optJSONArray("paid")),
                json.has("opened") ? KuudraChest.valueOf(json.getString("opened")) : null);
    }

    private static JSONArray rewardsJson(List<KuudraReward> rewards) {
        JSONArray array = new JSONArray();
        rewards.forEach(reward -> array.put(reward.toJson()));
        return array;
    }

    private static List<KuudraReward> rewards(@Nullable JSONArray array) {
        List<KuudraReward> rewards = new ArrayList<>();
        if (array != null) for (int i = 0; i < array.length(); i++) rewards.add(KuudraReward.fromJson(array.getJSONObject(i)));
        return List.copyOf(rewards);
    }
}
