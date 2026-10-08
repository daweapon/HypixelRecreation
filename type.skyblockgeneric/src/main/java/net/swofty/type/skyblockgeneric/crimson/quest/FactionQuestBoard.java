package net.swofty.type.skyblockgeneric.crimson.quest;

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
                    .put("claimed", slot.isClaimed()));
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
                slots.add(new Slot(id, slot.optInt("progress", 0), slot.optBoolean("completed", false),
                        slot.optBoolean("claimed", false)));
            }
        }
        return new FactionQuestBoard(json.optLong("day", -1), slots);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Slot {
        private final String questId;
        private int progress;
        private boolean completed;
        private boolean claimed;

        public FactionQuest quest() {
            return FactionQuestCatalogue.get(questId);
        }

        public String statusSymbol() {
            return completed ? "<a>✔" : "<c>✖";
        }
    }
}
