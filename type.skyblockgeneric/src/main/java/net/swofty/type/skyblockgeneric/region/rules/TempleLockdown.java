package net.swofty.type.skyblockgeneric.region.rules;

import net.minestom.server.potion.PotionEffect;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TempleLockdown {
    public static final double SPEED_LIMIT = 100;
    private static final Set<UUID> LOCKED = ConcurrentHashMap.newKeySet();
    private static boolean registered;

    private TempleLockdown() {
    }

    public static boolean isLocked(SkyBlockPlayer player) {
        return LOCKED.contains(player.getUuid());
    }

    public static synchronized void enter(SkyBlockPlayer player) {
        if (!registered) {
            registered = true;
            ZoneRuleService.register(ZoneRule.regions("jungle_temple", Set.of(RegionType.JUNGLE_TEMPLE), true, null));
        }
        if (!LOCKED.add(player.getUuid())) return;
        apply(player, true);
    }

    public static void leave(SkyBlockPlayer player) {
        if (!LOCKED.remove(player.getUuid())) return;
        apply(player, false);
    }

    public static void forget(UUID player) {
        LOCKED.remove(player);
    }

    public static double speedCap(SkyBlockPlayer player) {
        return limitSpeed(isLocked(player), Double.MAX_VALUE);
    }

    public static double limitSpeed(boolean locked, double speed) {
        return locked ? Math.min(speed, SPEED_LIMIT) : speed;
    }

    private static void apply(SkyBlockPlayer player, boolean locked) {
        if (player.getSkyblockDataHandler() != null) {
            player.getPetData().setSuspended(locked);
            player.getStatistics().invalidateStatistics();
        }
        if (locked) player.removeEffect(PotionEffect.JUMP_BOOST);
        ZoneRuleService.refresh(player);
    }
}
