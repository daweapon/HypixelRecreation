package dev.daweapon.type.thefarmingislands.events;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.thefarmingislands.shepherd.EntityKeepSheep;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.item.Material;
import net.kyori.adventure.key.Key;

public class ActionShearKeepSheep implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onShear(PlayerEntityInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN) return;
        if (!(event.getTarget() instanceof EntityKeepSheep sheep) || sheep.isSheared()) return;
        if (event.getPlayer().getItemInMainHand().material() != Material.SHEARS) return;
        sheep.shear(System.currentTimeMillis());
        event.getPlayer().playSound(Sound.sound(Key.key("entity.sheep.shear"), Sound.Source.NEUTRAL, 1f, 1f));
    }
}
