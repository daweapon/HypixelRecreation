package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.BurningDamage;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Flame enchantment for bows (Blaze Rod collection discount): the same ignite as Fire Aspect, fired on arrow hits.
public class EnchantmentFlame implements Ench, DamageEventEnchant {
    public static final double[] DURATION_SECONDS = {3.5, 4};
    public static final double[] DAMAGE_PERCENTAGES = {0.03, 0.06};

    @Override
    public String getDescription(int level) {
        double seconds = DURATION_SECONDS[level - 1];
        String duration = seconds == Math.rint(seconds) ? String.valueOf((int) seconds) : String.valueOf(seconds);
        return "Ignites your enemies for <a>" + duration + "s<7>, dealing <a>"
                + (int) Math.round(DAMAGE_PERCENTAGES[level - 1] * 100) + "%<7> of your damage per second.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(1, 25, 2, 50));
        if (player.hasCustomCollectionAward(CustomCollectionAward.FLAME_DISCOUNT)) {
            levels.replaceAll((level, cost) -> (int) (cost * 0.75));
        }
        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.BOW);
    }

    @Override
    public void onDamageDealt(SkyBlockPlayer player, LivingEntity target, double damageDealt, int level) {
        if (level < 1 || level > DURATION_SECONDS.length || !(target instanceof SkyBlockMob)) return;
        BurningDamage.ignite(player, target, BurningDamage.Source.FLAME, DamageType.PLAYER_ATTACK,
                damageDealt * DAMAGE_PERCENTAGES[level - 1],
                (int) Math.round(DURATION_SECONDS[level - 1] * BurningDamage.TICKS_PER_SECOND));
    }
}
