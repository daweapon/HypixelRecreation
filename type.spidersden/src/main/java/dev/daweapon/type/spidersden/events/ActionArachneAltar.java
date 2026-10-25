package dev.daweapon.type.spidersden.events;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.spidersden.ArachneFight;
import dev.daweapon.type.skyblockgeneric.spidersden.ArachneTier;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionArachneAltar implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN) return;
        var at = event.getBlockPosition();
        if (!ArachneFight.isAltar(at.blockX(), at.blockY(), at.blockZ())) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        var held = player.getItemInMainHand();
        if (held.isAir() || !SkyBlockItem.isSkyBlockItem(held)) return;
        String id = new SkyBlockItem(held).getAttributeHandler().getTypeAsString();
        for (ArachneTier tier : ArachneTier.values()) {
            if (!tier.summonItem().equals(id)) continue;
            event.setCancelled(true);
            ArachneFight.place(player, tier);
            return;
        }
    }
}
