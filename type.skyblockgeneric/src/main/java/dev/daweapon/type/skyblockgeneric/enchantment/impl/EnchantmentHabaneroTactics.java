package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.PlayerContextEnchant;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.slayer.inferno.BlazeDagger;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Habanero Tactics ultimate enchantment (Chili Pepper collection).
public class EnchantmentHabaneroTactics implements Ench, PlayerContextEnchant {
    private static final int[] COSTS = {223, 266};
    private static final Set<String> SLAYER_WEAPONS = Set.of("REVENANT_FALCHION", "REAPER_FALCHION", "RECLUSE_FANG",
            "POOCH_SWORD", "SHAMAN_SWORD", "SCORPION_FOIL", "ORNATE_ZOMBIE_SWORD", "FLORID_ZOMBIE_SWORD", "ZOMBIE_SWORD",
            "TARANTULA_SILK", "VOIDEDGE_KATANA", "VOIDWALKER_KATANA", "VORPAL_KATANA", "ATOMSPLIT_KATANA", "LEECHING_SWORD");

    public static double damageBonusPercent(int level) {
        return level >= 5 ? 25 : level >= 4 ? 20 : 0;
    }

    public static double combatWisdom(int level) {
        return level >= 5 ? 5 : level >= 4 ? 2.5 : 0;
    }

    public static boolean isSlayerWeapon(SkyBlockItem item) {
        if (item == null || item.isAir()) return false;
        if (BlazeDagger.of(item) != null) return true;
        ItemType type = item.getAttributeHandler().getPotentialType();
        return type != null && SLAYER_WEAPONS.contains(type.name());
    }

    static int wornLevel(SkyBlockPlayer player) {
        return player.getStatistics().getAllOfEnchants(EnchantmentType.HABANERO_TACTICS, false)
                .keySet().stream().max(Integer::compareTo).orElse(0);
    }

    public static double outgoingMultiplier(SkyBlockPlayer player, SkyBlockMob target) {
        if (!(target instanceof SlayerBossMob)) return 1;
        int level = wornLevel(player);
        if (level < 4 || !isSlayerWeapon(new SkyBlockItem(player.getItemInMainHand()))) return 1;
        return 1 + damageBonusPercent(level) / 100D;
    }

    @Override
    public String getDescription(int level) {
        boolean five = level >= 5;
        return "<a>ℏ <7>Heal <a>+" + (five ? "7.5" : "6") + "% <7>more from wands. <8>(Not active yet: no healing wands)\n"
                + "<a>ℏ <7>Deal <c>+" + (five ? 25 : 20) + "% <7>damage against Slayer Bosses with Slayer weapons.\n"
                + "<a>ℏ <7>Gain <3>+" + (five ? "5" : "2.5") + "☯ Combat Wisdom <7>with Slayer weapons.\n"
                + "<a>ℏ <7>With Smoldering Polarization, gain <b>+" + (five ? 15 : 10) + " Magic Find <7>on your slayer weapon. <8>(Not active yet: needs the Re-heated Gummy Polar Bear)";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(4, COSTS[0], 5, COSTS[1])));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.ARMOR);
    }

    @Override
    public boolean isUltimate() {
        return true;
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, int level) {
        double wisdom = combatWisdom(level);
        if (wisdom <= 0 || !isSlayerWeapon(new SkyBlockItem(player.getItemInMainHand()))) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.COMBAT_WISDOM, wisdom).build();
    }
}
