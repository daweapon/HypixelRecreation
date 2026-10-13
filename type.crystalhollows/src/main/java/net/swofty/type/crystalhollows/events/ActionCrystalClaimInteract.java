package net.swofty.type.crystalhollows.events;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.swofty.type.crystalhollows.claim.CrystalClaims;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionCrystalClaimInteract implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN) return;
        NucleusCrystal crystal = CrystalClaims.crystalAt(event.getBlockPosition());
        if (crystal == null) return;
        event.setBlockingItemUse(true);
        CrystalClaims.claim((SkyBlockPlayer) event.getPlayer(), crystal);
    }
}
