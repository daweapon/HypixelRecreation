package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.MobTypeDamageEnchantment;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

// Smoldering enchantment: boosts damage dealt to infernal-type mobs.
public final class EnchantmentSmoldering implements Ench, MobTypeDamageEnchantment {
    private static final double[] BONUSES = {5, 10, 15, 20, 30};

    @Override
    public Set<MobType> affectedMobTypes() {
        return Set.of(MobType.INFERNAL);
    }

    @Override
    public double[] damageBonuses() {
        return BONUSES;
    }

    @Override
    public String getDescription(int level) {
        return "Increases damage dealt to " + MobType.INFERNAL.getFullDisplayName() + "<7> mobs by <a>" + BONUSES[level - 1] + "%<7>.";
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.FISHING_WEAPON, EnchantItemGroups.LONG_SWORD, EnchantItemGroups.GAUNTLET, EnchantItemGroups.BOW);
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(Map.of(1, 10, 2, 20, 3, 30, 4, 40, 5, 50));
    }
}
