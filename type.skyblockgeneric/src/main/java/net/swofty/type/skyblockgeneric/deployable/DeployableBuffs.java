package net.swofty.type.skyblockgeneric.deployable;

import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.TemporaryStatistic;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DeployableBuffs {
    public static final String BUFF_PREFIX = "Deployable: ";
    static final long LINGER_MS = 1_500;
    private static final Map<UUID, Active> ACTIVE = new ConcurrentHashMap<>();

    private record Active(DeployableType type, long expiresAt) {
    }

    private DeployableBuffs() {
    }

    static void apply(SkyBlockPlayer player, DeployableType type, boolean weather) {
        player.getStatistics().removeTemporaryStatistics(stat -> stat.getDisplayName() != null
                && stat.getDisplayName().startsWith(BUFF_PREFIX));
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(type.statistics(weather))
                .withExpirationInMs(LINGER_MS)
                .withDisplayName(BUFF_PREFIX + type.displayName().replaceAll("<[^>]*>", ""))
                .build());
        ACTIVE.put(player.getUuid(), new Active(type, System.currentTimeMillis() + LINGER_MS));
    }

    public static double manaRegenFraction(SkyBlockPlayer player) {
        Active active = ACTIVE.get(player.getUuid());
        if (active == null) return 0;
        if (active.expiresAt() < System.currentTimeMillis()) {
            ACTIVE.remove(player.getUuid(), active);
            return 0;
        }
        return active.type().manaRegenPercent() / 100D;
    }

    public static void forget(UUID player) {
        ACTIVE.remove(player);
    }
}
