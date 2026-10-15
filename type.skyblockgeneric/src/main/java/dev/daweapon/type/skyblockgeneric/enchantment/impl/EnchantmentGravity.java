package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.MobTypeDamageEnchantment;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

// Gravity enchantment: boosts damage dealt to airborne-type mobs.
public final class EnchantmentGravity implements Ench, EnchFromTable, MobTypeDamageEnchantment {
    public static final double[] BONUSES = {5, 10, 15, 20, 30, 40};

    @Override
    public Set<MobType> affectedMobTypes() {
        return Set.of(MobType.AIRBORNE);
    }

    @Override
    public double[] damageBonuses() {
        return BONUSES;
    }

    @Override
    public String getDescription(int level) {
        return "Increases damage dealt to " + MobType.AIRBORNE.getFullDisplayName() + "<7> mobs by <a>" + BONUSES[level - 1] + "%<7>.";
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.FISHING_WEAPON, EnchantItemGroups.LONG_SWORD, EnchantItemGroups.GAUNTLET, EnchantItemGroups.BOW);
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(Map.of(1, 10, 2, 15, 3, 20, 4, 25, 5, 30, 6, 100));
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        return new TableLevels(Map.of(1, 10, 2, 15, 3, 20, 4, 25, 5, 30));
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 0;
    }
}
