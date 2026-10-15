package dev.daweapon.type.skyblockgeneric.region.rules;

import net.minestom.server.potion.Potion;
import net.minestom.server.potion.TimedPotion;
import dev.daweapon.type.skyblockgeneric.potion.CookieBuffService;
import dev.daweapon.type.skyblockgeneric.potion.GodPotionService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;

import java.util.ArrayList;
import java.util.List;

/**
 * One player's buffs taken off for a while: each keeps the time it had left and gets exactly that back on
 * {@link #restore()}. The Booster Cookie is not a potion effect and stays on.
 */
final class PausedBuffs {
    private final SkyBlockPlayer player;
    private final List<PausedStatistic> statistics = new ArrayList<>();
    private final List<Potion> potions = new ArrayList<>();

    PausedBuffs(SkyBlockPlayer player) {
        this.player = player;
    }

    boolean pause() {
        GodPotionService.pauseClock(player);
        sweep();
        long alive = player.getAliveTicks();
        for (TimedPotion timed : List.copyOf(player.getActiveEffects())) {
            Potion potion = timed.potion();
            if (potion.duration() == Potion.INFINITE_DURATION) {
                potions.add(potion);
                continue;
            }
            int left = (int) (potion.duration() - (alive - timed.startingTicks()));
            if (left > 0) potions.add(new Potion(potion.effect(), potion.amplifier(), left, potion.flags()));
        }
        player.clearEffects();
        return hasAny();
    }

    void sweep() {
        long now = System.currentTimeMillis();
        player.getStatistics().removeTemporaryStatistics(statistic -> {
            if (CookieBuffService.isCookieBuff(statistic)) return false;
            long left = statistic.getExpiration() - now;
            if (left > 0) statistics.add(new PausedStatistic(statistic, left));
            return true;
        });
    }

    boolean restore() {
        boolean any = hasAny();
        long now = System.currentTimeMillis();
        for (PausedStatistic paused : statistics) {
            paused.statistic.setExpiration(now + paused.remainingMs);
            player.getStatistics().boostStatistic(paused.statistic);
        }
        statistics.clear();
        potions.forEach(player::addEffect);
        potions.clear();
        GodPotionService.resumeClock(player);
        return any;
    }

    private boolean hasAny() {
        return !statistics.isEmpty() || !potions.isEmpty();
    }

    void forget() {
        GodPotionService.resumeClock(player);
    }

    private record PausedStatistic(TemporaryStatistic statistic, long remainingMs) {
    }
}
