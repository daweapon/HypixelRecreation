package dev.daweapon.type.skyblockgeneric.farming.lab;

import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

public final class LabEntrance {
    public static final int X = 175;
    public static final int Y = 42;
    public static final int Z = -470;

    private LabEntrance() {
    }

    public static boolean isEntrance(Point position) {
        return position.blockX() == X && position.blockY() == Y && position.blockZ() == Z;
    }

    public static boolean handle(SkyBlockPlayer player, Point position, Block block) {
        if (!isEntrance(position) || !block.compare(Block.COARSE_DIRT) || player.getSkyblockDataHandler() == null) return false;
        if (player.getInstance() == null) return true;
        if (!LabQuest.canDig(player)) {
            player.sendMessage("<c>This dirt is packed too tight to dig through.");
            return true;
        }
        player.getInstance().setBlock(position, Block.AIR);
        if (!ChapterTriggers.done(player, LabQuest.OPENED)) {
            ChapterTriggers.fire(player, LabQuest.OPENED);
            player.sendMessage("<a>You dug through the dirt and uncovered a hidden passage!");
            player.sendMessage("<7>Tell <e>Beth<7> what you found the next time she visits your Garden.");
        }
        return true;
    }
}
