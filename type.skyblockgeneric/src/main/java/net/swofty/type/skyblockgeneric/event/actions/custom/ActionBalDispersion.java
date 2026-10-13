package net.swofty.type.skyblockgeneric.event.actions.custom;

import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalHollowsZones;
import net.swofty.type.skyblockgeneric.crystalhollows.HeatService;
import net.swofty.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering.MiningPetBonuses;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionBalDispersion implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, phase = EventPhase.GAMEPLAY)
    public void run(PlayerKilledSkyBlockMobEvent event) {
        SkyBlockPlayer player = event.getPlayer();
        if (player.getRegion() == null || !CrystalHollowsZones.isCrystalHollows(player.getRegion().getType())) return;
        HeatService.reduce(player, MiningPetBonuses.dispersionHeat(player));
    }
}
