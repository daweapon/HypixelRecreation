package net.swofty.type.skyblockgeneric.accessory.handler;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tarantula Talisman and Ring: every 10th melee hit that lands on the same enemy deals bonus damage.
 * Swings inside the player's invulnerability window don't land, so they don't count.
 */
public final class TarantulaAccessories {
    private static final int HIT_INTERVAL = 10;
    private static final long MILLIS_PER_TICK = 50;
    private static final Map<UUID, Streak> STREAKS = new ConcurrentHashMap<>();

    private record Streak(UUID target, int hits, long lastHit) {
    }

    private TarantulaAccessories() {
    }

    public static double bonusPercent(SkyBlockPlayer player) {
        if (UsableAccessory.usable(player, ItemType.TARANTULA_RING)) return 15;
        if (UsableAccessory.usable(player, ItemType.TARANTULA_TALISMAN)) return 10;
        return 0;
    }

    public static double meleeMultiplier(SkyBlockPlayer player, SkyBlockMob mob) {
        double bonus = bonusPercent(player);
        if (bonus <= 0) {
            STREAKS.remove(player.getUuid());
            return 1;
        }
        long now = System.currentTimeMillis();
        Streak streak = STREAKS.get(player.getUuid());
        boolean sameTarget = streak != null && streak.target().equals(mob.getUuid());
        if (sameTarget && now - streak.lastHit() < player.getStatistics().getInvulnerabilityTime() * MILLIS_PER_TICK) return 1;
        int hits = sameTarget ? streak.hits() + 1 : 1;
        STREAKS.put(player.getUuid(), new Streak(mob.getUuid(), hits % HIT_INTERVAL, now));
        return hits % HIT_INTERVAL == 0 ? 1 + bonus / 100D : 1;
    }
}
