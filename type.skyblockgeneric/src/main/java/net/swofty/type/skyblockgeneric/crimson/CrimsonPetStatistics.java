package net.swofty.type.skyblockgeneric.crimson;

import net.swofty.commons.skyblock.statistics.ItemStatistic;

import java.util.List;

public final class CrimsonPetStatistics {
    public static final List<ItemStatistic> COMBAT_AND_MISC = List.of(
            ItemStatistic.HEALTH, ItemStatistic.DEFENSE, ItemStatistic.STRENGTH, ItemStatistic.INTELLIGENCE,
            ItemStatistic.CRITICAL_CHANCE, ItemStatistic.CRITICAL_DAMAGE, ItemStatistic.BONUS_ATTACK_SPEED,
            ItemStatistic.ABILITY_DAMAGE, ItemStatistic.TRUE_DEFENSE, ItemStatistic.FEROCITY,
            ItemStatistic.HEALTH_REGENERATION, ItemStatistic.VITALITY, ItemStatistic.MENDING, ItemStatistic.SWING_RANGE,
            ItemStatistic.SPEED, ItemStatistic.MAGIC_FIND, ItemStatistic.PET_LUCK);

    private CrimsonPetStatistics() {
    }
}
