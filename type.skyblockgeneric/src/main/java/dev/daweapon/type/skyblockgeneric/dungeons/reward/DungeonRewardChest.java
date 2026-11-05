package dev.daweapon.type.skyblockgeneric.dungeons.reward;

import dev.daweapon.dungeons.catacombs.reward.DungeonChestTier;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** One rolled chest of a run: its contents, its total coin cost, and whether it was bought or rerolled. */
public record DungeonRewardChest(DungeonChestTier tier, List<DungeonReward> rewards, long cost, boolean opened,
                                 boolean rerolled) {

    public DungeonRewardChest open() {
        return new DungeonRewardChest(tier, rewards, cost, true, rerolled);
    }

    public DungeonRewardChest rerolledTo(DungeonRewardChest fresh) {
        return new DungeonRewardChest(tier, fresh.rewards(), fresh.cost(), false, true);
    }

    public JSONObject toJson() {
        JSONArray array = new JSONArray();
        rewards.forEach(reward -> array.put(reward.toJson()));
        JSONObject json = new JSONObject().put("tier", tier.name()).put("cost", cost).put("rewards", array);
        if (opened) json.put("opened", true);
        if (rerolled) json.put("rerolled", true);
        return json;
    }

    public static DungeonRewardChest fromJson(JSONObject json) {
        List<DungeonReward> rewards = new ArrayList<>();
        JSONArray array = json.optJSONArray("rewards");
        if (array != null) for (int i = 0; i < array.length(); i++) rewards.add(DungeonReward.fromJson(array.getJSONObject(i)));
        return new DungeonRewardChest(DungeonChestTier.valueOf(json.getString("tier")), List.copyOf(rewards),
                json.optLong("cost", 0), json.optBoolean("opened", false), json.optBoolean("rerolled", false));
    }
}
