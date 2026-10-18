package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.BurningDamage;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fire Aspect enchantment: ignites hit enemies, dealing ongoing burn damage over time.
public class EnchantmentFireAspect implements Ench, EnchFromTable, DamageEventEnchant {

    public static final int[] DURATION_SECONDS = new int[]{3, 4, 4};

    public static final double[] DAMAGE_PERCENTAGES = new double[]{0.03, 0.06, 0.09};

    @Override
    public String getDescription(int level) {
        int duration = DURATION_SECONDS[level - 1];
        int damagePercent = (int) (DAMAGE_PERCENTAGES[level - 1] * 100);
        return "Ignites your enemies for <a>" + duration + "s<7>, dealing <a>" + damagePercent + "%<7> of your damage per second.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 15,
                2, 30,
                3, 0
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.FIRE_ASPECT_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.FISHING_WEAPON,
                EnchantItemGroups.GAUNTLET, EnchantItemGroups.LONG_SWORD);
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 15,
                2, 30
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.FIRE_ASPECT_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 0;
    }

    @Override
    public void onDamageDealt(SkyBlockPlayer player, LivingEntity target, double damageDealt, int level) {
        if (level < 1 || level > 3) return;
        if (!(target instanceof SkyBlockMob)) return;

        BurningDamage.ignite(player, target, BurningDamage.Source.FIRE_ASPECT, DamageType.PLAYER_ATTACK,
                damageDealt * DAMAGE_PERCENTAGES[level - 1],
                DURATION_SECONDS[level - 1] * BurningDamage.TICKS_PER_SECOND);
    }
}
