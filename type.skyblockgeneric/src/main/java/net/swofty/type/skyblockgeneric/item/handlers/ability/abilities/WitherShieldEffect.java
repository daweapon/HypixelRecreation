package net.swofty.type.skyblockgeneric.item.handlers.ability.abilities;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Shared by Wither Impact and the standalone Wither Shield ability: 10% damage reduction and an
// absorption shield for 5 seconds. Real-Hypixel absorption scales with Catacombs level via
// (12 + level * 0.32) * 50 - there is no Catacombs-level accessor in this codebase yet, so level
// is treated as 0 until that system exists (see Roadmap #21 handoff notes).
public class WitherShieldEffect {
    private static final double DAMAGE_REDUCTION_PERCENT = 0.10;
    private static final int DURATION_TICKS = 100; // 5 seconds

    public static void apply(SkyBlockPlayer player) {
        player.getStatistics().boostIncomingDamageReduction(DAMAGE_REDUCTION_PERCENT, DURATION_TICKS);

        int catacombsLevel = 0; // TODO: read the player's real Catacombs level once tracked
        float shieldAmount = (float) ((12 + catacombsLevel * 0.32) * 50);

        player.setAdditionalHearts(player.getAdditionalHearts() + shieldAmount);
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            player.setAdditionalHearts(Math.max(player.getAdditionalHearts() - shieldAmount, 0));
        }, TaskSchedule.tick(DURATION_TICKS), TaskSchedule.stop());
    }
}
