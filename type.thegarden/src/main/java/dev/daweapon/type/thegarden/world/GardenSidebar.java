package dev.daweapon.type.thegarden.world;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.garden.GardenLayout;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class GardenSidebar {
    private GardenSidebar() {
    }

    public static @Nullable Text area(SkyBlockPlayer player) {
        int plot = lockedPlot(player);
        if (plot == GardenLayout.NONE) return Text.of("<a>The Garden" + pests(Gardens.profile(player).pests().aliveCount()));
        return plotName(plot);
    }

    public static @Nullable List<Text> detail(SkyBlockPlayer player) {
        int plot = plotOf(player);
        if (plot == GardenLayout.NONE || plot == GardenLayout.BARN) return List.of();
        if (Gardens.profile(player).isPlotUnlocked(plot)) {
            return List.of(Text.key("scoreboard.skyblock.region_line",
                    Text.of("<a>Plot <7>- <b>{}" + pests(Gardens.profile(player).pests().aliveIn(plot)), plot)));
        }
        return List.of(Text.of("<c>   Locked"));
    }

    private static int lockedPlot(SkyBlockPlayer player) {
        int plot = plotOf(player);
        if (plot == GardenLayout.NONE || plot == GardenLayout.BARN) return GardenLayout.NONE;
        return Gardens.profile(player).isPlotUnlocked(plot) ? GardenLayout.NONE : plot;
    }

    private static int plotOf(SkyBlockPlayer player) {
        return GardenLayout.plotAt(player.getPosition().x(), player.getPosition().z());
    }

    private static String pests(int count) {
        return count > 0 ? " <c>ൠ x" + count : "";
    }

    private static Text plotName(int plot) {
        return Text.of("<a>Plot <7>- <b>{}", plot);
    }
}
