package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobStatusEffects;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.BurningDamage;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

// Inferno ultimate enchantment (Kuudra loot).
public class EnchantmentInferno implements Ench, DamageEventEnchant {
    public static final int HITS_PER_INFERNO = 10;
    public static final int TRAP_SECONDS = 5;
    private static final int[] COSTS = {45, 91, 136, 179, 223};
    private static final Map<LivingEntity, Map<UUID, Integer>> HITS = Collections.synchronizedMap(new WeakHashMap<>());

    public static double damageMultiplier(int level) {
        return 1 + 0.25 * level;
    }

    public static boolean triggers(int hitNumber) {
        return hitNumber > 0 && hitNumber % HITS_PER_INFERNO == 0;
    }

    @Override
    public String getDescription(int level) {
        return "Every <a>10th <7>hit on a mob traps it for <a>5s <7>and deals <c>" + (int) Math.round(damageMultiplier(level) * 100)
                + "% <7>of that hit's damage as magic damage over the trap's duration.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        Map<Integer, Integer> levels = new HashMap<>();
        for (int level = 1; level <= COSTS.length; level++) levels.put(level, COSTS[level - 1]);
        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.FISHING_ROD, EnchantItemGroups.SWORD, EnchantItemGroups.FISHING_WEAPON, EnchantItemGroups.AXE, EnchantItemGroups.LONG_SWORD, EnchantItemGroups.GAUNTLET, EnchantItemGroups.BOW);
    }

    @Override
    public boolean isUltimate() {
        return true;
    }

    @Override
    public void onDamageDealt(SkyBlockPlayer player, LivingEntity target, double damageDealt, int level) {
        if (level < 1 || level > COSTS.length || !(target instanceof SkyBlockMob mob)) return;
        int hit;
        synchronized (HITS) {
            hit = HITS.computeIfAbsent(target, ignored -> new HashMap<>()).merge(player.getUuid(), 1, Integer::sum);
        }
        if (!triggers(hit)) return;
        MobStatusEffects.stun(mob, TRAP_SECONDS * 1000L);
        BurningDamage.ignite(player, target, BurningDamage.Source.INFERNO, DamageType.MAGIC,
                damageDealt * damageMultiplier(level) / TRAP_SECONDS,
                TRAP_SECONDS * BurningDamage.TICKS_PER_SECOND);
    }
}
