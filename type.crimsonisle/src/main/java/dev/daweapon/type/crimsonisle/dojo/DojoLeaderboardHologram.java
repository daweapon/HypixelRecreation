package dev.daweapon.type.crimsonisle.dojo;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.hologram.ServerHolograms;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoTest;
import dev.daweapon.type.skyblockgeneric.leaderboard.SkyBlockBoards;
import dev.daweapon.type.skyblockgeneric.leaderboard.SkyBlockLeaderboards;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.List;

/**
 * The Dojo leaderboard next to Master Tao (wiki: Dojo): the 10 best scores of each test, cycling through the seven
 * tests. Rows come from the per-profile SkyBlock leaderboards, so staff profiles are never listed.
 */
public final class DojoLeaderboardHologram {
    static final Pos POSITION = new Pos(-230.5, 109.5, -604.5);
    static final int ROTATE_SECONDS = 10;
    static final int LINES = SkyBlockLeaderboards.TOP_SIZE + 2;

    private static ServerHolograms.ExternalHologram hologram;
    private static int index;

    private DojoLeaderboardHologram() {
    }

    public static void start() {
        if (hologram != null) return;
        hologram = ServerHolograms.ExternalHologram.builder()
                .instance(HypixelConst.getInstanceContainer())
                .pos(POSITION)
                .text(placeholder(DojoTest.values()[0]))
                .build();
        ServerHolograms.addExternalHologram(hologram);
        MinecraftServer.getSchedulerManager().buildTask(DojoLeaderboardHologram::rotate)
                .repeat(TaskSchedule.seconds(ROTATE_SECONDS)).schedule();
    }

    private static void rotate() {
        DojoTest test = DojoTest.values()[index];
        index = (index + 1) % DojoTest.values().length;
        Thread.startVirtualThread(() -> {
            String[] text = lines(test);
            MinecraftServer.getSchedulerManager().scheduleNextTick(
                    () -> ServerHolograms.updateExternalHologramText(hologram.getUuid(), text));
        });
    }

    static String[] lines(DojoTest test) {
        List<SkyBlockLeaderboards.Row> top = SkyBlockBoards.fixed(SkyBlockBoards.dojoId(test))
                .map(board -> SkyBlockLeaderboards.load(board, null).top())
                .orElse(List.of());
        List<String> lines = new ArrayList<>();
        lines.add("<6><l>" + test.fullName());
        lines.add("<7>Top 10 Best Scores");
        for (int i = 0; i < SkyBlockLeaderboards.TOP_SIZE; i++) {
            lines.add(i < top.size() ? row(top.get(i)) : "<e>#" + (i + 1) + " <8>---");
        }
        return lines.toArray(String[]::new);
    }

    private static String row(SkyBlockLeaderboards.Row row) {
        return "<e>#" + row.rank() + " <7>" + row.owner() + " <8>- <6>"
                + StringUtility.commaify((long) row.score());
    }

    private static String[] placeholder(DojoTest test) {
        String[] text = new String[LINES];
        text[0] = "<6><l>" + test.fullName();
        text[1] = "<7>Top 10 Best Scores";
        for (int i = 2; i < LINES; i++) text[i] = "<e>#" + (i - 1) + " <8>---";
        return text;
    }
}
