package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.set.impl.ArmorSet;

import java.util.ArrayList;
import java.util.List;

public class SuperiorDragonSet implements ArmorSet {
    public static final double STAT_BOOST_PERCENT = 5;
    public static final List<ItemStatistic> BOOSTED = List.of(
            ItemStatistic.HEALTH, ItemStatistic.DEFENSE, ItemStatistic.STRENGTH, ItemStatistic.INTELLIGENCE,
            ItemStatistic.CRITICAL_CHANCE, ItemStatistic.CRITICAL_DAMAGE, ItemStatistic.SPEED,
            ItemStatistic.BONUS_ATTACK_SPEED, ItemStatistic.FEROCITY, ItemStatistic.ABILITY_DAMAGE,
            ItemStatistic.TRUE_DEFENSE, ItemStatistic.MAGIC_FIND);

    @Override
    public String getName() {
        return "Superior Blood";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of(
                "<7>Increases all Combat stats and",
                "<7>Magic Find by <a>5%<7> and Aspect of the",
                "<7>Dragons ability deals <a>50%<7> more",
                "<7>damage.",
                "",
                "<8>Not active yet: the Aspect of the Dragons damage bonus"));
    }

    @Override
    public ItemStatistics getStatistics() {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        BOOSTED.forEach(stat -> builder.withMultiplicativePercentage(stat, STAT_BOOST_PERCENT));
        return builder.build();
    }
}
