package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.item.set.impl.MuseumableSet;

import java.util.ArrayList;
import java.util.List;

public class ProspectingSet implements ArmorSet, MuseumableSet {
    public static final double MINING_SPEED = 40;

    @Override
    public String getName() {
        return "Beginner's Boost";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of("<7>Grants <6>+40⸕ Mining Speed<7>."));
    }

    @Override
    public ItemStatistics getStatistics() {
        return ItemStatistics.builder().withBase(ItemStatistic.MINING_SPEED, MINING_SPEED).build();
    }
}
