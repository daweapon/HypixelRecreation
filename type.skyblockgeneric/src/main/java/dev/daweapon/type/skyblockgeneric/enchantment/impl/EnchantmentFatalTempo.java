package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeLoadout;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Fatal Tempo ultimate enchantment (Kuudra loot).
public class EnchantmentFatalTempo implements Ench {
    public static final double FEROCITY_PER_LEVEL = 10;
    public static final double MAX_BONUS = 200;
    public static final long WINDOW_MILLIS = 3000;
    private static final int[] COSTS = {45, 91, 136, 179, 223};
    private static final Map<UUID, Stacks> STACKS = new ConcurrentHashMap<>();

    private record Stacks(double bonus, long lastHit) {
    }

    public static double activeBonus(double bonus, long millisSinceLastHit) {
        return millisSinceLastHit > WINDOW_MILLIS ? 0 : bonus;
    }

    public static double nextBonus(double bonus, long millisSinceLastHit, int level) {
        return Math.min(MAX_BONUS, activeBonus(bonus, millisSinceLastHit) + FEROCITY_PER_LEVEL * level);
    }

    public static double registerHit(SkyBlockPlayer player) {
        int level = heldLevel(player);
        if (level < 1) return 0;
        long now = System.currentTimeMillis();
        Stacks previous = STACKS.get(player.getUuid());
        double bonus = previous == null ? 0 : previous.bonus();
        long elapsed = previous == null ? Long.MAX_VALUE : now - previous.lastHit();
        double next = nextBonus(bonus, elapsed, level);
        STACKS.put(player.getUuid(), new Stacks(next, now));
        return next;
    }

    private static int heldLevel(SkyBlockPlayer player) {
        SkyBlockItem held = PlayerItemOrigin.getFromCache(player.getUuid()).get(PlayerItemOrigin.MAIN_HAND);
        if (held == null || held.isAir()) return 0;
        SkyBlockEnchantment enchantment = held.getAttributeHandler().getEnchantment(EnchantmentType.FATAL_TEMPO);
        if (enchantment == null) return 0;
        return ReforgeLoadout.effectiveEnchantLevel(player, enchantment.type(), enchantment.level());
    }

    @Override
    public String getDescription(int level) {
        return "Attacking increases your <c>⫽ Ferocity <7>by <a>" + (level * 10) + "% <7>per hit, capped at <a>200% <7>for <a>3 <7>seconds after your last attack. Ferocity strikes count as hits.";
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
}
