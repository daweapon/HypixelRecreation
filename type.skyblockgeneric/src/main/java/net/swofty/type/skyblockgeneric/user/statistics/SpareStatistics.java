package net.swofty.type.skyblockgeneric.user.statistics;

import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import java.util.*;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.carnival.CarnivalStatistics;
import net.swofty.type.skyblockgeneric.crimson.town.CarrolynExports;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkills;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import net.swofty.type.skyblockgeneric.essence.CrimsonEssenceShop;
import net.swofty.type.skyblockgeneric.essence.IceEssenceShop;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContestStatistics;
import net.swofty.type.skyblockgeneric.gifts.BottleOfJyrre;
import net.swofty.type.skyblockgeneric.mythological.gear.MythologicalItemHandlers;
import net.swofty.type.skyblockgeneric.slayer.SlayerProgression;
import net.swofty.type.skyblockgeneric.user.pets.PetScore;

final class SpareStatistics {
    private final SkyBlockPlayer player;

    SpareStatistics(SkyBlockPlayer player) {
        this.player = player;
    }

    ItemStatistics statistics() {
        ItemStatistics spare = ItemStatistics.builder().build();

        int fairySouls = player.getFairySouls().getExchangedFairySouls().size();
        spare = ItemStatistics.add(spare, ItemStatistics.builder().withBase(ItemStatistic.HEALTH, (double) (fairySouls * 2)).build());

        DatapointSkills.PlayerSkills skills = player.getSkills();
        spare = ItemStatistics.add(spare, skills.getSkillStatistics());

        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
        spare = ItemStatistics.add(spare, net.swofty.type.skyblockgeneric.levels.SkyBlockLevelRequirement
                .getStatisticsUpTo(experience.getLevel().asInt()));
        spare = ItemStatistics.add(spare, FarmingContestStatistics.all(player));
        spare = ItemStatistics.add(spare, net.swofty.type.skyblockgeneric.garden.chip.ChipStatistics.all(player));
        spare = ItemStatistics.add(spare, CarnivalStatistics.all(player));
        spare = ItemStatistics.add(spare, IceEssenceShop.statistics(player));
        spare = ItemStatistics.add(spare, net.swofty.type.skyblockgeneric.essence.FossilEssenceShop.statistics(player));
        spare = ItemStatistics.add(spare, CrimsonEssenceShop.statistics(player));
        spare = ItemStatistics.add(spare, net.swofty.type.skyblockgeneric.essence.EssenceShops.statistics(player));
        spare = ItemStatistics.add(spare, SlayerProgression.statistics(player));
        spare = ItemStatistics.add(spare, CarrolynExports.statistics(player));
        spare = ItemStatistics.add(spare, PetScore.statistics(player));
        spare = ItemStatistics.add(spare, MythologicalItemHandlers.permanentStatistics(player));
        spare = ItemStatistics.add(spare, BottleOfJyrre.permanentStatistics(player));
        spare = ItemStatistics.add(spare, net.swofty.type.skyblockgeneric.garden.greenhouse.RosewaterFlask.statistics(player));
        spare = ItemStatistics.add(spare, net.swofty.type.skyblockgeneric.minion.inferno.ReaperPepper.permanentStatistics(player));
        spare = ItemStatistics.add(spare, net.swofty.type.skyblockgeneric.consumable.VialOfVenom.permanentStatistics(player));
        spare = ItemStatistics.add(spare, net.swofty.type.skyblockgeneric.consumable.DwarvenOs.statistics(player));
        spare = ItemStatistics.add(spare, net.swofty.type.skyblockgeneric.fishing.lava.CrimsonFishingGear.statistics(player));

        return spare;
    }
}
