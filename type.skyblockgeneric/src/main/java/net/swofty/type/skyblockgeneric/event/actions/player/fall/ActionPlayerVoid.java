package net.swofty.type.skyblockgeneric.event.actions.player.fall;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.island.VisitManager;

// Deals fatal void damage to survival/adventure players who fall below y=0. Island visitors cannot
// die, so they are returned to the island spawn instead.
public class ActionPlayerVoid implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (player.getPosition().y() < 0 && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            if (VisitManager.isVisiting(player.getUuid())) {
                player.teleport(player.getRespawnPoint());
                return;
            }
            player.damage(DamageType.OUT_OF_WORLD, Float.MAX_VALUE);
        }
    }
}
