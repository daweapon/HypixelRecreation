package net.swofty.type.skyblockgeneric.mythological.ability;

import net.minestom.server.coordinate.Vec;
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
 * Leaps into the air and drifts down, shooting arrows while airborne. Each arrow that lands cuts the
 * player's Intelligence and Vitality (to at most -50%) until the Harpy dies. Below half Health it
 * stays on the ground.
 */
final class HarpyAbility implements MythologicalAbility {
    private static final String CURSE = "Harpy's Curse";
    private static final int LEAP_INTERVAL_TICKS = 120;
    private static final double LEAP_VELOCITY = 32;
    private static final double MAX_FALL_SPEED = -1.5;
    private static final int SHOT_INTERVAL_TICKS = 12;
    private static final double ARROW_SPEED = 1.6;
    private static final double MAX_SHOT_DISTANCE = 24;
    private static final double REDUCTION_PER_HIT = 10;
    private static final double MAX_REDUCTION = 50;

    private final Map<UUID, Integer> hits = new HashMap<>();
    private boolean airborne;

    @Override
    public void tick(MythologicalCreatureMob mob, int ticksAlive) {
        boolean grounded = mob.healthRatio() <= 0.5;
        if (airborne) {
            Vec velocity = mob.getVelocity();
            if (velocity.y() < MAX_FALL_SPEED) mob.setVelocity(velocity.withY(MAX_FALL_SPEED));
            if (mob.isOnGround() && ticksAlive % LEAP_INTERVAL_TICKS > 10) airborne = false;
            if (ticksAlive % SHOT_INTERVAL_TICKS == 0) shoot(mob);
            return;
        }
        if (!grounded && ticksAlive > 0 && ticksAlive % LEAP_INTERVAL_TICKS == 0) {
            airborne = true;
            mob.setVelocity(mob.getVelocity().withY(LEAP_VELOCITY));
            MythologicalCombat.sound(mob, "entity.phantom.flap", 1f, 1f);
        }
    }

    private void shoot(MythologicalCreatureMob mob) {
        MythologicalCombat.target(mob)
                .filter(target -> target.getPosition().distance(mob.getPosition()) <= MAX_SHOT_DISTANCE)
                .ifPresent(target -> {
                    mob.lookAt(target);
                    MythologicalCombat.sound(mob, "entity.skeleton.shoot", 1f, 1.2f);
                    MobProjectile.arrow(mob, target, ARROW_SPEED, player -> curse(mob, player));
                });
    }

    private void curse(MythologicalCreatureMob mob, SkyBlockPlayer player) {
        MobAttackService.strike(mob, player);
        int count = hits.merge(player.getUuid(), 1, Integer::sum);
        double reduction = -Math.min(MAX_REDUCTION, count * REDUCTION_PER_HIT);
        MythologicalCombat.debuff(player, CURSE, ItemStatistics.builder()
                .withMultiplicativePercentage(ItemStatistic.INTELLIGENCE, reduction)
                .withMultiplicativePercentage(ItemStatistic.VITALITY, reduction)
                .build());
    }

    @Override
    public void cleanup(MythologicalCreatureMob mob) {
        hits.keySet().forEach(id -> {
            SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(id);
            if (player != null) MythologicalCombat.clearDebuff(player, CURSE);
        });
        hits.clear();
    }
}
