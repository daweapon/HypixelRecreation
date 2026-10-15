package dev.daweapon.type.skyblockgeneric.event.actions.player;

import net.minestom.server.event.player.PlayerMoveEvent;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Teleports non-staff players back to spawn if they linger too long outside any region
public class ActionPlayerStrayTooFar implements HypixelEventClass {
    public static Map<UUID, Long> startedStray = new HashMap<>();


    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        SkyBlockRegion region = player.getRegion();

        if (player.getRank().isEqualOrHigherThan(Rank.STAFF)) return;

        if (region != null) {
            startedStray.remove(player.getUuid());
            return;
        }

        if (startedStray.containsKey(player.getUuid())) {
            if (System.currentTimeMillis() - startedStray.get(player.getUuid()) > 5000) {
                player.teleport(HypixelConst.getTypeLoader().getLoaderValues().spawnPosition().apply(
                        player.getOriginServer()
                ));
                startedStray.remove(player.getUuid());
                player.sendMessage("<c>You have strayed too far from the spawn! Teleporting you back...");
            }
        } else {
            startedStray.put(player.getUuid(), System.currentTimeMillis());
        }
    }
}
