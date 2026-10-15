package dev.daweapon.type.skyblockgeneric.crystalhollows;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointFairySouls;
import dev.daweapon.type.skyblockgeneric.fishing.FishingMedium;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.fairysouls.FairySoul;
import dev.daweapon.type.skyblockgeneric.user.fairysouls.FairySoulZone;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FairyGrottoFishing {
    public static final double CHANCE_PERCENT = 0.5;
    public static final int MAX_PER_LOBBY = 2;

    private static final Map<UUID, Integer> FOUND_THIS_LOBBY = new ConcurrentHashMap<>();

    private FairyGrottoFishing() {
    }

    public static void reset() {
        FOUND_THIS_LOBBY.clear();
    }

    public static void onCatch(SkyBlockPlayer player, RegionType region, FishingMedium medium) {
        if (region != RegionType.FAIRY_GROTTO || medium != FishingMedium.WATER) return;
        if (FOUND_THIS_LOBBY.getOrDefault(player.getUuid(), 0) >= MAX_PER_LOBBY) return;
        FairySoul soul = nextUncollected(player);
        if (soul == null || !LootRoll.roll(player.getUuid(), null, CHANCE_PERCENT)) return;
        FOUND_THIS_LOBBY.merge(player.getUuid(), 1, Integer::sum);
        soul.collect(player);
    }

    private static FairySoul nextUncollected(SkyBlockPlayer player) {
        DatapointFairySouls.PlayerFairySouls collected = player.getFairySouls();
        return FairySoul.getFairySouls().stream()
                .filter(soul -> soul.getZone() == FairySoulZone.MISC_FISHING)
                .filter(soul -> !collected.getAllFairySouls().contains(soul.getId()))
                .findFirst().orElse(null);
    }
}
