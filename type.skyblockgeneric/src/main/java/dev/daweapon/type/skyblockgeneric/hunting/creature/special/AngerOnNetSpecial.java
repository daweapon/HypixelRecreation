package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.VanillaMeleeAttackGoal;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.utils.time.TimeUnit;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

final class AngerOnNetSpecial implements CreatureSpecial {
    private static final double ATTACK_RANGE = 1.5;
    private static final int ATTACK_COOLDOWN_TICKS = 20;
    private static final int AGGRO_RANGE = 32;

    private final Set<HuntingCreatureMob> angry = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public boolean onNetHit(SkyBlockPlayer player, HuntingCreatureMob mob, CreatureSpec.NetTier tier, double distance) {
        if (angry.add(mob)) {
            List<GoalSelector> goals = List.of(new VanillaMeleeAttackGoal(mob, ATTACK_RANGE, ATTACK_COOLDOWN_TICKS, TimeUnit.SERVER_TICK));
            List<TargetSelector> targets = List.of(new ClosestEntityTarget(mob, AGGRO_RANGE, entity -> entity == player));
            mob.getAIGroups().clear();
            mob.addAIGroup(goals, targets);
        }
        mob.setTarget(player);
        return false;
    }
}
