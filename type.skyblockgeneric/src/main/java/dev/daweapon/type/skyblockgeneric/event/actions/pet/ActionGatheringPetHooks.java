package dev.daweapon.type.skyblockgeneric.event.actions.pet;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.GatheringPetDrops;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetAbilities;

// Gathering pet drops from mobs, and the Endermite's extra End Stone and experience orbs
public class ActionGatheringPetHooks implements HypixelEventClass {
    public static final long END_STONE_ORB_EXPERIENCE = 1;


    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onKill(PlayerKilledSkyBlockMobEvent event) {
        GatheringPetDrops.mobKilled(event.getPlayer(), event.getKilledMob());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onBlockBreak(CustomBlockBreakEvent event) {
        if (event.getMaterial() != Material.END_STONE || Boolean.TRUE.equals(event.getPlayerPlaced())) return;
        SkyBlockPlayer player = event.getPlayer();
        double chance = PetAbilities.value(player, ItemType.ENDERMITE_PET, "More Stonks", 0);
        if (chance <= 0) return;
        player.addExperience(END_STONE_ORB_EXPERIENCE);
        if (LootRoll.roll(player.getUuid(), ItemType.END_STONE, chance)) {
            player.addAndUpdateItem(ItemType.END_STONE, 1);
        }
    }
}
