package net.swofty.type.thegarden.events.traditional;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseLayout;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionGreenhouseInteract implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        GreenhouseLayout.Spot spot = GreenhouseService.spotAt(Gardens.profile(player), event.getBlockPosition());
        if (spot == null) return;
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        if (GreenhouseService.interact(player, spot, held)) {
            event.setBlockingItemUse(true);
            return;
        }
        if (held.isNA()) return;
        if (GreenhouseService.unlockSlot(player, spot, held) || GreenhouseService.plant(player, spot, held)) {
            event.setBlockingItemUse(true);
        }
    }
}
