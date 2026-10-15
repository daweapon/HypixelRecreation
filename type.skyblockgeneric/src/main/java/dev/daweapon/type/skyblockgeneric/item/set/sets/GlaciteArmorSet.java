package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.set.impl.PlayerArmorSet;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GlaciteArmorSet implements PlayerArmorSet {
    public static final double MINING_SPEED_PER_LEVEL = 2;

    public static double miningSpeed(int miningLevel) {
        return MINING_SPEED_PER_LEVEL * miningLevel;
    }

    @Override
    public String getName() {
        return "Expert Miner";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of(
                "<7>Grants <6>+2 Mining Speed <7>per Mining",
                "<7>Skill level unlocked."));
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player) {
        return ItemStatistics.builder()
                .withAdditive(ItemStatistic.MINING_SPEED, miningSpeed(player.getSkills().getCurrentLevel(SkillCategories.MINING)))
                .build();
    }
}
