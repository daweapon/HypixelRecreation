package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Shared by Wither Impact and the standalone Wither Shield ability: 10% damage reduction for
// 5 seconds, plus an absorption shield at most once every 20 seconds. Real-Hypixel absorption
// scales with Catacombs level via (12 + level * 0.32) * 50 - there is no Catacombs-level accessor
// in this codebase yet, so level is treated as 0 until that system exists.
public class WitherShieldEffect {
    private static final double DAMAGE_REDUCTION_PERCENT = 0.10;
    private static final int DURATION_TICKS = 100;
    private static final long SHIELD_COOLDOWN_MS = 20_000;
    private static final Map<UUID, Long> lastShield = new ConcurrentHashMap<>();

    public static void apply(SkyBlockPlayer player) {
        player.getStatistics().boostIncomingDamageReduction(DAMAGE_REDUCTION_PERCENT, DURATION_TICKS);

        long now = System.currentTimeMillis();
        Long last = lastShield.get(player.getUuid());
        if (last != null && now - last < SHIELD_COOLDOWN_MS) return;
        lastShield.put(player.getUuid(), now);

        int catacombsLevel = 0;
        float shieldAmount = (float) ((12 + catacombsLevel * 0.32) * 50);

        player.setAdditionalHearts(player.getAdditionalHearts() + shieldAmount);
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            player.setAdditionalHearts(Math.max(player.getAdditionalHearts() - shieldAmount, 0));
        }, TaskSchedule.tick(DURATION_TICKS), TaskSchedule.stop());
    }
}
