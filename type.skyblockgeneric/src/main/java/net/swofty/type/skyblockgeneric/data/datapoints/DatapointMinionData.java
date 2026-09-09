package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;

// Persists which minion types/tiers a profile has ever crafted, used to determine minion slot count.
public class DatapointMinionData extends SkyBlockDatapoint<DatapointMinionData.ProfileMinionData> {

    public DatapointMinionData(String key, DatapointMinionData.ProfileMinionData value) {
        super(key, value, new Serializer<>() {
            @Override
            public String serialize(DatapointMinionData.ProfileMinionData value) {
                StringBuilder sb = new StringBuilder();
                sb.append("{");

                for (Map.Entry<String, List<Integer>> entry : value.craftedMinions) {
                    sb.append(entry.getKey()).append(":[");
                    entry.getValue().forEach(tier -> sb.append(tier).append(","));
                    if (sb.length() > 1) {
                        sb.deleteCharAt(sb.length() - 1);
                    }
                    sb.append("],");
                }

                if (sb.length() > 1) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                sb.append("}");
                return sb.toString();
            }

            @Override
            public DatapointMinionData.ProfileMinionData deserialize(String json) {
                JSONObject jsonObject = new JSONObject(json);
                List<Map.Entry<String, List<Integer>>> craftedMinions = new ArrayList<>();
                for (String key : jsonObject.keySet()) {
                    List<Integer> list = new ArrayList<>();
                    for (Object object : jsonObject.getJSONArray(key)) {
                        list.add((int) object);
                    }
                    craftedMinions.add(Map.entry(key, list));
                }
                return new ProfileMinionData(craftedMinions);
            }

            @Override
            public ProfileMinionData clone(ProfileMinionData value) {
                return new ProfileMinionData(new ArrayList<>(value.craftedMinions));
            }
        });
    }

    public DatapointMinionData(String key) {
        this(key, new ProfileMinionData(new ArrayList<>()));
    }

    public record ProfileMinionData(List<Map.Entry<String, List<Integer>>> craftedMinions) {
        private static final NavigableMap<Integer, Integer> SLOTS_FOR_CRAFTED = new TreeMap<>(Map.ofEntries(
                Map.entry(0, 5), Map.entry(5, 6), Map.entry(15, 7), Map.entry(30, 8),
                Map.entry(50, 9), Map.entry(75, 10), Map.entry(100, 11), Map.entry(125, 12),
                Map.entry(150, 13), Map.entry(175, 14), Map.entry(200, 15), Map.entry(225, 16),
                Map.entry(250, 17), Map.entry(275, 18), Map.entry(300, 19), Map.entry(350, 20),
                Map.entry(400, 21), Map.entry(450, 22), Map.entry(500, 23), Map.entry(550, 24),
                Map.entry(600, 25), Map.entry(650, 26), Map.entry(700, 27)
        ));

        public int getUniqueMinions() {
            return (int) craftedMinions.stream()
                    .flatMap(entry -> entry.getValue().stream().map(tier -> entry.getKey() + ':' + tier))
                    .distinct()
                    .count();
        }

        public int getSlots() {
            return SLOTS_FOR_CRAFTED.floorEntry(getUniqueMinions()).getValue();
        }
    }
}
