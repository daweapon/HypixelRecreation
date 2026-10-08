package net.swofty.type.skyblockgeneric.mythological.ability;

import net.minestom.server.entity.damage.Damage;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * A Mythological creature's combat kit. One instance belongs to one mob, so implementations may
 * keep per-fight state. {@link #cleanup} runs exactly once when the mob dies, escapes or is dismissed.
 */
public interface MythologicalAbility {
    default void onSpawn(MythologicalCreatureMob mob) {
    }

    default void tick(MythologicalCreatureMob mob, int ticksAlive) {
    }

    default float modifyIncomingDamage(MythologicalCreatureMob mob, Damage damage, float amount) {
        return amount;
    }

    default void onDamaged(MythologicalCreatureMob mob, Damage damage, float amount) {
    }

    default void onMeleeHit(MythologicalCreatureMob mob, SkyBlockPlayer target) {
    }

    default ItemStatistics modifyStatistics(MythologicalCreatureMob mob, ItemStatistics statistics) {
        return statistics;
    }

    default String nameHeader(MythologicalCreatureMob mob) {
        return "";
    }

    default boolean immuneToArrows() {
        return false;
    }

    default void cleanup(MythologicalCreatureMob mob) {
    }

    MythologicalAbility NONE = new MythologicalAbility() {
    };
}
