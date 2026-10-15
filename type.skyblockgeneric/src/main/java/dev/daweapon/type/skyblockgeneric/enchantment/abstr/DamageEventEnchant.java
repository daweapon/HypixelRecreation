package dev.daweapon.type.skyblockgeneric.enchantment.abstr;

import net.minestom.server.entity.LivingEntity;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Hook for enchantments that react when the wielder deals damage to a living entity.
public interface DamageEventEnchant {
    default void onDamageDealt(SkyBlockPlayer player, LivingEntity target, double damageDealt, int level) {
    }
}

