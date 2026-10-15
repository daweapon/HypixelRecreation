package dev.daweapon.type.skyblockgeneric.mythological;

import dev.daweapon.type.skyblockgeneric.bestiary.BestiaryData;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBestiary;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.List;

/**
 * Bestiary families for the Mythological Creatures section, and the "Mythological Kills" SkyBlock
 * XP task, which pays 1 XP per 100 Mythological kills up to 10,000 kills.
 */
public final class MythologicalBestiary {
    private static final int KILLS_PER_XP = 100;
    private static final BestiaryData BESTIARY = new BestiaryData();

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

    public static int totalTiers(SkyBlockPlayer player) {
        DatapointBestiary.PlayerBestiary bestiary = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.BESTIARY, DatapointBestiary.class).getValue();
        int tiers = 0;
        for (MythologicalCreature creature : MythologicalCreature.values()) {
            List<BestiaryMob> family = family(creature);
            tiers += BESTIARY.getCurrentBestiaryTier(family.getFirst(), bestiary.getAmount(family));
        }
        return tiers;
    }

    public static void updateKillsXP(SkyBlockPlayer player) {
        DatapointBestiary.PlayerBestiary bestiary = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.BESTIARY, DatapointBestiary.class).getValue();
        int kills = bestiary.getAmount(AllFamilies.MOBS);
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.MYTHOLOGICAL_KILLS, kills / KILLS_PER_XP);
    }
}
