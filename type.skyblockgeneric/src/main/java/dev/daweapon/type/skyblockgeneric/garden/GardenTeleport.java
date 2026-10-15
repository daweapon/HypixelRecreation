package dev.daweapon.type.skyblockgeneric.garden;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.garden.npc.GardenGround;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.garden.SkyBlockGarden;

public final class GardenTeleport {
    private static final Pos BARN_SPAWN = new Pos(-5.5, 71, 17.5, 180, 0);

    private GardenTeleport() {
    }

    public static boolean inGarden(SkyBlockPlayer player) {
        return player.getInstance() != null && player.getInstance() == SkyBlockGarden.of(player).getGardenInstance();
    }

    public static boolean toPlot(SkyBlockPlayer player, int plot) {
        if (!inGarden(player)) {
            player.sendMessage("<c>You can only teleport to plots while in your Garden!");
            return false;
        }
        Pos target;
        if (plot == GardenLayout.BARN) {
            target = BARN_SPAWN;
        } else {
            int[] origin = GardenLayout.plotOrigin(plot);
            if (origin == null || !Gardens.profile(player).isPlotUnlocked(plot)) {
                player.sendMessage("<c>You haven't unlocked Plot - " + plot + "<c>!");
                return false;
            }
            target = new Pos(origin[0] + GardenLayout.PLOT_SIZE / 2.0, 72, origin[1] + GardenLayout.PLOT_SIZE / 2.0);
        }
        Instance instance = player.getInstance();
        Pos wanted = target;
        instance.loadChunk(wanted).thenRun(() -> {
            Pos spot = plot == GardenLayout.BARN ? wanted : GardenGround.onGround(instance, wanted);
            player.teleport(spot);
        });
        return true;
    }
}
