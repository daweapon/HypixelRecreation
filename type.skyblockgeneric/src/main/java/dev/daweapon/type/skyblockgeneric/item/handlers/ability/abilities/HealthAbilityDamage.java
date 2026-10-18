package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import dev.daweapon.type.skyblockgeneric.item.equipment.passive.EquipmentPassiveMath;
import dev.daweapon.type.skyblockgeneric.item.equipment.passive.EquipmentPassives;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.BooleanSupplier;

/**
 * Marks the damage an ability deals while it is being cast as health-consuming, which the Ancient Cloak
 * boosts. Damage dealt by effects scheduled after the cast is not marked.
 */
public final class HealthAbilityDamage {
    private static final ThreadLocal<Boolean> CASTING = ThreadLocal.withInitial(() -> false);

    private HealthAbilityDamage() {
    }

    public static boolean cast(RegisteredAbility.AbilityCost cost, BooleanSupplier action) {
        if (!(cost instanceof HealthAbilityCost)) return action.getAsBoolean();
        CASTING.set(true);
        try {
            return action.getAsBoolean();
        } finally {
            CASTING.set(false);
        }
    }

    public static double multiplier(SkyBlockPlayer player) {
        if (!CASTING.get()) return 1;
        return EquipmentPassives.wearing(player, EquipmentPassiveMath.ANCIENT_CLOAK) ? EquipmentPassiveMath.ANCIENT_CLOAK_MULTIPLIER : 1;
    }
}
