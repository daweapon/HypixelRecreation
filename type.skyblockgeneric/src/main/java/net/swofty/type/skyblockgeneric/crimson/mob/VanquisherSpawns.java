package net.swofty.type.skyblockgeneric.crimson.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobVanquisher;
import net.swofty.type.skyblockgeneric.essence.CrimsonEssenceShop;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.hunting.AttributeId;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Vanquishers (wiki: Vanquisher): 1 in 640 on every Crimson Isle mob kill. After 640 kills without one the
 * chance doubles, after 960 it triples and after 1,280 it quadruples. Wither Piper, the Crimson Serendipity
 * attribute and Tracking (a Vanquisher is Elusive) each multiply it. The pity count is per profile.
 */
public final class VanquisherSpawns {
    static final double BASE_ODDS = 640;
    static final String PITY_KEY = "vanquisher:pity";
    private static final AttributeId CRIMSON_SERENDIPITY = AttributeId.parse("R59");

    private VanquisherSpawns() {
    }

    public static int pityMultiplier(long killsSinceLast) {
        if (killsSinceLast >= 1280) return 4;
        if (killsSinceLast >= 960) return 3;
        if (killsSinceLast >= 640) return 2;
        return 1;
    }

    public static double chancePercent(SkyBlockPlayer player, long killsSinceLast) {
        double chance = 100D / BASE_ODDS * pityMultiplier(killsSinceLast);
        chance *= 1 + CrimsonEssenceShop.witherPiperBonus(player) / 100D;
        chance *= 1 + AttributeEffectService.value(player.getHuntingData(), CRIMSON_SERENDIPITY) / 100D;
        chance *= 1 + player.getStatistics().allStatistics().getOverall(ItemStatistic.TRACKING) / 100D;
        return chance;
    }

    public static void onKill(SkyBlockPlayer killer, Pos at, Instance instance) {
        long pity = DatapointCrimsonIsle.of(killer).getNumber(PITY_KEY);
        if (!LootRoll.roll(killer.getUuid(), null, chancePercent(killer, pity))) {
            DatapointCrimsonIsle.edit(killer, data -> data.getNumbers().put(PITY_KEY, pity + 1));
            return;
        }
        DatapointCrimsonIsle.edit(killer, data -> data.getNumbers().put(PITY_KEY, 0L));
        killer.sendMessage("<5>A Vanquisher is spawning nearby!");
        new MobVanquisher().setInstance(instance, at.add(0, 1, 0));
    }
}
