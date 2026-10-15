package dev.daweapon.type.thegarden.events.traditional;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseLayout;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
