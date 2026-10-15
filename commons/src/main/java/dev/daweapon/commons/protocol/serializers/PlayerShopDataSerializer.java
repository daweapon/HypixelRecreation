package dev.daweapon.commons.protocol.serializers;

import lombok.SneakyThrows;
import dev.daweapon.commons.skyblock.PlayerShopData;
import dev.daweapon.commons.protocol.Serializer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

// Serializes PlayerShopData to/from JSON for persistence.
public class PlayerShopDataSerializer implements Serializer<PlayerShopData> {
    private final ObjectMapper mapper;

    public PlayerShopDataSerializer() {
        mapper = new ObjectMapper();
    }

    @SneakyThrows
    @Override
    public String serialize(PlayerShopData value) {
        return mapper.writeValueAsString(value.serialize());
    }

    @SneakyThrows
    @Override
    public PlayerShopData deserialize(String json) {
        JsonNode node = mapper.readTree(json);
        Map<String, Object> map = mapper.convertValue(node, Map.class);

        PlayerShopData shopData = new PlayerShopData();
        shopData.deserialize(map);

        return shopData;
    }

    @Override
    public PlayerShopData clone(PlayerShopData value) {
        return value;
    }
}
