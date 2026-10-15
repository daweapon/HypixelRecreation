package dev.daweapon.type.skyblockgeneric.event.actions.pet;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.entity.PetEntityImpl;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PetSkinComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetSkins;

// Right-clicking your summoned pet while holding a Pet Skin applies it
public class ActionPetSkinApply implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onInteract(PlayerEntityInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN) return;
        if (!(event.getTarget() instanceof PetEntityImpl pet) || pet.getOwner() != event.getPlayer()) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (player.getItemInMainHand().isAir()) return;
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        if (!held.hasComponent(PetSkinComponent.class)) return;
        PetSkins.applyFromHand(player, held);
    }
}
