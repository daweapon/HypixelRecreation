package dev.daweapon.type.skyblockgeneric.essence;

import dev.daweapon.type.generic.data.datapoints.DatapointMapStringLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;

public final class EssencePerks {
    private EssencePerks() {
    }

    public static int tier(SkyBlockPlayer player, EssencePerk perk) {
        Number value = datapoint(player).getValue().get(key(perk));
        return value == null ? 0 : value.intValue();
    }

    public static double value(SkyBlockPlayer player, EssencePerk perk) {
        return perk.valueAt(tier(player, perk));
    }

    public static void setTier(SkyBlockPlayer player, EssencePerk perk, int tier) {
        DatapointMapStringLong datapoint = datapoint(player);
        Map<String, Long> values = new HashMap<>();
        datapoint.getValue().forEach((key, value) -> values.put(key, ((Number) value).longValue()));
        values.put(key(perk), (long) Math.clamp(tier, 0, perk.getMaxTier()));
        datapoint.setValue(values);
    }

    private static String key(EssencePerk perk) {
        return perk.essence().name() + ":" + perk.name();
    }

    private static DatapointMapStringLong datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ESSENCE_PERKS, DatapointMapStringLong.class);
    }
}
