package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.farmingcontest.FarmingContest;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class JacobsContestWidget extends AreaWidget {
    private static final long TICK_MILLIS = 50;

    JacobsContestWidget() {
        super("jacobs_contest", "Jacob's Contest", Material.GOLDEN_HOE, 60, TabLocation.GARDEN);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        Optional<FarmingContest> active = FarmingContest.active();
        FarmingContest contest = active.orElseGet(() -> FarmingContest.upcoming(1).getFirst());
        long elapsed = SkyBlockCalendar.getElapsed();
        long remaining = ((active.isPresent() ? contest.endElapsed() : contest.startElapsed()) - elapsed) * TICK_MILLIS;
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<a><l>Jacob's Contest:"));
        lines.add(Text.of(active.isPresent() ? " <a>Active <7>(ends in {})" : " <7>Starts In: <a>{}",
                AreaTabClock.compact(remaining)));
        contest.crops().forEach(crop -> lines.add(Text.of(" <f>{}", crop.getDisplayName())));
        return lines;
    }
}
