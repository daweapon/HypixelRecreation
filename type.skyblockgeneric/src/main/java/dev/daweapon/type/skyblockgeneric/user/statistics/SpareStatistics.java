package dev.daweapon.type.skyblockgeneric.user.statistics;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import java.util.*;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalStatistics;
import dev.daweapon.type.skyblockgeneric.crimson.town.CarrolynExports;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkills;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import dev.daweapon.type.skyblockgeneric.essence.CrimsonEssenceShop;
import dev.daweapon.type.skyblockgeneric.essence.IceEssenceShop;
import dev.daweapon.type.skyblockgeneric.farmingcontest.FarmingContestStatistics;
import dev.daweapon.type.skyblockgeneric.gifts.BottleOfJyrre;
import dev.daweapon.type.skyblockgeneric.mythological.gear.MythologicalItemHandlers;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerProgression;
import dev.daweapon.type.skyblockgeneric.user.pets.PetScore;

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
        spare = ItemStatistics.add(spare, dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelRequirement
                .getStatisticsUpTo(experience.getLevel().asInt()));
        spare = ItemStatistics.add(spare, FarmingContestStatistics.all(player));
        spare = ItemStatistics.add(spare, dev.daweapon.type.skyblockgeneric.garden.chip.ChipStatistics.all(player));
        spare = ItemStatistics.add(spare, CarnivalStatistics.all(player));
        spare = ItemStatistics.add(spare, dev.daweapon.type.skyblockgeneric.collection.CollectionStatistics.all(player));
        spare = ItemStatistics.add(spare, dev.daweapon.type.skyblockgeneric.communityupgrades.CommunityUpgradeStatistics.all(player));
        spare = ItemStatistics.add(spare, IceEssenceShop.statistics(player));
        spare = ItemStatistics.add(spare, dev.daweapon.type.skyblockgeneric.essence.FossilEssenceShop.statistics(player));
        spare = ItemStatistics.add(spare, CrimsonEssenceShop.statistics(player));
        spare = ItemStatistics.add(spare, dev.daweapon.type.skyblockgeneric.essence.EssenceShops.statistics(player));
        spare = ItemStatistics.add(spare, SlayerProgression.statistics(player));
        spare = ItemStatistics.add(spare, CarrolynExports.statistics(player));
        spare = ItemStatistics.add(spare, PetScore.statistics(player));
        spare = ItemStatistics.add(spare, MythologicalItemHandlers.permanentStatistics(player));
        spare = ItemStatistics.add(spare, BottleOfJyrre.permanentStatistics(player));
        spare = ItemStatistics.add(spare, dev.daweapon.type.skyblockgeneric.garden.greenhouse.RosewaterFlask.statistics(player));
        spare = ItemStatistics.add(spare, dev.daweapon.type.skyblockgeneric.minion.inferno.ReaperPepper.permanentStatistics(player));
        spare = ItemStatistics.add(spare, dev.daweapon.type.skyblockgeneric.consumable.VialOfVenom.permanentStatistics(player));
        spare = ItemStatistics.add(spare, dev.daweapon.type.skyblockgeneric.consumable.DwarvenOs.statistics(player));
        spare = ItemStatistics.add(spare, dev.daweapon.type.skyblockgeneric.fishing.lava.CrimsonFishingGear.statistics(player));

        return spare;
    }
}
