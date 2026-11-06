package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.type.skyblockgeneric.dungeons.queue.CatacombsQueue;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;
import java.util.UUID;

/**
 * {@code /instancerequeue}, what the captured "Click HERE to re-queue into The Catacombs!" line runs: the instance
 * creator queues the same floor again through the normal Catacombs queue (party checks included), and when this
 * server has room the new run starts here and everyone moves across without changing server.
 */
public final class CatacombsRequeue {
    public static final String NOT_CREATOR = "<c>Only the instance creator can re-queue!";
    public static final String NOT_IN_INSTANCE = "<c>You are not in an instance!";
    public static final String MASTER_UNAVAILABLE = "<c>Master Mode can't be re-queued yet!";

    public enum Decision {REQUEUE, NOT_IN_INSTANCE, NOT_CREATOR}

    private CatacombsRequeue() {
    }

    public static Decision decide(@Nullable UUID creator, UUID player) {
        if (creator == null) return Decision.NOT_IN_INSTANCE;
        return creator.equals(player) ? Decision.REQUEUE : Decision.NOT_CREATOR;
    }

    public static void requeue(SkyBlockPlayer player) {
        CatacombsRun run = CatacombsRuns.of(player.getUuid());
        switch (decide(run == null ? null : run.creator(), player.getUuid())) {
            case NOT_IN_INSTANCE -> player.sendMessage(NOT_IN_INSTANCE);
            case NOT_CREATOR -> player.sendMessage(NOT_CREATOR);
            case REQUEUE -> {
                if (run.floorId().mode() == CatacombsMode.MASTER) {
                    player.sendMessage(MASTER_UNAVAILABLE);
                    return;
                }
                CatacombsQueue.queue(player, run.floorId().floor());
            }
        }
    }

    /** {@link CatacombsQueue}'s local start: a fresh run on this server, everyone moved out of their old one. */
    public static boolean startHere(CatacombsFloor floor, List<UUID> roster, List<SkyBlockPlayer> members) {
        if (CatacombsRuns.remainingSlots() <= 0) return false;
        CatacombsRun next;
        try {
            next = CatacombsRuns.create(new CatacombsFloorId(floor, CatacombsMode.NORMAL), roster);
        } catch (RuntimeException exception) {
            Logger.warn(exception, "Couldn't start a Catacombs run here; queueing through the orchestrator");
            return false;
        }
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            for (SkyBlockPlayer member : members) {
                if (!member.isOnline()) continue;
                CatacombsRun previous = CatacombsRuns.of(member.getUuid());
                if (previous != null && previous != next) previous.depart(member);
                next.admit(member);
            }
        });
        return true;
    }
}
