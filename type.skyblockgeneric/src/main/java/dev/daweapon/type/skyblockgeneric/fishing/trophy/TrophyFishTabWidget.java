package dev.daweapon.type.skyblockgeneric.fishing.trophy;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointTrophyFish;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * The Crimson Isle "Trophy Fish" tab widget: Odger rank, fish discovered and the Golden Fish timer. The wiki shows
 * the timer is in the tab list but not the widget's exact lines; this layout is an estimate.
 */
public final class TrophyFishTabWidget {
    private TrophyFishTabWidget() {
    }

    public static List<String> lines(SkyBlockPlayer player) {
        DatapointTrophyFish.TrophyFishData data = player.getTrophyFishData();
        TrophyFisherRank rank = TrophyFisherRank.highest(data);
        long discovered = FishingRegistry.getTrophyFish().stream()
                .filter(fish -> data.getFish().containsKey(fish.id()) && data.getFish().get(fish.id()).getTotalCatches() > 0)
                .count();
        List<String> lines = new ArrayList<>();
        lines.add("<6><l>Trophy Fish:");
        lines.add(" <7>Rank: " + (rank == null ? "<8>None" : rank.coloredName()));
        lines.add(" <7>Discovered: <a>" + discovered + "<8>/" + FishingRegistry.getTrophyFish().size());
        lines.add(" <7>Golden Fish: " + goldenFish(player));
        return lines;
    }

    private static String goldenFish(SkyBlockPlayer player) {
        if (GoldenFishService.hasSurfaced(player)) return "<6>Surfaced!";
        long fishing = GoldenFishService.fishingMillis(player);
        if (fishing <= 0) return "<8>Not fishing";
        long left = GoldenFishService.eligibleAfter(player) - fishing;
        if (left <= 0) return "<a>Could appear!";
        return "<e>" + (left / 60_000) + "m " + (left / 1000 % 60) + "s";
    }
}
