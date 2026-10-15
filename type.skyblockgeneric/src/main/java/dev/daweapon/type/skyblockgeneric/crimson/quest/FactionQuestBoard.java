package dev.daweapon.type.skyblockgeneric.crimson.quest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * One faction's Town Board for one UTC day: five quest slots with their progress. For Rescue Missions the
 * progress is the mission stage ({@link RescueStage}).
 */
@Getter
@AllArgsConstructor
public class FactionQuestBoard {
    private final long day;
    private final List<Slot> slots;

    public @Nullable Slot slot(int index) {
        return index >= 0 && index < slots.size() ? slots.get(index) : null;
    }

    public JSONObject toJson() {
        JSONArray array = new JSONArray();
        for (Slot slot : slots) {
            array.put(new JSONObject()
                    .put("id", slot.getQuestId())
                    .put("progress", slot.getProgress())
                    .put("completed", slot.isCompleted())
                    .put("claimed", slot.isClaimed())
                    .putOpt("reward", slot.getReward() == null ? null : slot.getReward().itemId())
                    .put("reward_amount", slot.getReward() == null ? 0 : slot.getReward().amount()));
        }
        return new JSONObject().put("day", day).put("slots", array);
    }

    public static FactionQuestBoard fromJson(JSONObject json) {
        List<Slot> slots = new ArrayList<>();
        JSONArray array = json.optJSONArray("slots");
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject slot = array.getJSONObject(i);
                String id = slot.optString("id", "");
                if (FactionQuestCatalogue.get(id) == null) continue;
                Slot loaded = new Slot(id, slot.optInt("progress", 0), slot.optBoolean("completed", false),
                        slot.optBoolean("claimed", false));
                String reward = slot.optString("reward", "");
                int rewardAmount = slot.optInt("reward_amount", 0);
                if (!reward.isEmpty() && rewardAmount > 0) loaded.setReward(new FactionQuestTier.RewardItem(reward, rewardAmount));
                slots.add(loaded);
            }
        }
        return new FactionQuestBoard(json.optLong("day", -1), slots);
    }

    @Getter
    @Setter
    public static class Slot {
        private final String questId;
        private int progress;
        private boolean completed;
        private boolean claimed;
        private @Nullable FactionQuestTier.RewardItem reward;

        public Slot(String questId, int progress, boolean completed, boolean claimed) {
            this.questId = questId;
            this.progress = progress;
            this.completed = completed;
            this.claimed = claimed;
        }

        public FactionQuest quest() {
            return FactionQuestCatalogue.get(questId);
        }

        public String statusSymbol() {
            return completed ? "<a>✔" : "<c>✖";
        }
    }
}
