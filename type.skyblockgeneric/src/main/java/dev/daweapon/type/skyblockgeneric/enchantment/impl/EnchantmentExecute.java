package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.collection.CustomCollectionAward;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.ConflictingEnch;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Execute enchantment: increases damage dealt scaled by the target's missing health percentage;
// conflicts with Prosecute.
public class EnchantmentExecute implements Ench, EnchFromTable, EventBasedEnchant, ConflictingEnch {

    public static final double[] DAMAGE_MULTIPLIERS = new double[]{0.2, 0.4, 0.6, 0.8, 1.0, 1.25};

    @Override
    public String getDescription(int level) {
        return "Increases damage dealt by <a>" + DAMAGE_MULTIPLIERS[level - 1] + "%<7> for each percent of health missing on your target.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                4, 40,
                5, 50,
                6, 200
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.EXECUTE_DISCOUNT)) {
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
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (causer == null || receiver == null) {
            return ItemStatistics.empty();
        }

        double targetMaxHealth = SkyBlockMob.maxHealthOf(receiver);
        double targetCurrentHealth = receiver.getHealth();

        if (targetMaxHealth <= 0 || targetCurrentHealth >= targetMaxHealth) {
            return ItemStatistics.empty();
        }

        double missingHealthPercentage = ((targetMaxHealth - targetCurrentHealth) / targetMaxHealth) * 100;
        double damageMultiplier = missingHealthPercentage * DAMAGE_MULTIPLIERS[level - 1];

        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, damageMultiplier).build();
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 20,
                2, 25,
                3, 30,
                4, 40,
                5, 50
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.EXECUTE_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 10;
    }

    @Override
    public List<EnchantmentType> getConflictingEnchantments() {
        return List.of(EnchantmentType.PROSECUTE);
    }
}