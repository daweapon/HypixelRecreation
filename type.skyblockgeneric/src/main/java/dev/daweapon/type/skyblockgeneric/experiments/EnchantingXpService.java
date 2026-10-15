package dev.daweapon.type.skyblockgeneric.experiments;

import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointExperiments;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.LocalDate;
import java.time.ZoneOffset;

public final class EnchantingXpService {
    private static final String DAY_COUNTER = "manualXpDay";
    private static final String XP_COUNTER = "manualXpToday";

    private EnchantingXpService() {
    }

    public static DatapointExperiments.ExperimentsData data(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.EXPERIMENTS, DatapointExperiments.class).getValue();
    }

    public static double award(SkyBlockPlayer player, double xp) {
        if (xp <= 0) return 0;
        double before = player.getSkills().getRaw(SkillCategories.ENCHANTING);
        player.getSkills().increase(player, SkillCategories.ENCHANTING, xp);
        return player.getSkills().getRaw(SkillCategories.ENCHANTING) - before;
    }

    public static double awardForLevelsSpent(SkyBlockPlayer player, int levelsSpent) {
        DatapointExperiments.ExperimentsData data = data(player);
        long today = LocalDate.now(ZoneOffset.UTC).toEpochDay();
        if (data.counter(DAY_COUNTER) != today) {
            data.getCounters().put(DAY_COUNTER, today);
            data.getCounters().put(XP_COUNTER, 0L);
        }
        double xp = EnchantingXp.withinDailyCap(EnchantingXp.forLevelsSpent(levelsSpent), data.counter(XP_COUNTER));
        data.getCounters().put(XP_COUNTER, data.counter(XP_COUNTER) + Math.round(xp));
        award(player, xp);
        return xp;
    }
}
