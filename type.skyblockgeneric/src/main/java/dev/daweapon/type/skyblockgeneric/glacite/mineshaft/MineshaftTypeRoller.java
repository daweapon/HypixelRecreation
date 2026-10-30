package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import org.jetbrains.annotations.Nullable;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class MineshaftTypeRoller {
    public static final double MATCH_SHARE = 1.0 / 3;
    public static final double BASE_CRYSTAL_PERCENT = 25;

    private MineshaftTypeRoller() {
    }

    public static Map<ShaftType, Double> probabilities(@Nullable ShaftType matched, double fairyBonusPercent) {
        Map<ShaftType, Double> raw = new EnumMap<>(ShaftType.class);
        double total = 0;
        for (ShaftType type : ShaftType.values()) {
            if (type == matched) continue;
            double weight = type == ShaftType.FAIR ? 1 + fairyBonusPercent / 100 : 1;
            raw.put(type, weight);
            total += weight;
        }
        double remaining = matched == null ? 1 : 1 - MATCH_SHARE;
        Map<ShaftType, Double> result = new EnumMap<>(ShaftType.class);
        double sum = total;
        raw.forEach((type, weight) -> result.put(type, weight / sum * remaining));
        if (matched != null) result.put(matched, MATCH_SHARE);
        return result;
    }

    public static ShaftType rollType(@Nullable UUID player, @Nullable ShaftType matched, double fairyBonusPercent, RandomGenerator random) {
        Map<ShaftType, Double> probabilities = probabilities(matched, fairyBonusPercent);
        List<ShaftType> types = new ArrayList<>(probabilities.keySet());
        return LootRoll.pick(player, types, _ -> null, probabilities::get, random);
    }

    public static ShaftLayout rollLayout(@Nullable UUID player, ShaftType type, double crystalPercent, RandomGenerator random) {
        if (type.hasCrystalLayout() && LootRoll.roll(player, null, crystalPercent, random)) return type.crystalLayout();
        return LootRoll.pick(player, type.plainLayouts(), _ -> null, _ -> 1.0, random);
    }

    public static ShaftLayout roll(@Nullable UUID player, @Nullable ShaftType matched, double fairyBonusPercent,
                                   double crystalPercent, RandomGenerator random) {
        return rollLayout(player, rollType(player, matched, fairyBonusPercent, random), crystalPercent, random);
    }
}
