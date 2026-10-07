package net.swofty.type.skyblockgeneric.mythological;

import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBestiary;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.List;

/**
 * Bestiary families for the Mythological Creatures section, and the "Mythological Kills" SkyBlock
 * XP task, which pays 1 XP per 100 Mythological kills up to 10,000 kills.
 */
public final class MythologicalBestiary {
    private static final int KILLS_PER_XP = 100;

    private MythologicalBestiary() {
    }

    public static List<BestiaryMob> family(MythologicalCreature creature) {
        return creature.allVariants().stream()
                .map(variant -> (BestiaryMob) MythologicalCreatureMob.of(creature, variant))
                .toList();
    }

    private static final class AllFamilies {
        static final List<BestiaryMob> MOBS = Arrays.stream(MythologicalCreature.values())
                .flatMap(creature -> family(creature).stream())
                .toList();
    }

    public static void updateKillsXP(SkyBlockPlayer player) {
        DatapointBestiary.PlayerBestiary bestiary = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.BESTIARY, DatapointBestiary.class).getValue();
        int kills = bestiary.getAmount(AllFamilies.MOBS);
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.MYTHOLOGICAL_KILLS, kills / KILLS_PER_XP);
    }
}
