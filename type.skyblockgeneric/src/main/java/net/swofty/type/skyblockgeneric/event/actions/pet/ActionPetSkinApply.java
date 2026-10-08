package net.swofty.type.skyblockgeneric.event.actions.pet;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.entity.PetEntityImpl;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetSkinComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetSkins;

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
