package net.swofty.type.skyblockgeneric.mythological.ability;

import net.minestom.server.entity.damage.Damage;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.MobAttackService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Every hit a player lands lowers all of that player's stats by 10%, to at most 40%, until the
 * Inquisitor is gone. It calls lightning down on the player's block, harder the longer it has lived.
 */
final class MinosInquisitorAbility implements MythologicalAbility {
    private static final String DRAIN = "Inquisitor's Judgement";
    private static final double REDUCTION_PER_HIT = 10;
    private static final double MAX_REDUCTION = 40;
    private static final int LIGHTNING_INTERVAL_TICKS = 100;
    private static final double LIGHTNING_DAMAGE_MULTIPLIER = 2;
    private static final double LIGHTNING_RANGE = 20;

    private final Map<UUID, Integer> hits = new HashMap<>();

    @Override
    public void onDamaged(MythologicalCreatureMob mob, Damage damage, float amount) {
        if (!(damage.getAttacker() instanceof SkyBlockPlayer player)) return;
        int count = hits.merge(player.getUuid(), 1, Integer::sum);
        if (count * REDUCTION_PER_HIT > MAX_REDUCTION) return;
        double reduction = -Math.min(MAX_REDUCTION, count * REDUCTION_PER_HIT);
        ItemStatistics.Builder drain = ItemStatistics.builder();
        for (ItemStatistic statistic : ItemStatistic.values()) {
            drain.withMultiplicativePercentage(statistic, reduction);
        }
        MythologicalCombat.debuff(player, DRAIN, drain.build());
    }

    @Override
    public void tick(MythologicalCreatureMob mob, int ticksAlive) {
        if (ticksAlive == 0 || ticksAlive % LIGHTNING_INTERVAL_TICKS != 0) return;
        MythologicalCombat.target(mob)
                .filter(target -> target.getPosition().distance(mob.getPosition()) <= LIGHTNING_RANGE)
                .ifPresent(target -> {
                    MythologicalCombat.lightning(mob.getInstance(), target.getPosition());
                    MobAttackService.strike(mob, target,
                            mob.getStatistics().getOverall(ItemStatistic.DAMAGE) * LIGHTNING_DAMAGE_MULTIPLIER);
                });
    }

    @Override
    public void cleanup(MythologicalCreatureMob mob) {
        hits.keySet().forEach(id -> {
            SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(id);
            if (player != null) MythologicalCombat.clearDebuff(player, DRAIN);
        });
        hits.clear();
    }
}
