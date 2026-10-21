package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.hunting.creature.LassoService;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;

final class LookAwayTeleportAttack implements CreatureSpecial {
    static final double WATCHED_DOT = 0.5;
    private static final double AWARE_RANGE = 16;
    private static final double BEHIND = 1.6;
    private static final long CHECK_PERIOD = 4;
    private static final long ATTACK_COOLDOWN_MILLIS = 3000;

    @Override
    public void onTick(HuntingCreatureMob mob, long ticks) {
        if (ticks % CHECK_PERIOD != 0 || LassoService.isLassoed(mob)) return;
        CreatureMotion.nearestPlayer(mob, AWARE_RANGE).ifPresent(player -> {
            if (CreatureMotion.lookingAt(player, mob.getPosition().add(0, mob.getEyeHeight(), 0), WATCHED_DOT)) {
                mob.getVanillaNavigator().stop();
                mob.setVelocity(new Vec(0, mob.getVelocity().y(), 0));
                return;
            }
            if (mob.getLastAttack() + ATTACK_COOLDOWN_MILLIS > System.currentTimeMillis()) return;
            Vec facing = CreatureMotion.horizontal(Pos.ZERO, player.getPosition().direction());
            Pos behind = player.getPosition().sub(facing.x() * BEHIND, 0, facing.z() * BEHIND);
            Instance instance = mob.getInstance();
            if (instance.getBlock(behind).isSolid() || instance.getBlock(behind.add(0, 1, 0)).isSolid()) return;
            mob.teleport(behind.withLookAt(player.getPosition().add(0, player.getEyeHeight(), 0)));
            CreatureMotion.strike(mob, player, ATTACK_COOLDOWN_MILLIS);
        });
    }
}
