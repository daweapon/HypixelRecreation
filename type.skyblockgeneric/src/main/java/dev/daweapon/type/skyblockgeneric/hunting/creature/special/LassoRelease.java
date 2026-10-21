package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.MinecraftServer;

/**
 */
final class LassoRelease {
    private static final double DISTANCE = 30;
    private static final int RETURN_TICKS = 3;

    private LassoRelease() {
    }

    static void release(SkyBlockPlayer player, HuntingCreatureMob mob) {
        dev.daweapon.type.skyblockgeneric.hunting.creature.LassoService.release(player);
    }
}
