package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;

import java.util.ArrayList;
import java.util.List;

public class SpeedsterSet implements ArmorSet {
    public static final double BONUS_SPEED = 20;

    @Override
    public String getName() {
        return "Bonus Speed";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of("<7>Increases <f>✦ Speed <7>by <a>+20<7>."));
    }

    @Override
    public ItemStatistics getStatistics() {
        return ItemStatistics.builder().withBase(ItemStatistic.SPEED, BONUS_SPEED).build();
    }
}
