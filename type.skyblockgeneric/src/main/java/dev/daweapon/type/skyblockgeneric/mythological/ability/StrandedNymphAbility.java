package dev.daweapon.type.skyblockgeneric.mythological.ability;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.skyblockgeneric.mythological.MythologicalCreatureMob;

/**
 * Creeps towards the player; at half Health it floods a 5x5 pool around itself and moves faster
 * while standing in water.
 */
final class StrandedNymphAbility implements MythologicalAbility {
    private static final int POOL_RADIUS = 2;
    private static final double WATER_SPEED_MULTIPLIER = 2;

    private final TemporaryBlocks pool = new TemporaryBlocks();
    private boolean flooded;
    private double landSpeed = -1;

    @Override
    public void onDamaged(MythologicalCreatureMob mob, Damage damage, float amount) {
        if (flooded || mob.healthRatio() > 0.5) return;
        flooded = true;
        Pos center = mob.getPosition();
        for (int dx = -POOL_RADIUS; dx <= POOL_RADIUS; dx++) {
            for (int dz = -POOL_RADIUS; dz <= POOL_RADIUS; dz++) {
                pool.place(mob.getInstance(), center.add(dx, 0, dz), Block.WATER, false);
            }
        }
        MythologicalCombat.sound(mob, "entity.player.splash.high_speed", 1f, 1f);
    }

    @Override
    public void tick(MythologicalCreatureMob mob, int ticksAlive) {
        if (!flooded) return;
        var speed = mob.getAttribute(Attribute.MOVEMENT_SPEED);
        if (landSpeed < 0) landSpeed = speed.getBaseValue();
        boolean inWater = pool.contains(mob.getPosition());
        speed.setBaseValue(inWater ? landSpeed * WATER_SPEED_MULTIPLIER : landSpeed);
    }

    @Override
    public void cleanup(MythologicalCreatureMob mob) {
        pool.restoreAll();
    }
}
