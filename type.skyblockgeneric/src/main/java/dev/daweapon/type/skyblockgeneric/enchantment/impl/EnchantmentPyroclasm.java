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

// Pyroclasm (internal id MAGMARIZER): boosts damage dealt to magmatic mobs. VI comes from fishing.
public final class EnchantmentPyroclasm implements Ench, MobTypeDamageEnchantment {
    private static final double[] BONUSES = {10, 20, 30, 40, 60, 80};

    @Override
    public Set<MobType> affectedMobTypes() {
        return Set.of(MobType.MAGMATIC);
    }

    @Override
    public double[] damageBonuses() {
        return BONUSES;
    }

    @Override
    public String getDescription(int level) {
        return "Increases damage dealt to " + MobType.MAGMATIC.getFullDisplayName() + "<7> mobs by <a>" + BONUSES[level - 1] + "%<7>.";
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.FISHING_ROD, EnchantItemGroups.SWORD, EnchantItemGroups.LONG_SWORD, EnchantItemGroups.GAUNTLET);
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(Map.of(1, 10, 2, 20, 3, 30, 4, 40, 5, 50, 6, 60));
    }
}
