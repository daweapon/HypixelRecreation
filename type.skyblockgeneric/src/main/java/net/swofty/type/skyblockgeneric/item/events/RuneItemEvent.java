package net.swofty.type.skyblockgeneric.item.events;

import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import net.swofty.type.skyblockgeneric.item.rune.RuneEffect;
import net.swofty.type.skyblockgeneric.item.rune.RuneEffects;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class RuneItemEvent implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, isAsync = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerKilledSkyBlockMobEvent event) {
        SkyBlockPlayer player = event.getPlayer();
        var position = event.getKilledMob().getPosition();

        RuneEffects.active(player.getStatistics().getRunedItems(), RuneEffect.Trigger.KILL)
                .forEach(active -> RuneEffects.emit(player, position, active));
    }
}
