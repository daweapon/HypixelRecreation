package net.swofty.type.skyblockgeneric.data.datapoints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatapointAccessoryBag extends SkyBlockDatapoint<DatapointAccessoryBag.PlayerAccessoryBag> {
    public static Serializer<DatapointAccessoryBag.PlayerAccessoryBag> serializer = new Serializer<>() {
        @Override
        public String serialize(DatapointAccessoryBag.PlayerAccessoryBag value) {
            JSONObject json = new JSONObject();
            json.put("discoveredAccessories", new JSONArray(value.getDiscoveredAccessories().stream().map(ItemType::name).toList()));

            for (Map.Entry<Integer, SkyBlockItem> entry : value.getAccessoryMap().entrySet()) {
                json.put(String.valueOf(entry.getKey()), entry.getValue().toUnderstandable().serialize());
            }

            return json.toString();
        }

        @Override
        public DatapointAccessoryBag.PlayerAccessoryBag deserialize(String json) {
            JSONObject obj = new JSONObject(json);
            Map<Integer, SkyBlockItem> map = new HashMap<>();
            List<ItemType> discoveredAccessories = new ArrayList<>();

            for (String key : obj.keySet()) {
                if (key.equals("discoveredAccessories")) {
                    if (!(obj.get(key) instanceof JSONArray discovered)) continue;
                    for (Object o : discovered) {
                        ItemType type = ItemType.get(o.toString());
                        if (type != null && !discoveredAccessories.contains(type)) discoveredAccessories.add(type);
                    }
                    continue;
                }

                map.put(Integer.parseInt(key),
                        new SkyBlockItem(UnderstandableSkyBlockItem.deserialize(obj.getString(key))));
            }

            return new DatapointAccessoryBag.PlayerAccessoryBag(map, discoveredAccessories);
        }

        @Override
        public DatapointAccessoryBag.PlayerAccessoryBag clone(DatapointAccessoryBag.PlayerAccessoryBag value) {
            return new DatapointAccessoryBag.PlayerAccessoryBag(new HashMap<>(value.getAccessoryMap()),
                    new ArrayList<>(value.getDiscoveredAccessories()));
        }
    };

    public DatapointAccessoryBag(String key, DatapointAccessoryBag.PlayerAccessoryBag value) {
        super(key, value, serializer);
    }

    public DatapointAccessoryBag(String key) {
        super(key, new DatapointAccessoryBag.PlayerAccessoryBag(), serializer);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlayerAccessoryBag {
        private Map<Integer, SkyBlockItem> accessoryMap = new HashMap<>();
        private List<ItemType> discoveredAccessories = new ArrayList<>();

        public @Nullable SkyBlockItem getInSlot(int slot) {
            return accessoryMap.get(slot);
        }

        public List<SkyBlockItem> getAllAccessories() {
            return List.copyOf(accessoryMap.values());
        }

        public void addDiscoveredAccessory(ItemType type) {
            if (discoveredAccessories.contains(type)) return;
            discoveredAccessories.add(type);
        }

        public boolean hasDiscoveredAccessory(ItemType type) {
            return discoveredAccessories.contains(type);
        }

        public void removeFromSlot(int slot) {
            accessoryMap.remove(slot);
        }

        public void setInSlot(int slot, SkyBlockItem item) {
            accessoryMap.put(slot, item);
        }
    }
}
