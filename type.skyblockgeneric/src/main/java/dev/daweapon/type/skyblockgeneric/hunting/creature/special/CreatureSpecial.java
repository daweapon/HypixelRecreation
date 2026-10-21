package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Custom behaviour for creatures whose wiki capture or movement text needs code. Every hook has a no-op default;
 * implementations are registered by the creature's {@code special} id in {@link CreatureSpecials}.
 */
public interface CreatureSpecial {
    default void onSpawn(HuntingCreatureMob mob) {
    }

    default void onTick(HuntingCreatureMob mob, long ticks) {
    }

    /** A lasso is about to attach; return false to refuse (the special sends its own message). */
    default boolean allowLasso(SkyBlockPlayer player, HuntingCreatureMob mob) {
        return true;
    }

    /** True if this creature can be lassoed even though its capture method is not LASSO (Grizzly Bear). */
    default boolean lassoable() {
        return false;
    }

    /** The last stamina bar was reeled in; return true to replace the normal capture. */
    default boolean onLassoReeled(SkyBlockPlayer player, HuntingCreatureMob mob) {
        return false;
    }

    /** Called every tick a lasso is attached. */
    default void onLassoTick(SkyBlockPlayer player, HuntingCreatureMob mob, long ticks) {
    }

    /** Called when the lasso detaches for any reason. */
    default void onLassoDetach(SkyBlockPlayer player, HuntingCreatureMob mob) {
    }

    /**
     * A fishing net hit this creature. Return true if the special handled the hit completely (no default Pull damage
     * or instant capture). The distance is from the player to the creature.
     */
    default boolean onNetHit(SkyBlockPlayer player, HuntingCreatureMob mob, CreatureSpec.NetTier tier, double distance) {
        return false;
    }

    /** A player hit the creature with a weapon (before damage filtering). Return true to cancel the hit. */
    default boolean onPlayerHit(SkyBlockPlayer player, HuntingCreatureMob mob) {
        return false;
    }

    /** A player right-clicked the creature. Return true if handled. */
    default boolean onInteract(SkyBlockPlayer player, HuntingCreatureMob mob) {
        return false;
    }

    /** Modify how many shards a capture gives (Shellwise gives 1 to 3 by max health). */
    default int shardAmount(HuntingCreatureMob mob, int base) {
        return base;
    }
}
