package dev.daweapon.type.skyblockgeneric.user.statistics;

import lombok.Getter;
import lombok.Setter;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class TemporaryStatistic {
    private ItemStatistics statistics;
    private long expiration;
    // Optional display info for tablist
    private @Nullable String displayName;
    private @Nullable String displayColor;
    private boolean hiddenFromEffects;

    public TemporaryStatistic(ItemStatistics statistics, long expiration) {
        this.statistics = statistics;
        this.expiration = expiration;
    }

    public TemporaryStatistic(ItemStatistics statistics, long expiration, String displayName, String displayColor) {
        this.statistics = statistics;
        this.expiration = expiration;
        this.displayName = displayName;
        this.displayColor = displayColor;
    }

    /**
     * Get remaining duration in milliseconds
     */
    public long getRemainingMs() {
        return Math.max(0, expiration - System.currentTimeMillis());
    }

    /**
     * Check if this statistic has display info for tablist
     */
    public boolean hasDisplayInfo() {
        return displayName != null && !hiddenFromEffects;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ItemStatistics statistics;
        private long expiration;
        private String displayName;
        private String displayColor;
        private boolean hiddenFromEffects;

        public Builder withStatistics(ItemStatistics statistics) {
            this.statistics = statistics;
            return this;
        }

        public Builder withExpirationInMs(long expiration) {
            this.expiration = System.currentTimeMillis() + (expiration);
            return this;
        }

        public Builder withExpirationInTicks(long expiration) {
            this.expiration = System.currentTimeMillis() + (expiration * 50);
            return this;
        }

        public Builder withDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder withDisplayColor(String displayColor) {
            this.displayColor = displayColor;
            return this;
        }

        /** Named so it can be found and refreshed, but left out of the Active Effects list, like an ability buff on Hypixel. */
        public Builder hiddenFromEffects() {
            this.hiddenFromEffects = true;
            return this;
        }

        public TemporaryStatistic build() {
            TemporaryStatistic statistic = new TemporaryStatistic(statistics, expiration, displayName, displayColor);
            statistic.hiddenFromEffects = hiddenFromEffects;
            return statistic;
        }
    }
}
