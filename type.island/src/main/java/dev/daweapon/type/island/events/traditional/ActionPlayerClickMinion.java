package dev.daweapon.type.island.events.traditional;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.entity.MinionEntityImpl;
import dev.daweapon.type.skyblockgeneric.gui.inventories.GUIMinion;
import dev.daweapon.type.skyblockgeneric.minion.inferno.CapsaicinEyedrops;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Opens the minion management GUI when a player interacts with a minion entity on their island.
public class ActionPlayerClickMinion implements HypixelEventClass {


    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerEntityInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (event.getTarget() instanceof MinionEntityImpl minion) {
            if (CapsaicinEyedrops.isHeld(player)) {
                if (event.getHand() == PlayerHand.MAIN) CapsaicinEyedrops.apply(player, minion.getIslandMinion());
                return;
            }
            new GUIMinion(minion.getIslandMinion()).open(player);
        }
    }
}
