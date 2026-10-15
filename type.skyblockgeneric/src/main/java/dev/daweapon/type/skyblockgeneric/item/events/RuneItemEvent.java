package dev.daweapon.type.skyblockgeneric.item.events;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.item.rune.RuneEffect;
import dev.daweapon.type.skyblockgeneric.item.rune.RuneEffects;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class RuneItemEvent implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, isAsync = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerKilledSkyBlockMobEvent event) {
        SkyBlockPlayer player = event.getPlayer();
        var position = event.getKilledMob().getPosition();

        RuneEffects.active(player.getStatistics().getRunedItems(), RuneEffect.Trigger.KILL)
                .forEach(active -> RuneEffects.emit(player, position, active));
    }
}
