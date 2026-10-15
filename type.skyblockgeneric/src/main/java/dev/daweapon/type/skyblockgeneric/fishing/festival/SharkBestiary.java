package dev.daweapon.type.skyblockgeneric.fishing.festival;

import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBestiary;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.List;

/**
 * Bestiary entries for the Sharks, and the "Fishing Festival" SkyBlock XP task, which pays 1 XP
 * per 50 Sharks killed up to 5,000 kills (wiki: SkyBlock Levels, Event Tasks).
 */
public final class SharkBestiary {
    private static final int KILLS_PER_XP = 50;

    private SharkBestiary() {
    }

    private static final class AllSharks {
        static final List<BestiaryMob> MOBS = Arrays.stream(FestivalShark.values())
                .map(shark -> (BestiaryMob) SharkMob.of(shark))
                .toList();
    }

    public static void updateKillsXP(SkyBlockPlayer player) {
        DatapointBestiary.PlayerBestiary bestiary = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.BESTIARY, DatapointBestiary.class).getValue();
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
        double earned = bestiary.getAmount(AllSharks.MOBS) / KILLS_PER_XP;
        if (earned > experience.getSourceXP(SkyBlockXPSource.FISHING_FESTIVAL)) {
            experience.setSourceXP(SkyBlockXPSource.FISHING_FESTIVAL, earned);
        }
    }
}
