package dev.daweapon.type.skyblockgeneric.data.datapoints;

import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

// Persists the pet a player currently has queued at Kat's pet-upgrade service and when it finishes.
public class DatapointKat extends SkyBlockDatapoint<DatapointKat.PlayerKat> {
    public static Serializer<PlayerKat> serializer = new Serializer<>() {
        @Override
        public String serialize(PlayerKat value) {
            Map<String, String> serialized = new HashMap<>();

            for (Map.Entry<Long, SkyBlockItem> entry : value.katMap().entrySet()) {
                if (entry.getValue() == null) continue;
                serialized.put(String.valueOf(entry.getKey()), entry.getValue().toUnderstandable().serialize());
            }
            return new JSONObject(serialized).toString();
        }

        @Override
        public PlayerKat deserialize(String json) {
            JSONObject obj = new JSONObject(json);
            Map<Long, SkyBlockItem> katMap = new HashMap<>();

            for (String key : obj.keySet()) {
                SkyBlockItem item = new SkyBlockItem(UnderstandableSkyBlockItem.deserialize(obj.getString(key)));
                katMap.put(Long.parseLong(key), item);
            }
            return new PlayerKat(katMap);
        }

        @Override
        public PlayerKat clone(PlayerKat value) {
            return new PlayerKat(new HashMap<>(value.katMap()));
        }
    };

    public DatapointKat(String key, PlayerKat value) {
        super(key, value, serializer);
    }

    public DatapointKat(String key) {
        super(key, new PlayerKat(new HashMap<>()), serializer);
    }

    public record PlayerKat(Map<Long, SkyBlockItem> katMap) {

        public long getFinishTime() {
            for (Map.Entry<Long, SkyBlockItem> entry : katMap.entrySet()) {
                if (entry.getValue() != null) return entry.getKey();
            }
            return 0;
        }

        @Nullable
        public SkyBlockItem getPet() {
            for (SkyBlockItem pet : katMap.values()) {
                if (pet != null) return pet;
            }
            return null;
        }

        public void set(long finishTime, SkyBlockItem pet) {
            katMap.clear();
            katMap.put(finishTime, pet);
        }

        public void clear() {
            katMap.clear();
        }

        public void setFinishedTime(long timestamp) {
            SkyBlockItem pet = getPet();
            if (pet != null) set(timestamp, pet);
        }
    }
}
