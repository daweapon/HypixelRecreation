package dev.daweapon.type.skyblockgeneric.mythological.ability;

import net.minestom.server.entity.damage.Damage;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Every hit a player lands lowers all of that player's stats by 10%, to at most 40%, until the
 * Inquisitor is gone. It calls lightning down on the player's block, harder the longer it has lived;
 * jumping dodges it.
 */
final class MinosInquisitorAbility implements MythologicalAbility {
    private static final String DRAIN = "Inquisitor's Judgement";
    private static final double REDUCTION_PER_HIT = 10;
    private static final double MAX_REDUCTION = 40;
    private static final int LIGHTNING_MIN_INTERVAL_TICKS = 200;
    private static final int LIGHTNING_MAX_INTERVAL_TICKS = 300;
    private static final double LIGHTNING_DAMAGE_MULTIPLIER = 2;
    private static final double LIGHTNING_RANGE = 20;

    private final Map<UUID, Integer> hits = new HashMap<>();
    private int nextStrike = nextInterval();

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
        if (ticksAlive < nextStrike) return;
        nextStrike = ticksAlive + nextInterval();
        MythologicalCombat.target(mob)
                .filter(target -> target.getPosition().distance(mob.getPosition()) <= LIGHTNING_RANGE)
                .ifPresent(target -> {
                    MythologicalCombat.lightning(mob.getInstance(), target.getPosition().withY(Math.floor(target.getPosition().y())));
                    if (!target.isOnGround()) return;
                    MobAttackService.strike(mob, target,
                            mob.getStatistics().getOverall(ItemStatistic.DAMAGE) * LIGHTNING_DAMAGE_MULTIPLIER);
                });
    }

    private static int nextInterval() {
        return ThreadLocalRandom.current().nextInt(LIGHTNING_MIN_INTERVAL_TICKS, LIGHTNING_MAX_INTERVAL_TICKS + 1);
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
