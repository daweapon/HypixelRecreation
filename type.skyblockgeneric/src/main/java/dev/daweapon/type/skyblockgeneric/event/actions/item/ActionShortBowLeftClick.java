package dev.daweapon.type.skyblockgeneric.event.actions.item;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.bow.BowShot;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Shortbows also fire on left click, sharing the right click's shot cooldown
public class ActionShortBowLeftClick implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerHandAnimationEvent event) {
        if (event.getHand() != PlayerHand.MAIN || event.getPlayer().getItemInMainHand().material() != Material.BOW) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        BowShot.tryShortBow(player, new SkyBlockItem(player.getItemInMainHand()));
    }
}
