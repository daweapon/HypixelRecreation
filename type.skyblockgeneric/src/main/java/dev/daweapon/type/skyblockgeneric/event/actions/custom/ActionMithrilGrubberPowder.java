package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureMob;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.fishing.grubber.MithrilGrubbers;

public class ActionMithrilGrubberPowder implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, phase = EventPhase.GAMEPLAY)
    public void run(PlayerKilledSkyBlockMobEvent event) {
        if (!(event.getKilledMob() instanceof SeaCreatureMob seaCreature)) return;
        MithrilGrubbers.rewardKill(event.getPlayer(), seaCreature.getSeaCreatureId());
    }
}
