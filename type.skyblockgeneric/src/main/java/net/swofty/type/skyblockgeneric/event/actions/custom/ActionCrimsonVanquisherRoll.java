package net.swofty.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.instance.Instance;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crimson.CrimsonLocation;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.VanquisherSpawns;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;

// Every enemy killed on the Crimson Isle, lava Sea Creatures included, can raise a Vanquisher.
public class ActionCrimsonVanquisherRoll implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, phase = EventPhase.GAMEPLAY)
    public void run(PlayerKilledSkyBlockMobEvent event) {
        if (!CrimsonLocation.isCrimsonIsleServer()) return;
        SkyBlockMob mob = event.getKilledMob();
        if (mob instanceof CrimsonIsleMob crimson && !crimson.countsTowardVanquisher()) return;
        Instance instance = mob.getInstance();
        if (instance == null) return;
        VanquisherSpawns.onKill(event.getPlayer(), mob.getPosition(), instance);
    }
}
