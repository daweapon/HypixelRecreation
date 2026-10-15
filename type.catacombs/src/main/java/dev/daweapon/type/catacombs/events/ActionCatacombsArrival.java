package dev.daweapon.type.catacombs.events;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.catacombs.TypeCatacombsLoader;
import dev.daweapon.type.catacombs.run.CatacombsRun;
import dev.daweapon.type.catacombs.run.CatacombsRuns;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.redis.service.GameInformationHandler;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

/**
 * Arrivals wait in the empty instance until their run is known, then enter it. The orchestrator's game
 * assignment can land after the player does, so the run is looked up again for a few seconds, and the
 * run's own roster is checked too.
 */
public class ActionCatacombsArrival implements HypixelEventClass {
    private static final int LOOKUP_ATTEMPTS = 40;
    private static final int LOOKUP_INTERVAL_TICKS = 5;

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.CONNECT)
    public void onConfigure(AsyncPlayerConfigurationEvent event) {
        event.setSpawningInstance(HypixelConst.getEmptyInstance());
        event.getPlayer().setRespawnPoint(TypeCatacombsLoader.WAITING_POSITION);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn() || !(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        int[] attempts = {0};
        player.scheduler().submitTask(() -> {
            if (!player.isOnline()) return TaskSchedule.stop();
            CatacombsRun run = findRun(player);
            if (run != null) {
                run.admit(player);
                return TaskSchedule.stop();
            }
            if (++attempts[0] < LOOKUP_ATTEMPTS) return TaskSchedule.tick(LOOKUP_INTERVAL_TICKS);
            sendBack(player);
            return TaskSchedule.stop();
        });
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.DISCONNECT)
    public void onDisconnect(PlayerDisconnectEvent event) {
        GameInformationHandler.game.remove(event.getPlayer().getUuid());
        for (CatacombsRun run : CatacombsRuns.all()) run.handleDisconnect(event.getPlayer().getUuid());
    }

    private static @Nullable CatacombsRun findRun(SkyBlockPlayer player) {
        String assigned = GameInformationHandler.game.remove(player.getUuid());
        CatacombsRun run = assigned == null ? null : CatacombsRuns.get(assigned);
        if (run != null && run.isExpecting(player.getUuid())) return run;
        return CatacombsRuns.expecting(player.getUuid());
    }

    private static void sendBack(SkyBlockPlayer player) {
        if (player.getRank().isEqualOrHigherThan(Rank.STAFF)) {
            player.sendMessage("<7>You aren't in a Catacombs run. Staff can stay here; use <e>/catacombsrun <7>inside a run.");
            return;
        }
        player.sendMessage("<c>Your Catacombs run couldn't be found! Sending you back to the Dungeon Hub...");
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> player.asProxyPlayer()
                .transferToWithIndication(ServerType.SKYBLOCK_DUNGEON_HUB));
    }
}
