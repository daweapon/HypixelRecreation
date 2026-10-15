package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.ConflictingEnch;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Life Steal enchantment: heals the wielder a percentage of max health per hit on a mob; conflicts
// with Drain and Mana Steal.
public class EnchantmentLifeSteal implements Ench, EnchFromTable, DamageEventEnchant, ConflictingEnch {

    public static final double[] HEAL_PERCENTAGES = new double[]{0.005, 0.01, 0.015, 0.02, 0.025};

    @Override
    public String getDescription(int level) {
        double healPercent = HEAL_PERCENTAGES[level - 1] * 100;
        return "Heals for <a>" + healPercent + "%<7> of your max health each time you hit a mob.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 20,
                2, 25,
                3, 30,
                4, 50,
                5, 200
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.LIFE_STEAL_DISCOUNT)) {
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
                1, 20,
                2, 25,
                3, 30
        ));

        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 3;
    }

    @Override
    public void onDamageDealt(SkyBlockPlayer player, net.minestom.server.entity.LivingEntity target, double damageDealt, int level) {
        if (!(target instanceof SkyBlockMob)) return;

        double healPercent = HEAL_PERCENTAGES[level - 1];
        float healAmount = (float) (player.getMaxHealth() * healPercent);

        float newHealth = Math.min(player.getHealth() + healAmount, player.getMaxHealth());
        player.setHealth(newHealth);
    }

    @Override
    public List<EnchantmentType> getConflictingEnchantments() {
        return List.of(EnchantmentType.DRAIN, EnchantmentType.MANA_STEAL);
    }
}

