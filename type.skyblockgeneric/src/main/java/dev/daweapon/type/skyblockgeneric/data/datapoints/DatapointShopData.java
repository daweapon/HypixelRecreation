package dev.daweapon.type.skyblockgeneric.data.datapoints;

import dev.daweapon.commons.skyblock.PlayerShopData;
import dev.daweapon.commons.protocol.serializers.PlayerShopDataSerializer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDatapoint;

// Persists a player's shop purchase/cooldown data (PlayerShopData) using its dedicated serializer.
public class DatapointShopData extends SkyBlockDatapoint<PlayerShopData> {
    private static final PlayerShopDataSerializer serializer = new PlayerShopDataSerializer();

    public DatapointShopData(String key, PlayerShopData value) {
        super(key, value, serializer);
    }

    public DatapointShopData(String key) {
        super(key, null, serializer);
    }
}
