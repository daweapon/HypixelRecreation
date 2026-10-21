package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;

final class ShrinkTeleportSpecial implements CreatureSpecial {
    private static final int HITS_TO_CAPTURE = 3;
    private static final double SHRINK_PER_HIT = 0.7;
    private static final double MIN_JUMP = 3;
    private static final double MAX_JUMP = 6;
    private static final int ATTEMPTS = 12;
    private static final int BLINDNESS_TICKS = 40;

    private final Map<HuntingCreatureMob, Integer> hits = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public boolean onNetHit(SkyBlockPlayer player, HuntingCreatureMob mob, CreatureSpec.NetTier tier, double distance) {
        int count = hits.merge(mob, 1, Integer::sum);
        player.addEffect(new Potion(PotionEffect.BLINDNESS, 0, BLINDNESS_TICKS));
        if (count >= HITS_TO_CAPTURE) {
            hits.remove(mob);
            mob.capture(player);
            return true;
        }
        mob.getAttribute(Attribute.SCALE).setBaseValue(Math.pow(SHRINK_PER_HIT, count));
        teleportAway(mob);
        return true;
    }

    private void teleportAway(HuntingCreatureMob mob) {
        if (mob.getInstance() == null) return;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < ATTEMPTS; i++) {
            double angle = random.nextDouble(Math.PI * 2);
            double jump = random.nextDouble(MIN_JUMP, MAX_JUMP);
            Pos target = mob.getPosition().add(Math.cos(angle) * jump, random.nextDouble(-1, 1), Math.sin(angle) * jump);
            if (mob.getInstance().getBlock(target).isLiquid()) {
                mob.teleport(target);
                return;
            }
        }
    }
}
