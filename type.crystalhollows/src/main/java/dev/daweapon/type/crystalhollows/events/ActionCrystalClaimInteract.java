package dev.daweapon.type.crystalhollows.events;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import dev.daweapon.type.crystalhollows.claim.CrystalClaims;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
