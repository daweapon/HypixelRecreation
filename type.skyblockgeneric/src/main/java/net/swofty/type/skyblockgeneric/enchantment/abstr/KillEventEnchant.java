package net.swofty.type.skyblockgeneric.enchantment.abstr;

import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Hook for enchantments that react when the wielder kills a SkyBlock mob.
public interface KillEventEnchant {
    default void onMobKilled(SkyBlockPlayer player, SkyBlockMob killedMob, int level) {
    }
}
