package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsProgress;
import dev.daweapon.type.skyblockgeneric.healing.VitalityRegen;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Shared by Wither Impact and the standalone Wither Shield: costs 50 Vitality (no Vitality, no effect), grants 10% damage
// reduction for 5 seconds and an absorption shield of (12 + Catacombs level * 0.32) * 50, then refunds Vitality in
// proportion to the shield left unused. The shield can be granted at most once every 20 seconds.
public class WitherShieldEffect {
    public static final int VITALITY_COST = 50;
    private static final double DAMAGE_REDUCTION_PERCENT = 0.10;
    private static final int DURATION_TICKS = 100;
    private static final long SHIELD_COOLDOWN_MS = 20_000;
    private static final Map<UUID, Long> lastShield = new ConcurrentHashMap<>();

    public static double shieldAmount(int catacombsLevel) {
        return (12 + catacombsLevel * 0.32) * 50;
    }

    public static double refund(double shield, double remaining) {
        if (shield <= 0) return 0;
        return VITALITY_COST * Math.max(0, Math.min(shield, remaining)) / shield;
    }

    public static boolean apply(SkyBlockPlayer player) {
        if (player.getVitality() < VITALITY_COST) return false;
        player.setVitality(player.getVitality() - VITALITY_COST);
        player.getStatistics().boostIncomingDamageReduction(DAMAGE_REDUCTION_PERCENT, DURATION_TICKS);

        long now = System.currentTimeMillis();
        Long last = lastShield.get(player.getUuid());
        if (last != null && now - last < SHIELD_COOLDOWN_MS) return true;
        lastShield.put(player.getUuid(), now);

        float shield = (float) shieldAmount(CatacombsProgress.level(player));
        player.setAdditionalHearts(player.getAdditionalHearts() + shield);
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            float remaining = Math.min(shield, player.getAdditionalHearts());
            player.setAdditionalHearts(Math.max(player.getAdditionalHearts() - shield, 0));
            VitalityRegen.grant(player, refund(shield, remaining));
        }, TaskSchedule.tick(DURATION_TICKS), TaskSchedule.stop());
        return true;
    }
}
