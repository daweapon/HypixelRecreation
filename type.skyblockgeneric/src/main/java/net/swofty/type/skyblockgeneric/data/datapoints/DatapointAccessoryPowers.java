package net.swofty.type.skyblockgeneric.data.datapoints;

import lombok.Getter;
import lombok.Setter;
import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.accessory.power.AccessoryPowerType;
import net.swofty.type.skyblockgeneric.accessory.power.TuningStat;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Maxwell's per-profile state: powers learned from Power Stones, the selected power, the tuning
 * points assigned to each stat, the saved tuning templates and how many template slots are unlocked.
 * Unknown names are skipped, so a removed power or stat never breaks a save.
 */
public class DatapointAccessoryPowers extends SkyBlockDatapoint<DatapointAccessoryPowers.PowerData> {
    public static final Serializer<PowerData> serializer = new Serializer<>() {
        @Override
        public String serialize(PowerData value) {
            JSONArray learned = new JSONArray();
            value.getLearned().forEach(power -> learned.put(power.name()));
            JSONObject templates = new JSONObject();
            value.getTemplates().forEach((slot, tuning) -> templates.put(String.valueOf(slot), tuning(tuning)));
            return new JSONObject()
                    .put("learned", learned)
                    .put("selected", value.isPowerChosen()
                            ? (value.getSelected() == null ? NO_POWER : value.getSelected().name())
                            : JSONObject.NULL)
                    .put("tuning", tuning(value.getTuning()))
                    .put("templates", templates)
                    .put("templates_unlocked", value.getTemplatesUnlocked())
                    .toString();
        }

        @Override
        public PowerData deserialize(String json) {
            PowerData data = new PowerData();
            if (json == null || json.isBlank()) return data;
            JSONObject root;
            try {
                root = new JSONObject(json);
            } catch (JSONException e) {
                return data;
            }
            JSONArray learned = root.optJSONArray("learned");
            if (learned != null) {
                for (int i = 0; i < learned.length(); i++) {
                    AccessoryPowerType power = AccessoryPowerType.byName(learned.optString(i));
                    if (power != null) data.getLearned().add(power);
                }
            }
            if (root.has("selected") && !root.isNull("selected")) {
                String selected = root.optString("selected");
                data.setPowerChosen(true);
                data.setSelected(AccessoryPowerType.byName(selected));
            }
            data.getTuning().putAll(readTuning(root.optJSONObject("tuning")));
            JSONObject templates = root.optJSONObject("templates");
            if (templates != null) {
                for (String key : templates.keySet()) {
                    try {
                        data.getTemplates().put(Integer.parseInt(key), readTuning(templates.optJSONObject(key)));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            data.setTemplatesUnlocked(Math.max(1, root.optInt("templates_unlocked", 1)));
            return data;
        }

        @Override
        public PowerData clone(PowerData value) {
            return deserialize(serialize(value));
        }
    };

    public static final String NO_POWER = "NONE";

    public DatapointAccessoryPowers(String key, PowerData value) {
        super(key, value, serializer);
    }

    public DatapointAccessoryPowers(String key) {
        super(key, new PowerData(), serializer);
    }

    private static JSONObject tuning(Map<TuningStat, Integer> tuning) {
        JSONObject object = new JSONObject();
        tuning.forEach((stat, points) -> {
            if (points > 0) object.put(stat.name(), points);
        });
        return object;
    }

    private static Map<TuningStat, Integer> readTuning(@Nullable JSONObject object) {
        Map<TuningStat, Integer> tuning = new EnumMap<>(TuningStat.class);
        if (object == null) return tuning;
        for (String key : object.keySet()) {
            try {
                int points = object.getInt(key);
                if (points > 0) tuning.put(TuningStat.valueOf(key), points);
            } catch (IllegalArgumentException | JSONException ignored) {
            }
        }
        return tuning;
    }

    @Getter
    @Setter
    public static class PowerData {
        private final Set<AccessoryPowerType> learned = EnumSet.noneOf(AccessoryPowerType.class);
        private @Nullable AccessoryPowerType selected;
        private boolean powerChosen;
        private final Map<TuningStat, Integer> tuning = new EnumMap<>(TuningStat.class);
        private final Map<Integer, Map<TuningStat, Integer>> templates = new HashMap<>();
        private int templatesUnlocked = 1;

        public int assignedPoints() {
            return tuning.values().stream().mapToInt(Integer::intValue).sum();
        }

        public int pointsIn(TuningStat stat) {
            return tuning.getOrDefault(stat, 0);
        }
    }
}
