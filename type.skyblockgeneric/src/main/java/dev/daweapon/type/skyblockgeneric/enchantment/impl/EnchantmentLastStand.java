package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.CatalogueEnchantment;
import dev.daweapon.type.skyblockgeneric.healing.VitalityRegen;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantmentLastStand extends CatalogueEnchantment {
    public static final double LOW_HEALTH = 0.4;
    public static final long COOLDOWN_MS = 30_000;
    public static final long DURATION_MS = 10_000;

    private static final Map<UUID, Long> LAST_TRIGGER = new ConcurrentHashMap<>();

    public EnchantmentLastStand() {
        super("LAST_STAND");
    }

    public static double defensePercent(int level) {
        return level * 2.5;
    }

    public static double vitality(int level) {
        return level * 2D;
    }

    public static boolean crossesThreshold(double health, double damage, double maxHealth) {
        return health - damage < maxHealth * LOW_HEALTH;
    }

    public static void onDamageTaken(SkyBlockPlayer player, double damage) {
        Map<Integer, Integer> pieces = player.getStatistics().getAllOfEnchants(EnchantmentType.LAST_STAND, false);
        if (pieces.isEmpty() || !crossesThreshold(player.getHealth(), damage, player.getMaxHealth())) return;
        long now = System.currentTimeMillis();
        if (!VitalityRegen.offCooldown(LAST_TRIGGER, player.getUuid(), COOLDOWN_MS, now)) return;
        LAST_TRIGGER.put(player.getUuid(), now);

        double defense = 0;
        double vitality = 0;
        for (Map.Entry<Integer, Integer> entry : pieces.entrySet()) {
            defense += defensePercent(entry.getKey()) * entry.getValue();
            vitality += vitality(entry.getKey()) * entry.getValue();
        }
        VitalityRegen.grant(player, vitality);
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(ItemStatistics.builder().withAdditivePercentage(ItemStatistic.DEFENSE, defense).build())
                .withExpirationInMs(DURATION_MS)
                .withDisplayName("Last Stand")
                .withDisplayColor("<a>")
                .build());
    }

    @Override
    public boolean isEffectActive() {
        return true;
    }
}
