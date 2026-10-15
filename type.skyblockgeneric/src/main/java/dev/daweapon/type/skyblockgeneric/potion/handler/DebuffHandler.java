package dev.daweapon.type.skyblockgeneric.potion.handler;

import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.potion.PotionEffectType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;
import org.jetbrains.annotations.Nullable;

/**
 * Handler for debuff potions (Weakness, Slowness, Poison).
 * Applies negative stat modifiers and visual effects.
 */
public class DebuffHandler implements PotionEffectHandler {

    private final PotionEffectType effectType;

    public DebuffHandler(PotionEffectType effectType) {
        this.effectType = effectType;
    }

    @Override
    public void applyToPlayer(SkyBlockPlayer player, int level, long durationMs) {
        // Apply stat debuff
        ItemStatistics stats = effectType.getStatistics(level);
        if (!stats.getStatisticsBase().isEmpty() ||
            !stats.getStatisticsAdditive().isEmpty() ||
            !stats.getStatisticsMultiplicative().isEmpty()) {

            TemporaryStatistic tempStat = TemporaryStatistic.builder()
                    .withStatistics(stats)
                    .withExpirationInMs(durationMs)
                    .withDisplayName(effectType.getLevelDisplay(level))
                    .withDisplayColor(effectType.getColor())
                    .build();

            player.getStatistics().boostStatistic(tempStat);
        }

        // Apply Minestom visual effect if available
        @Nullable PotionEffect minestomEffect = effectType.getMinestomEffect();
        if (minestomEffect != null) {
            int durationTicks = (int) (durationMs / 50);
            byte amplifier = (byte) Math.max(0, level - 1);
            player.addEffect(new Potion(minestomEffect, amplifier, durationTicks));
        }

        // Handle poison special case - damage over time would be implemented here
        // with a scheduled task if needed
    }

    @Override
    public void applyToMob(SkyBlockMob mob, int level, long durationMs, double distance) {
        double modifier = getDistanceModifier(distance);
        long adjustedDuration = (long) (durationMs * modifier);

        // Apply Minestom visual effect
        @Nullable PotionEffect minestomEffect = effectType.getMinestomEffect();
        if (minestomEffect != null) {
            int durationTicks = (int) (adjustedDuration / 50);
            byte amplifier = (byte) Math.max(0, level - 1);
            mob.addEffect(new Potion(minestomEffect, amplifier, durationTicks));
        }
    }

    @Override
    public boolean affectsMobs() {
        return true;
    }

    @Override
    public boolean affectsOtherPlayers() {
        return false;
    }
}
