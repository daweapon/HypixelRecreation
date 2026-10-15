package dev.daweapon.type.crystalhollows.quest;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Function;

public final class KingsScentWater {
    private static final double[] BODY_HEIGHTS = {0.0, 0.9, 1.62};
    private static boolean started;

    private KingsScentWater() {
    }

    public static synchronized void start() {
        if (started) return;
        started = true;
        MinecraftServer.getSchedulerManager().buildTask(() -> SkyBlockGenericLoader.getLoadedPlayers().forEach(KingsScentWater::check))
                .repeat(TaskSchedule.tick(5)).schedule();
    }

    public static boolean isWet(Block block) {
        return block.compare(Block.WATER) || "true".equals(block.getProperty("waterlogged"));
    }

    public static boolean touchesWater(Function<Point, Block> blocks, Point feet) {
        for (double height : BODY_HEIGHTS) {
            if (isWet(blocks.apply(feet.add(0, height, 0)))) return true;
        }
        return false;
    }

    public static void check(SkyBlockPlayer player) {
        if (player.getInstance() == null || !CrystalHollowsProgress.hasKingsScent(player.getUuid())) return;
        if (!touchesWater(point -> player.getInstance().getBlock(point), player.getPosition())) return;
        CrystalHollowsProgress.clearKingsScent(player.getUuid());
        player.sendMessage(Text.of("<c>The water washed away King Yolkar's scent!"));
    }
}
