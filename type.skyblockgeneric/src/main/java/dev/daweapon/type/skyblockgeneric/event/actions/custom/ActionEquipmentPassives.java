package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.item.Material;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.item.equipment.passive.Contagion;
import dev.daweapon.type.skyblockgeneric.item.equipment.passive.EquipmentPassives;

public class ActionEquipmentPassives implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onKill(PlayerKilledSkyBlockMobEvent event) {
        EquipmentPassives.onKill(event.getPlayer(), event.getKilledMob());
        Contagion.onKill(event.getPlayer(), event.getKilledMob());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onBreak(CustomBlockBreakEvent event) {
        if (Boolean.TRUE.equals(event.getPlayerPlaced()) || event.getMaterial() != Material.GLOWSTONE) return;
        EquipmentPassives.increment(event.getPlayer(), EquipmentPassives.Counter.GLOWING);
    }
}
