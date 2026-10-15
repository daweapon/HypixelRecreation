package dev.daweapon.type.generic.event.actions.data;

import lombok.SneakyThrows;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.data.domain.PlayerDataService;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.resourcepack.ResourcePackManager;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.flow.PlayerFlow;

public class ActionPlayerDataLoad implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER_DATA, requireDataLoaded = false, phase = EventPhase.LOAD_DATA)
    public void run(AsyncPlayerConfigurationEvent event) {
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        ServerType type = HypixelConst.getTypeLoader().getType();
        PlayerFlow.run(player, "data/load", () -> {
            PlayerDataService.loadAll(type, player.getUuid());
            PlayerDataService.attachAll(type, player);
        });

        ResourcePackManager packManager = HypixelConst.getResourcePackManager();
        if (packManager != null) {
            PlayerFlow.run(player, "data/resource-pack", () -> packManager.sendPackBlocking(player, 25));
        }
    }
}
