package net.swofty.type.skyblockgeneric.accessory.power;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;

import java.util.Map;

/**
 * Hypixel's Accessory Power stats multiplier (wiki Accessory Power/Mechanics and Module:Power
 * {@code getStatsMultiplier}): {@code 29.97 * ln(0.0019 * AP + 1) ^ 1.2}. A power's stat is its base
 * value times the multiplier; its unique bonus is added flat.
 */
public final class PowerScaling {
    public static final int TUNING_POINT_AP = 10;

    private PowerScaling() {
    }

    public static double multiplier(int accessoryPower) {
        if (accessoryPower <= 0) return 0;
        return 29.97 * Math.pow(Math.log(0.0019 * accessoryPower + 1), 1.2);
    }

    public static ItemStatistics scaled(AccessoryPowerType power, int accessoryPower) {
        double multiplier = multiplier(accessoryPower);
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (Map.Entry<ItemStatistic, Double> entry : power.getBaseStatistics().entrySet()) {
            builder.withBase(entry.getKey(), entry.getValue() * multiplier);
        }
        return builder.build();
    }

    public static ItemStatistics uniqueBonus(AccessoryPowerType power) {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        power.getUniqueBonus().forEach(builder::withBase);
        return builder.build();
    }

    public static ItemStatistics statistics(AccessoryPowerType power, int accessoryPower) {
        return ItemStatistics.add(scaled(power, accessoryPower), uniqueBonus(power));
    }

    /** The sum of every stat a power grants at this Accessory Power; /maxprofile picks the highest. */
    public static double totalStats(AccessoryPowerType power, int accessoryPower) {
        double multiplier = multiplier(accessoryPower);
        double total = 0;
        for (double value : power.getBaseStatistics().values()) total += value * multiplier;
        for (double value : power.getUniqueBonus().values()) total += value;
        return total;
    }

    public static int tuningPoints(int accessoryPower) {
        return Math.max(0, accessoryPower) / TUNING_POINT_AP;
    }
}
