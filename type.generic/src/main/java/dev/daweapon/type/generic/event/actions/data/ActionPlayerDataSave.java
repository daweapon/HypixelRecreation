package dev.daweapon.type.generic.event.actions.data;

import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.protocol.objects.proxy.to.FinishedWithPlayerProtocol;
import dev.daweapon.commons.redis.RedisClient;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.data.domain.PlayerDataService;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.flow.PlayerFlow;
import dev.daweapon.type.generic.utility.MathUtility;

public class ActionPlayerDataSave implements HypixelEventClass {
    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, isAsync = true, phase = EventPhase.PERSIST)
    public void run(PlayerDisconnectEvent event) {
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        ServerType type = HypixelConst.getTypeLoader().getType();
        PlayerFlow.run(player, "data/save", () -> {
            PlayerDataService.saveAndUnloadAll(type, player);
            RedisClient.requestProxy(new FinishedWithPlayerProtocol(),
                    new FinishedWithPlayerProtocol.Request(player.getUuid().toString()));
            MathUtility.delay(() -> HypixelConst.getTypeLoader().getTablistManager().deleteTablistEntries(player), 5);
        });
    }
}
