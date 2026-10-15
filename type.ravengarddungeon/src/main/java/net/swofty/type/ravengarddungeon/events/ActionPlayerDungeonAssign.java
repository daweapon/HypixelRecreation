package net.swofty.type.ravengarddungeon.events;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.redis.service.GameInformationHandler;
import net.swofty.type.ravengarddungeon.game.DungeonInstanceRegistry;
import net.swofty.type.ravengarddungeon.user.RavengardDungeonPlayer;

import java.util.UUID;

// Routes a player to their orchestrator-assigned dungeon instance on spawn, and marks them as left on disconnect.
public class ActionPlayerDungeonAssign implements HypixelEventClass {
    private static final int ARRIVAL_ATTEMPTS = 25;
    private static final int ARRIVAL_RETRY_TICKS = 4;


    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn() || !(event.getPlayer() instanceof RavengardDungeonPlayer player)) {
            return;
        }
        String assignedGameId = GameInformationHandler.game.remove(player.getUuid());
        if (assignedGameId != null) {
            assign(player, assignedGameId);
            return;
        }
        int[] attempts = {0};
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            String late = GameInformationHandler.game.remove(player.getUuid());
            if (late != null) {
                assign(player, late);
                return TaskSchedule.stop();
            }
            if (!player.isOnline() || ++attempts[0] >= ARRIVAL_ATTEMPTS) return TaskSchedule.stop();
            return TaskSchedule.tick(ARRIVAL_RETRY_TICKS);
        });
    }

    private static void assign(RavengardDungeonPlayer player, String assignedGameId) {
        DungeonInstanceRegistry.DungeonInstance instance;
        try {
            instance = DungeonInstanceRegistry.get(UUID.fromString(assignedGameId));
        } catch (IllegalArgumentException exception) {
            return;
        }
        if (instance == null) {
            player.sendMessage("<c>Your dungeon instance no longer exists.");
            return;
        }
        DungeonInstanceRegistry.sendPlayerIn(player, instance);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.DISCONNECT)
    public void onDisconnect(PlayerDisconnectEvent event) {
        UUID uuid = event.getPlayer().getUuid();
        for (DungeonInstanceRegistry.DungeonInstance instance : DungeonInstanceRegistry.all()) {
            instance.markPlayerLeft(uuid);
        }
    }
}
