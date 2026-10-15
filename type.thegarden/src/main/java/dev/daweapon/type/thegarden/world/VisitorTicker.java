package dev.daweapon.type.thegarden.world;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.garden.composter.ComposterService;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseItems;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestService;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestVacuum;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.garden.SkyBlockGarden;

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
                    PestService.tick(player);
                    GreenhouseService.tick(player);
                    break;
                }
            }
        }
    }

    public static void tickVacuums() {
        for (SkyBlockGarden garden : SkyBlockGarden.loaded()) {
            Instance instance = garden.getGardenInstance();
            if (instance == null) continue;
            PestVacuum.tick(instance);
            GreenhouseItems.tick(instance);
        }
    }
}
