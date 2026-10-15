package dev.daweapon.type.skyblockgeneric.shop;

import dev.daweapon.type.generic.data.datapoints.DatapointMapStringLong;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.elections.ElectionManager;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntSupplier;

public record PurchaseLimit(String id, int limit, Period period) {

    public enum Period {
        MAYORSHIP(() -> ElectionManager.getElectionData().getMayorElectedYear()),
        YEAR(SkyBlockCalendar::getYear);

        private final IntSupplier id;

        Period(IntSupplier id) {
            this.id = id;
        }
    }

    public long remaining(SkyBlockPlayer player) {
        return Math.max(0, limit - bought(player));
    }

    public boolean reached(SkyBlockPlayer player) {
        return remaining(player) <= 0;
    }

    public void record(SkyBlockPlayer player, int amount) {
        DatapointMapStringLong datapoint = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.SHOP_PURCHASE_LIMITS, DatapointMapStringLong.class);
        Map<String, Long> values = new HashMap<>();
        stored(datapoint).forEach((key, value) -> {
            if (value instanceof Number number && !isStale(key)) values.put(key, number.longValue());
        });
        values.merge(key(), (long) amount, Long::sum);
        datapoint.setValue(values);
    }

    private long bought(SkyBlockPlayer player) {
        Object value = stored(player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.SHOP_PURCHASE_LIMITS, DatapointMapStringLong.class)).get(key());
        return value instanceof Number number ? number.longValue() : 0;
    }

    private String prefix() {
        return id + "@" + period.name() + ":";
    }

    private String key() {
        return prefix() + period.id.getAsInt();
    }

    private boolean isStale(String key) {
        return key.startsWith(prefix()) && !key.equals(key());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ?> stored(DatapointMapStringLong datapoint) {
        return (Map<String, ?>) (Map<?, ?>) datapoint.getValue();
    }
}
