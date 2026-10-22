package dev.daweapon.type.skyblockgeneric.beacon;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import net.minestom.server.item.Material;

public enum BeaconStat {
    HEALTH(ItemStatistic.HEALTH, 10, "<c>❤ Health", Material.GOLDEN_APPLE),
    DEFENSE(ItemStatistic.DEFENSE, 4, "<a>❈ Defense", Material.IRON_CHESTPLATE),
    STRENGTH(ItemStatistic.STRENGTH, 2, "<c>❁ Strength", Material.BLAZE_POWDER),
    SPEED(ItemStatistic.SPEED, 3, "<f>✦ Speed", Material.SUGAR),
    CRITICAL_CHANCE(ItemStatistic.CRITICAL_CHANCE, 1, "<9>☣ Crit Chance", Material.NETHER_STAR),
    CRITICAL_DAMAGE(ItemStatistic.CRITICAL_DAMAGE, 2, "<9>☠ Crit Damage", Material.IRON_SWORD),
    INTELLIGENCE(ItemStatistic.INTELLIGENCE, 5, "<b>✎ Intelligence", Material.ENCHANTED_BOOK),
    BONUS_ATTACK_SPEED(ItemStatistic.BONUS_ATTACK_SPEED, 1, "<e>⚔ Bonus Attack Speed", Material.GOLDEN_SWORD),
    SEA_CREATURE_CHANCE(ItemStatistic.SEA_CREATURE_CHANCE, 1, "<3>α Sea Creature Chance", Material.FISHING_ROD),
    MAGIC_FIND(ItemStatistic.MAGIC_FIND, 1, "<b>✯ Magic Find", Material.ENDER_EYE),
    TRACKING(ItemStatistic.TRACKING, 0.5, "<d>⌖ Tracking", Material.SPYGLASS),
    ABILITY_DAMAGE(ItemStatistic.ABILITY_DAMAGE, 1, "<c>๑ Ability Damage", Material.BLAZE_ROD);

    public static final int MAX_TIER = 5;

    private final ItemStatistic statistic;
    private final double perTier;
    private final String label;
    private final Material icon;

    BeaconStat(ItemStatistic statistic, double perTier, String label, Material icon) {
        this.statistic = statistic;
        this.perTier = perTier;
        this.label = label;
        this.icon = icon;
    }

    public ItemStatistic statistic() {
        return statistic;
    }

    public String label() {
        return label;
    }

    public Material icon() {
        return icon;
    }

    public double valueAtTier(int tier) {
        return perTier * tier;
    }

    public static BeaconStat parse(String name) {
        if (name == null || name.isBlank()) return null;
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
