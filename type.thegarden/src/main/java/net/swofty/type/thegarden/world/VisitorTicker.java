package net.swofty.type.thegarden.world;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterService;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.garden.SkyBlockGarden;

public final class VisitorTicker {
    private VisitorTicker() {
    }

    public static void tick() {
        for (SkyBlockGarden garden : SkyBlockGarden.loaded()) {
            Instance instance = garden.getGardenInstance();
            if (instance == null) continue;
            for (Player online : instance.getPlayers()) {
                if (online instanceof SkyBlockPlayer player) {
                    VisitorService.tick(player);
                    ComposterService.tick(player);
                    break;
                }
            }
        }
    }
}
