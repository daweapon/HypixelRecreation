package dev.daweapon.type.skyblockgeneric.enchantment.abstr;

import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Hook for enchantments that react when the wielder kills a SkyBlock mob.
public interface KillEventEnchant {
    default void onMobKilled(SkyBlockPlayer player, SkyBlockMob killedMob, int level) {
    }
}
