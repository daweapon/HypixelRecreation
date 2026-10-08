package net.swofty.type.skyblockgeneric.event.actions.pet;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.player.PlayerInputEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.entity.PetEntityImpl;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.SkillableMineComponent;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetEvents;

// Feeds crop harvests, jumps and clicks on your own pet to the active pets' abilities
public class ActionPetHooks implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onCrop(CustomBlockBreakEvent event) {
        if (Boolean.TRUE.equals(event.getPlayerPlaced()) || event.getMaterial() == null) return;
        SkyBlockItem block = new SkyBlockItem(event.getMaterial());
        if (!block.hasComponent(SkillableMineComponent.class)) return;
        if (block.getComponent(SkillableMineComponent.class).getCategory() != SkillCategories.FARMING) return;
        int crops = event.getDrops().stream().mapToInt(SkyBlockItem::getAmount).sum();
        PetEvents.cropHarvested(event.getPlayer(), event.getMaterial(), Math.max(1, crops));
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onInput(PlayerInputEvent event) {
        if (!event.hasPressedJumpKey() || !event.getPlayer().isOnGround()) return;
        PetEvents.jumped((SkyBlockPlayer) event.getPlayer());
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onInteract(PlayerEntityInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN) return;
        if (!(event.getTarget() instanceof PetEntityImpl pet) || pet.getOwner() != event.getPlayer()) return;
        PetEvents.interacted((SkyBlockPlayer) event.getPlayer());
    }
}
