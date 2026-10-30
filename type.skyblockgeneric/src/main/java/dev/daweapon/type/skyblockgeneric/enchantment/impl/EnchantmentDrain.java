package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import dev.daweapon.type.skyblockgeneric.healing.VitalityRegen;
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

// Drain enchantment: regenerates Vitality on each hit against a mob (1s cooldown); conflicts with
// Life Steal and Mana Steal.
public class EnchantmentDrain implements Ench, EnchFromTable, DamageEventEnchant, ConflictingEnch {

    @Override
    public String getDescription(int level) {
        return "<7>Regen <4>+" + VitalityRegen.drainAmount(level) + " Vitality <7>every time you hit a mob. <8>(1s Cooldown)";
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

        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.GAUNTLET, EnchantItemGroups.LONG_SWORD);
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
        return 5;
    }

    @Override
    public void onDamageDealt(SkyBlockPlayer player, LivingEntity target, double damageDealt, int level) {
        if (!(target instanceof SkyBlockMob)) return;
        VitalityRegen.onDrainHit(player, level);
    }

    @Override
    public List<EnchantmentType> getConflictingEnchantments() {
        return List.of(EnchantmentType.LIFE_STEAL, EnchantmentType.MANA_STEAL);
    }
}


