package net.swofty.type.skyblockgeneric.accessory.handler;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.TemporaryStatistic;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Kill-triggered accessories: the Devour Ring's heal on Undead kills and the Reaper Orb's Strength stacks. */
public final class KillAccessories {
    private static final double DEVOUR_HEAL = 10;
    private static final long DEVOUR_COOLDOWN_MILLIS = 500;
    private static final double REAPER_STRENGTH_PER_KILL = 2;
    private static final long REAPER_WINDOW_MILLIS = 5_000;
    private static final Map<UUID, Long> DEVOUR_READY_AT = new ConcurrentHashMap<>();

    private KillAccessories() {
    }

    public static void onKill(SkyBlockPlayer player, SkyBlockMob mob) {
        devour(player, mob);
        reap(player);
    }

    private static void devour(SkyBlockPlayer player, SkyBlockMob mob) {
        if (mob == null || !mob.getMobTypes().contains(MobType.UNDEAD)) return;
        if (!UsableAccessory.usable(player, ItemType.DEVOUR_RING)) return;
        long now = System.currentTimeMillis();
        if (now < DEVOUR_READY_AT.getOrDefault(player.getUuid(), 0L)) return;
        DEVOUR_READY_AT.put(player.getUuid(), now + DEVOUR_COOLDOWN_MILLIS);
        player.setHealth((float) Math.min(player.getMaxHealth(), player.getHealth() + DEVOUR_HEAL));
    }

    private static void reap(SkyBlockPlayer player) {
        if (!UsableAccessory.usable(player, ItemType.REAPER_ORB)) return;
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(ItemStatistics.builder().withBase(ItemStatistic.STRENGTH, REAPER_STRENGTH_PER_KILL).build())
                .withExpirationInMs(REAPER_WINDOW_MILLIS)
                .build());
    }
}
