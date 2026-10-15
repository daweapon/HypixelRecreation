package dev.daweapon.type.skyblockgeneric.region;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/** Zones that only exist while an event runs; the region underneath shows through otherwise. */
public final class RegionPresence {
    private static final Map<RegionType, BooleanSupplier> CONDITIONS = new EnumMap<>(RegionType.class);

    private RegionPresence() {}

    public static void register(RegionType type, BooleanSupplier present) {
        CONDITIONS.put(type, present);
    }

    public static boolean isPresent(RegionType type) {
        BooleanSupplier condition = CONDITIONS.get(type);
        return condition == null || condition.getAsBoolean();
    }
}
