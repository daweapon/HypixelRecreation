package net.swofty.type.skyblockgeneric.essence;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public record CataloguePerk(EssenceType essence, String id, String displayName, String description, String icon,
                            double[] values, long[] costs, @Nullable String[] labels,
                            @Nullable ItemStatistic stat, Scope scope, @Nullable String unavailableReason) implements EssencePerk {

    public enum Scope {ANYWHERE, MINING, SPIDERS_DEN, THE_END, FORAGING}

    public String valueLabel(int tier) {
        if (labels != null) return labels[Math.min(Math.max(tier, 1), labels.length) - 1];
        double value = valueAt(Math.max(tier, 1));
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    @Override
    public String name() {
        return id.toUpperCase(Locale.ROOT);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public double[] getValues() {
        return values;
    }

    @Override
    public long[] getCosts() {
        return costs;
    }

    @Override
    public @Nullable String getUnavailableReason() {
        return unavailableReason;
    }
}
