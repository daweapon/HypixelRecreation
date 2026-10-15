package dev.daweapon.type.skyblockgeneric.data.datapoints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.pettraining.PetTrainingType;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DatapointPetTraining extends SkyBlockDatapoint<DatapointPetTraining.PetTrainingData> {
    public static final Serializer<PetTrainingData> serializer = new Serializer<>() {
        @Override
        public String serialize(PetTrainingData value) {
            JSONObject sessions = new JSONObject();
            value.getSessions().forEach((slot, session) -> sessions.put(String.valueOf(slot), new JSONObject()
                    .put("pet", session.pet().toUnderstandable().serialize())
                    .put("type", session.type().name())
                    .put("startedAt", session.startedAt())
                    .put("endsAt", session.endsAt())
                    .put("xpPerDay", session.xpPerDay())
                    .put("coinsPerDay", session.coinsPerDay())
                    .put("bitsPerDay", session.bitsPerDay())));
            return new JSONObject()
                    .put("sessions", sessions)
                    .put("greatCarrotCandiesFed", value.getGreatCarrotCandiesFed())
                    .put("ultimateCarrotCandyFed", value.isUltimateCarrotCandyFed())
                    .toString();
        }

        @Override
        public PetTrainingData deserialize(String json) {
            JSONObject root = new JSONObject(json);
            Map<Integer, Session> sessions = new HashMap<>();
            JSONObject stored = root.has("sessions") ? root.getJSONObject("sessions") : new JSONObject();
            for (String slot : stored.keySet()) {
                JSONObject session = stored.getJSONObject(slot);
                sessions.put(Integer.parseInt(slot), new Session(
                        new SkyBlockItem(UnderstandableSkyBlockItem.deserialize(session.getString("pet"))),
                        PetTrainingType.valueOf(session.getString("type")),
                        session.getLong("startedAt"),
                        session.getLong("endsAt"),
                        session.getDouble("xpPerDay"),
                        session.getDouble("coinsPerDay"),
                        session.getInt("bitsPerDay")));
            }
            return new PetTrainingData(sessions,
                    root.optInt("greatCarrotCandiesFed", 0),
                    root.optBoolean("ultimateCarrotCandyFed", false));
        }

        @Override
        public PetTrainingData clone(PetTrainingData value) {
            return new PetTrainingData(new HashMap<>(value.getSessions()),
                    value.getGreatCarrotCandiesFed(), value.isUltimateCarrotCandyFed());
        }
    };

    public DatapointPetTraining(String key, PetTrainingData value) {
        super(key, value, serializer);
    }

    public DatapointPetTraining(String key) {
        this(key, new PetTrainingData(new HashMap<>(), 0, false));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class PetTrainingData {
        private Map<Integer, Session> sessions;
        private int greatCarrotCandiesFed;
        private boolean ultimateCarrotCandyFed;
    }

    public record Session(SkyBlockItem pet, PetTrainingType type, long startedAt, long endsAt,
                          double xpPerDay, double coinsPerDay, int bitsPerDay) {
    }
}
