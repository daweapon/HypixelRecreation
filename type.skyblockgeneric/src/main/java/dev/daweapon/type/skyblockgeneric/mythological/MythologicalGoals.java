package dev.daweapon.type.skyblockgeneric.mythological;

import net.minestom.server.utils.time.TimeUnit;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.VanillaMeleeAttackGoal;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.VanillaRandomStrollGoal;

/**
 * The chase and stroll goals every Mythological creature uses. Both stand down while an ability
 * has taken over the creature's movement (a charge, a retreat, a mount).
 */
final class MythologicalGoals {
    private static final double MELEE_RANGE = 1.6;
    private static final int ATTACK_DELAY_TICKS = 20;
    private static final int STROLL_RADIUS = 6;

    private MythologicalGoals() {
    }

    static Chase chase(MythologicalCreatureMob mob) {
        return new Chase(mob);
    }

    static Stroll stroll(MythologicalCreatureMob mob) {
        return new Stroll(mob);
    }

    static final class Chase extends VanillaMeleeAttackGoal {
        private final MythologicalCreatureMob mob;

        private Chase(MythologicalCreatureMob mob) {
            super(mob, MELEE_RANGE, ATTACK_DELAY_TICKS, TimeUnit.SERVER_TICK);
            this.mob = mob;
        }

        @Override
        public boolean shouldStart() {
            return !mob.isAiPaused() && super.shouldStart();
        }

        @Override
        public void tick(long time) {
            if (mob.isAiPaused()) return;
            super.tick(time);
        }

        @Override
        public boolean shouldEnd() {
            return mob.isAiPaused() || super.shouldEnd();
        }
    }

    static final class Stroll extends VanillaRandomStrollGoal {
        private final MythologicalCreatureMob mob;

        private Stroll(MythologicalCreatureMob mob) {
            super(mob, STROLL_RADIUS);
            this.mob = mob;
        }

        @Override
        public boolean shouldStart() {
            return !mob.isAiPaused() && super.shouldStart();
        }
    }
}
