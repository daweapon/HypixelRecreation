package dev.daweapon.type.skyblockgeneric.enchantment.catalogue;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntToDoubleFunction;

// A catalogue enchantment whose whole effect is flat statistics per level.
public class StatEnchantment extends CatalogueEnchantment {
    private final Map<ItemStatistic, IntToDoubleFunction> grants = new LinkedHashMap<>();
    private @Nullable String limitation;

    private StatEnchantment(String key) {
        super(key);
    }

    public static StatEnchantment of(String key) {
        return new StatEnchantment(key);
    }

    public StatEnchantment linear(ItemStatistic statistic, double perLevel) {
        grants.put(statistic, level -> perLevel * level);
        return this;
    }

    public StatEnchantment offsetLinear(ItemStatistic statistic, double perLevel, int firstLevel) {
        grants.put(statistic, level -> perLevel * (level - firstLevel + 1));
        return this;
    }

    public StatEnchantment values(ItemStatistic statistic, double... byLevel) {
        grants.put(statistic, level -> byLevel[Math.min(level, byLevel.length) - 1]);
        return this;
    }

    public StatEnchantment limited(String limitation) {
        this.limitation = limitation;
        return this;
    }

    public double grantAt(ItemStatistic statistic, int level) {
        IntToDoubleFunction grant = grants.get(statistic);
        return grant == null ? 0 : grant.applyAsDouble(level);
    }

    @Override
    public ItemStatistics getStatistics(int level) {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        grants.forEach((statistic, grant) -> builder.withBase(statistic, grant.applyAsDouble(level)));
        return builder.build();
    }

    @Override
    protected @Nullable String limitation() {
        return limitation;
    }

    @Override
    public boolean isEffectActive() {
        return true;
    }

    public Map<ItemStatistic, IntToDoubleFunction> grants() {
        return grants;
    }
}
