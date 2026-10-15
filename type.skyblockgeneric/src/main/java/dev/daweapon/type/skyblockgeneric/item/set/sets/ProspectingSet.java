package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.set.impl.ArmorSet;
import dev.daweapon.type.skyblockgeneric.item.set.impl.MuseumableSet;

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
