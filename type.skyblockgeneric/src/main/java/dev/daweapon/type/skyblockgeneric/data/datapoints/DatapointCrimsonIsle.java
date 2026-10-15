package dev.daweapon.type.skyblockgeneric.data.datapoints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Every per-profile piece of Crimson Isle progress in one save entry: faction membership,
 * reputation per faction, main-questline stages, and free-form sections that later systems
 * (faction quests, the Dojo, the Kuudra Follower line…) own by key.
 */
public class DatapointCrimsonIsle extends SkyBlockDatapoint<DatapointCrimsonIsle.CrimsonIsleData> {
    public static final Serializer<CrimsonIsleData> serializer = new Serializer<>() {
        @Override
        public String serialize(CrimsonIsleData value) {
            JSONObject reputation = new JSONObject();
            value.getReputation().forEach((faction, amount) -> reputation.put(faction.name(), amount));
            return new JSONObject()
                    .put("faction", value.getFaction() == null ? JSONObject.NULL : value.getFaction().name())
                    .put("reputation", reputation)
                    .put("questline", new JSONObject(value.getQuestline()))
                    .put("numbers", new JSONObject(value.getNumbers()))
                    .put("sections", new JSONObject(value.getSections()))
                    .toString();
        }

        @Override
        public CrimsonIsleData deserialize(String json) {
            CrimsonIsleData data = CrimsonIsleData.empty();
            if (json == null || json.isBlank()) return data;
            JSONObject root = new JSONObject(json);

            if (root.has("faction") && !root.isNull("faction")) {
                data.setFaction(CrimsonFaction.byName(root.getString("faction")));
            }
            JSONObject reputation = object(root, "reputation");
            for (String key : reputation.keySet()) {
                CrimsonFaction parsed = CrimsonFaction.byName(key);
                if (parsed != null) data.getReputation().put(parsed, Math.max(0, reputation.getInt(key)));
            }
            JSONObject questline = object(root, "questline");
            questline.keySet().forEach(key -> data.getQuestline().put(key, questline.getInt(key)));
            JSONObject numbers = object(root, "numbers");
            numbers.keySet().forEach(key -> data.getNumbers().put(key, numbers.getLong(key)));
            JSONObject sections = object(root, "sections");
            sections.keySet().forEach(key -> data.getSections().put(key, sections.getString(key)));
            return data;
        }

        private static JSONObject object(JSONObject root, String key) {
            return root.has(key) && !root.isNull(key) ? root.getJSONObject(key) : new JSONObject();
        }

        @Override
        public CrimsonIsleData clone(CrimsonIsleData value) {
            return new CrimsonIsleData(value.getFaction(),
                    new EnumMap<>(value.getReputation()),
                    new HashMap<>(value.getQuestline()),
                    new HashMap<>(value.getNumbers()),
                    new HashMap<>(value.getSections()));
        }
    };

    public DatapointCrimsonIsle(String key, CrimsonIsleData value) {
        super(key, value, serializer);
    }

    public DatapointCrimsonIsle(String key) {
        this(key, CrimsonIsleData.empty());
    }

    public static CrimsonIsleData of(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    /** Applies a change to a copy of the player's data and saves it, since mutating in place never persists. */
    public static void edit(SkyBlockPlayer player, Consumer<CrimsonIsleData> change) {
        DatapointCrimsonIsle datapoint = datapoint(player);
        CrimsonIsleData copy = serializer.clone(datapoint.getValue());
        change.accept(copy);
        datapoint.setValue(copy);
    }

    private static DatapointCrimsonIsle datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CRIMSON_ISLE, DatapointCrimsonIsle.class);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CrimsonIsleData {
        private @Nullable CrimsonFaction faction;
        private Map<CrimsonFaction, Integer> reputation;
        /** Main-questline progress keyed by questline id (e.g. "UNIVERSAL", "MAGE", "BARBARIAN"). */
        private Map<String, Integer> questline;
        /** Counters and timestamps owned by later systems, keyed "<system>:<name>". */
        private Map<String, Long> numbers;
        /** Serialized JSON blobs owned by later systems, keyed by system id. */
        private Map<String, String> sections;

        public static CrimsonIsleData empty() {
            return new CrimsonIsleData(null, new EnumMap<>(CrimsonFaction.class),
                    new HashMap<>(), new HashMap<>(), new HashMap<>());
        }

        public int getReputation(CrimsonFaction faction) {
            return reputation.getOrDefault(faction, 0);
        }

        public int getQuestlineStage(String questline) {
            return this.questline.getOrDefault(questline, 0);
        }

        public long getNumber(String key) {
            return numbers.getOrDefault(key, 0L);
        }

        public @Nullable String getSection(String key) {
            return sections.get(key);
        }
    }
}
