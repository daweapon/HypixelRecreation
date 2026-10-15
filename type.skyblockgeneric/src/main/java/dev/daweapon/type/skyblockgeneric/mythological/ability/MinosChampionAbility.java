package dev.daweapon.type.skyblockgeneric.mythological.ability;

import net.minestom.server.coordinate.BlockVec;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;

import java.util.List;

/**
 * Swings its sword up close and draws a bow at range. If it can't land a hit on a player within ten
 * blocks for a while, it raises an Iron Block cross under them and strikes them with lightning.
 */
final class MinosChampionAbility implements MythologicalAbility {
    private static final double BOW_MIN_DISTANCE = 5;
    private static final double BOW_MAX_DISTANCE = 24;
    private static final int BOW_INTERVAL_TICKS = 30;
    private static final double ARROW_SPEED = 1.8;
    private static final double STUCK_RANGE = 10;
    private static final int STUCK_TICKS = 160;
    private static final int LIGHTNING_DELAY_TICKS = 15;
    private static final int CROSS_LIFETIME_TICKS = 40;
    private static final double LIGHTNING_DAMAGE_MULTIPLIER = 2;

    private final TemporaryBlocks cross = new TemporaryBlocks();
    private int lastLanded;
    private int strikeAt = -1;
    private int crossExpiresAt = -1;
    private SkyBlockPlayer strikeTarget;

    @Override
    public void onMeleeHit(MythologicalCreatureMob mob, SkyBlockPlayer target) {
        lastLanded = mob.getFightTicks();
    }

    @Override
    public void tick(MythologicalCreatureMob mob, int ticksAlive) {
        if (ticksAlive == crossExpiresAt) cross.restoreAll();
        if (ticksAlive == strikeAt && strikeTarget != null && !strikeTarget.isRemoved()) {
            MythologicalCombat.lightning(mob.getInstance(), strikeTarget.getPosition());
            MobAttackService.strike(mob, strikeTarget,
                    mob.getStatistics().getOverall(ItemStatistic.DAMAGE) * LIGHTNING_DAMAGE_MULTIPLIER);
            strikeTarget = null;
        }

        SkyBlockPlayer target = MythologicalCombat.target(mob).orElse(null);
        if (target == null) return;
        double distance = target.getPosition().distance(mob.getPosition());

        if (ticksAlive % BOW_INTERVAL_TICKS == 0 && distance >= BOW_MIN_DISTANCE && distance <= BOW_MAX_DISTANCE) {
            MythologicalCombat.sound(mob, "entity.skeleton.shoot", 1f, 1f);
            MobProjectile.arrow(mob, target, ARROW_SPEED, player -> {
                MobAttackService.strike(mob, player);
                lastLanded = mob.getFightTicks();
            });
        }

        if (distance <= STUCK_RANGE && ticksAlive - lastLanded >= STUCK_TICKS) {
            lastLanded = ticksAlive;
            List<BlockVec> placed = IronCross.raise(mob.getInstance(), target.getPosition(), cross);
            if (!placed.isEmpty()) crossExpiresAt = ticksAlive + CROSS_LIFETIME_TICKS;
            strikeTarget = target;
            strikeAt = ticksAlive + LIGHTNING_DELAY_TICKS;
            MythologicalCombat.sound(mob, "block.anvil.land", 0.8f, 0.6f);
        }
    }

    @Override
    public void cleanup(MythologicalCreatureMob mob) {
        cross.restoreAll();
    }
}
