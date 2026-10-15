package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsZones;
import dev.daweapon.type.skyblockgeneric.crystalhollows.HeatService;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.MiningPetBonuses;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionBalDispersion implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, phase = EventPhase.GAMEPLAY)
    public void run(PlayerKilledSkyBlockMobEvent event) {
        SkyBlockPlayer player = event.getPlayer();
        if (player.getRegion() == null || !CrystalHollowsZones.isCrystalHollows(player.getRegion().getType())) return;
        HeatService.reduce(player, MiningPetBonuses.dispersionHeat(player));
    }
}
