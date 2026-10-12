package net.swofty.type.skyblockgeneric.item.handlers.bow;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.ArrowEntityImpl;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.ShortBowComponent;
import net.swofty.type.skyblockgeneric.item.set.sets.terror.HydraStrike;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.PlayerStatistics;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Map;

/** The normal damage formula for one arrow: the shooter's stats plus the arrow's, then the bow's and set's modifiers. */
public final class ArrowDamage {
    private ArrowDamage() {
    }

    public static ItemStatistics shooterStatistics(SkyBlockPlayer shooter, SkyBlockItem arrowItem) {
        return ItemStatistics.add(shooter.getStatistics().allStatistics(), arrowItem.getAttributeHandler().getStatistics());
    }

    public static Map.Entry<Double, Boolean> roll(SkyBlockPlayer shooter, ArrowEntityImpl arrow, ItemStatistics enemyStatistics) {
        ItemStatistics stats = divideCritChance(shooterStatistics(shooter, arrow.getArrowItem()), critChanceDivisor(arrow.getBow()));
        Map.Entry<Double, Boolean> hit = PlayerStatistics.runPrimaryDamageFormula(stats,
                ArrowEffects.enemyStatistics(ArrowEffects.of(arrow.getArrowItem()), enemyStatistics));
        double damage = hit.getKey() * arrow.getDamageFactor() * HydraStrike.damageMultiplier(shooter);
        return new AbstractMap.SimpleEntry<>(damage, hit.getValue());
    }

    /** Every arrow that lands on an enemy feeds Hydra Strike and the Terminator's Salvation counter. */
    public static void onLanded(SkyBlockPlayer shooter, ArrowEntityImpl arrow) {
        HydraStrike.onArrowHit(shooter);
        Salvation.onArrowHit(shooter, arrow.getBow());
    }

    public static double critChanceDivisor(@Nullable SkyBlockItem bow) {
        if (bow == null || !bow.hasComponent(ShortBowComponent.class)) return 1;
        return bow.getComponent(ShortBowComponent.class).getCritChanceDivisor();
    }

    public static ItemStatistics divideCritChance(ItemStatistics stats, double divisor) {
        if (divisor <= 1) return stats;
        return stats.withExactValue(ItemStatistic.CRITICAL_CHANCE, stats.getBase(ItemStatistic.CRITICAL_CHANCE) / divisor);
    }

    public static ItemStatistics alwaysCrit(ItemStatistics stats) {
        return stats.withExactValue(ItemStatistic.CRITICAL_CHANCE, 100);
    }
}
