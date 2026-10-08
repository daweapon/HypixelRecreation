package net.swofty.type.skyblockgeneric.mythological.ability;

import net.minestom.server.coordinate.Vec;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.MobAttackService;

import java.util.Map;

/**
 * Rams the player at high speed for heavy damage and true knockback, then stands still for five
 * seconds (still hurting anyone who touches it) before ramming again.
 */
final class CretanBullAbility implements MythologicalAbility {
    private static final Map<Rarity, Double> RAM_DAMAGE = Map.of(
            Rarity.UNCOMMON, 1_000D,
            Rarity.RARE, 2_000D,
            Rarity.EPIC, 4_000D,
            Rarity.LEGENDARY, 6_000D,
            Rarity.MYTHIC, 10_000D);
    private static final double CHARGE_SPEED = 20;
    private static final double MIN_CHARGE_DISTANCE = 3;
    private static final double MAX_CHARGE_DISTANCE = 16;
    private static final double CONTACT_DISTANCE = 1.8;
    private static final int MAX_CHARGE_TICKS = 30;
    private static final int REST_TICKS = 100;
    private static final int CHARGE_COOLDOWN_TICKS = 40;
    private static final int CONTACT_INTERVAL_TICKS = 20;

    private Vec chargeDirection;
    private int chargeTicks;
    private int restingUntil = -1;
    private int nextCharge = CHARGE_COOLDOWN_TICKS;

    @Override
    public void tick(MythologicalCreatureMob mob, int ticksAlive) {
        SkyBlockPlayer target = MythologicalCombat.target(mob).orElse(null);
        if (ticksAlive < restingUntil) {
            if (target != null && ticksAlive % CONTACT_INTERVAL_TICKS == 0
                    && target.getPosition().distance(mob.getPosition()) <= CONTACT_DISTANCE) {
                MobAttackService.strike(mob, target);
            }
            return;
        }
        if (restingUntil >= 0 && ticksAlive == restingUntil) mob.setAiPaused(false);
        if (target == null) return;

        double distance = target.getPosition().distance(mob.getPosition());
        if (chargeDirection != null) {
            mob.setVelocity(chargeDirection.mul(CHARGE_SPEED).withY(mob.getVelocity().y()));
            if (distance <= CONTACT_DISTANCE) {
                ram(mob, target, ticksAlive);
            } else if (++chargeTicks >= MAX_CHARGE_TICKS) {
                chargeDirection = null;
                mob.setAiPaused(false);
                nextCharge = ticksAlive + CHARGE_COOLDOWN_TICKS;
            }
            return;
        }
        if (ticksAlive >= nextCharge && distance >= MIN_CHARGE_DISTANCE && distance <= MAX_CHARGE_DISTANCE) {
            mob.setAiPaused(true);
            mob.getVanillaNavigator().stop();
            mob.lookAt(target);
            chargeDirection = target.getPosition().sub(mob.getPosition()).asVec().withY(0).normalize();
            chargeTicks = 0;
            MythologicalCombat.sound(mob, "entity.ravager.roar", 1f, 1.2f);
        }
    }

    private void ram(MythologicalCreatureMob mob, SkyBlockPlayer target, int ticksAlive) {
        chargeDirection = null;
        mob.setVelocity(Vec.ZERO);
        MobAttackService.strike(mob, target, RAM_DAMAGE.getOrDefault(mob.getVariant().griffinRarity(), 1_000D));
        MythologicalCombat.launch(target, MythologicalCombat.away(mob, target, 30, 12));
        MythologicalCombat.sound(mob, "entity.ravager.attack", 1f, 1f);
        restingUntil = ticksAlive + REST_TICKS;
        nextCharge = restingUntil + CHARGE_COOLDOWN_TICKS;
    }
}
