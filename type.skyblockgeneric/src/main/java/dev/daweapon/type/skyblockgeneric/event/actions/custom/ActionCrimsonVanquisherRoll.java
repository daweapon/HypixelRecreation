package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.instance.Instance;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.crimson.CrimsonLocation;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.VanquisherSpawns;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;

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
