package dev.daweapon.type.skyblockgeneric.foraging;

import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.MoongladeBeacon;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryConditionalStatistic;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class ForagingConditionalStats {
    private static final Set<SkyBlockPlayer> REGISTERED = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private ForagingConditionalStats() {
    }

    public static void ensure(SkyBlockPlayer player) {
        if (!REGISTERED.add(player)) return;
        player.getStatistics().boostStatistic(TemporaryConditionalStatistic.builder()
                .withStatistics(ForagingConditionalStats::statistics)
                .withExpiry(owner -> true)
                .build());
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        return ItemStatistics.add(TreeGiftMilestones.sweepStatistics(player), MoongladeBeacon.statistics(player));
    }
}
