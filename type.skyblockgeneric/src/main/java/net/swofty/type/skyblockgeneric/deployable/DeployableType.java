package net.swofty.type.skyblockgeneric.deployable;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

public enum DeployableType {
    LANTERN("LANTERN", "<f>Lantern", 1, 300, 30, 5, 0, 0, 0, 0, 0, false,
            stats(ItemStatistic.MINING_SPEED, 20)),
    MITHRIL_LANTERN("MITHRIL_LANTERN", "<a>Mithril Lantern", 2, 300, 30, 5, 0, 0, 0, 0, 2, false,
            stats(ItemStatistic.MINING_SPEED, 40, ItemStatistic.MINING_FORTUNE, 10)),
    TITANIUM_LANTERN("TITANIUM_LANTERN", "<9>Titanium Lantern", 3, 300, 30, 5, 0, 0, 0, 0, 5, false,
            stats(ItemStatistic.MINING_SPEED, 60, ItemStatistic.MINING_FORTUNE, 15, ItemStatistic.HEAT_RESISTANCE, 5)),
    GLACITE_LANTERN("GLACITE_LANTERN", "<5>Glacite Lantern", 4, 300, 30, 5, 0, 0, 0, 0, 8, false,
            stats(ItemStatistic.MINING_SPEED, 80, ItemStatistic.MINING_FORTUNE, 20,
                    ItemStatistic.HEAT_RESISTANCE, 10, ItemStatistic.COLD_RESISTANCE, 5)),
    WILL_O_WISP("WILL_O_WISP", "<6>Will-o'-wisp", 5, 300, 30, 10, 0, 0, 0, 0, 10, false,
            stats(ItemStatistic.MINING_SPEED, 100, ItemStatistic.MINING_FORTUNE, 25, ItemStatistic.HEAT_RESISTANCE, 20,
                    ItemStatistic.COLD_RESISTANCE, 10, ItemStatistic.GEMSTONE_SPREAD, 2.5)),
    UMBERELLA("UMBERELLA", "<9>Umberella", 1, 300, 30, 5, 0, 0, 0, 0, 0, true,
            stats(ItemStatistic.TROPHY_FISH_CHANCE, 5)),
    RADIANT_POWER_ORB("RADIANT_POWER_ORB", "<a>Radiant Power Orb", 10, 60, 18, 5, 30, 15, 0, 2, 0, false,
            stats(ItemStatistic.HEALTH_REGENERATION, 25)),
    MANA_FLUX_POWER_ORB("MANA_FLUX_POWER_ORB", "<9>Mana Flux Power Orb", 20, 60, 18, 5, 40, 20, 50, 6, 0, false,
            stats(ItemStatistic.HEALTH_REGENERATION, 50, ItemStatistic.STRENGTH, 10)),
    OVERFLUX_POWER_ORB("OVERFLUX_POWER_ORB", "<5>Overflux Power Orb", 30, 60, 18, 5, 50, 25, 100, 7, 0, false,
            stats(ItemStatistic.HEALTH_REGENERATION, 100, ItemStatistic.STRENGTH, 25)),
    PLASMAFLUX_POWER_ORB("PLASMAFLUX_POWER_ORB", "<6>Plasmaflux Power Orb", 40, 60, 20, 5, 60, 30, 125, 7, 0, false,
            stats(ItemStatistic.HEALTH_REGENERATION, 125, ItemStatistic.STRENGTH, 35));

    public static final double MANA_COST_FRACTION = 0.5;

    private final String itemId;
    private final String displayName;
    private final int rank;
    private final int durationSeconds;
    private final double radius;
    private final int maxPlayers;
    private final double selfHealPerSecond;
    private final double otherHealPerSecond;
    private final double manaRegenPercent;
    private final int wolfSlayerLevel;
    private final int hotmTier;
    private final boolean weatherDoubles;
    private final ItemStatistics statistics;

    DeployableType(String itemId, String displayName, int rank, int durationSeconds, double radius, int maxPlayers,
                   double selfHealPerSecond, double otherHealPerSecond, double manaRegenPercent, int wolfSlayerLevel,
                   int hotmTier, boolean weatherDoubles, ItemStatistics statistics) {
        this.itemId = itemId;
        this.displayName = displayName;
        this.rank = rank;
        this.durationSeconds = durationSeconds;
        this.radius = radius;
        this.maxPlayers = maxPlayers;
        this.selfHealPerSecond = selfHealPerSecond;
        this.otherHealPerSecond = otherHealPerSecond;
        this.manaRegenPercent = manaRegenPercent;
        this.wolfSlayerLevel = wolfSlayerLevel;
        this.hotmTier = hotmTier;
        this.weatherDoubles = weatherDoubles;
        this.statistics = statistics;
    }

    public static @Nullable DeployableType ofItem(@Nullable String itemId) {
        if (itemId == null) return null;
        for (DeployableType type : values()) {
            if (type.itemId.equals(itemId)) return type;
        }
        return null;
    }

    public ItemStatistics statistics(boolean weather) {
        if (!weatherDoubles || !weather) return statistics;
        return ItemStatistics.multiply(statistics, 2);
    }

    public String itemId() {
        return itemId;
    }

    public String displayName() {
        return displayName;
    }

    public int rank() {
        return rank;
    }

    public int durationSeconds() {
        return durationSeconds;
    }

    public double radius() {
        return radius;
    }

    public int maxPlayers() {
        return maxPlayers;
    }

    public double selfHealPerSecond() {
        return selfHealPerSecond;
    }

    public double otherHealPerSecond() {
        return otherHealPerSecond;
    }

    public double manaRegenPercent() {
        return manaRegenPercent;
    }

    public int wolfSlayerLevel() {
        return wolfSlayerLevel;
    }

    public int hotmTier() {
        return hotmTier;
    }

    public boolean isOrb() {
        return wolfSlayerLevel > 0;
    }

    private static ItemStatistics stats(Object... pairs) {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (int i = 0; i < pairs.length; i += 2) {
            builder.withBase((ItemStatistic) pairs[i], ((Number) pairs[i + 1]).doubleValue());
        }
        return builder.build();
    }
}
