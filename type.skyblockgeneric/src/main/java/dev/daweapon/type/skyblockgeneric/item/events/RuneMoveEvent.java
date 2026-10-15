package dev.daweapon.type.skyblockgeneric.item.events;

import net.minestom.server.event.player.PlayerMoveEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.item.rune.RuneEffect;
import dev.daweapon.type.skyblockgeneric.item.rune.RuneEffects;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RuneMoveEvent implements HypixelEventClass {
    private static final long INTERVAL_MILLIS = 150;
    private static final Map<UUID, Long> LAST_EMIT = new ConcurrentHashMap<>();

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        long now = System.currentTimeMillis();
        Long last = LAST_EMIT.get(player.getUuid());
        if (last != null && now - last < INTERVAL_MILLIS) return;
        LAST_EMIT.put(player.getUuid(), now);

        RuneEffects.active(player.getStatistics().getRunedItems(), RuneEffect.Trigger.MOVE)
                .forEach(active -> RuneEffects.emit(player, event.getNewPosition(), active));
    }
}
