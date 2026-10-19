package dev.daweapon.type.theend;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class RemnantOfTheEye {
    private static final int INVULNERABLE_TICKS = 60;
    private static final double VOID_Y = -5;

    private RemnantOfTheEye() {
    }

    public static boolean saves(SkyBlockPlayer player) {
        var region = SkyBlockRegion.getRegionOfPosition(player.getPosition());
        boolean voidDeath = player.getPosition().y() < VOID_Y;
        if (region != null && region.getType() == RegionType.VOID_SEPULTURE) {
            if (hasRemnant(player)) player.sendMessage("<c>Your Remnant of the Eye isn't strong enough for the magic of the Sepulture!");
            return false;
        }
        if (!player.removeItemFromPlayer(ItemType.REMNANT_OF_THE_EYE, 1)) return false;
        player.setHealth(player.getMaxHealth());
        if (voidDeath) {
            player.teleport(HypixelConst.getTypeLoader().getLoaderValues().spawnPosition().apply(player.getOriginServer()));
            player.sendMessage("<5>Your Remnant of the Eye saved you from certain death! You were safely teleported back to spawn.");
        } else {
            player.sendMessage("<5>Your Remnant of the Eye saved you from certain death!");
        }
        player.setInvulnerable(true);
        MinecraftServer.getSchedulerManager().buildTask(() -> player.setInvulnerable(false))
                .delay(TaskSchedule.tick(INVULNERABLE_TICKS)).schedule();
        return true;
    }

    private static boolean hasRemnant(SkyBlockPlayer player) {
        return player.removeItemFromPlayer(ItemType.REMNANT_OF_THE_EYE, 1) && give(player);
    }

    private static boolean give(SkyBlockPlayer player) {
        player.addAndUpdateItem(ItemType.REMNANT_OF_THE_EYE, 1);
        return true;
    }
}
