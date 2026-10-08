package net.swofty.type.skyblockgeneric.event.actions.pet;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering.GatheringPetDrops;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetAbilities;

// Gathering pet drops from mobs, and the Endermite's extra End Stone
public class ActionGatheringPetHooks implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onKill(PlayerKilledSkyBlockMobEvent event) {
        GatheringPetDrops.mobKilled(event.getPlayer(), event.getKilledMob());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onBlockBreak(CustomBlockBreakEvent event) {
        if (event.getMaterial() != Material.END_STONE || Boolean.TRUE.equals(event.getPlayerPlaced())) return;
        SkyBlockPlayer player = event.getPlayer();
        double chance = PetAbilities.value(player, ItemType.ENDERMITE_PET, "More Stonks", 0);
        if (chance > 0 && LootRoll.roll(player.getUuid(), ItemType.END_STONE, chance)) {
            player.addAndUpdateItem(ItemType.END_STONE, 1);
        }
    }
}
