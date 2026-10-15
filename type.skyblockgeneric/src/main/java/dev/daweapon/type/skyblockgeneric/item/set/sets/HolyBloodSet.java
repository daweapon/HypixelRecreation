package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.set.impl.ArmorSet;

import java.util.ArrayList;
import java.util.List;

public class HolyBloodSet implements ArmorSet {
    public static final double HEALTH_REGEN = 75;

    @Override
    public String getName() {
        return "Holy Blood";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of(
                "<7>Grants <c>+75 Health Regen <7>to you",
                "<7>and all players within <a>10 <7>blocks.",
                "<8>Effect only applies once!",
                "<8>Not active yet: sharing with nearby players"));
    }

    @Override
    public ItemStatistics getStatistics() {
        return ItemStatistics.builder().withBase(ItemStatistic.HEALTH_REGENERATION, HEALTH_REGEN).build();
    }
}
