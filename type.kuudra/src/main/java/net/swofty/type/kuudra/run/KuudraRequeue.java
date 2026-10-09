package net.swofty.type.kuudra.run;

import net.minestom.server.MinecraftServer;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.queue.KuudraQueue;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;
import java.util.UUID;

/**
 * Re-queueing the same tier from inside a run with {@code /instancerequeue}, offered to the instance creator when the run
 * ends. Only the creator may (the captures: "Only the instance creator can re-queue!"). The party goes through the normal
 * queue; when this server has room the new run is made here and everyone moves across without a server switch.
 */
public final class KuudraRequeue {
    public static final String COMMAND = "/instancerequeue";
    public static final String NOT_CREATOR = "<c>Only the instance creator can re-queue!";
    public static final String NOT_IN_INSTANCE = "<c>You are not in an instance!";

    public enum Decision {
        REQUEUE, NOT_IN_INSTANCE, NOT_CREATOR
    }

    private KuudraRequeue() {
    }

    public static Decision decide(@Nullable UUID creator, UUID player) {
        if (creator == null) return Decision.NOT_IN_INSTANCE;
        return creator.equals(player) ? Decision.REQUEUE : Decision.NOT_CREATOR;
    }

    /** The clickable line the creator gets at the end of a run. */
    public static String prompt(KuudraTier tier) {
        return "<hover:'<e>Click to re-queue!'><click:run:'" + COMMAND + "'><e>Click here to re-queue for <c>Kuudra's Hollow <8>- <e>"
                + tier.getDisplayName() + " Tier<e>!</click></hover>";
    }

    public static void requeue(SkyBlockPlayer player) {
        KuudraRun run = KuudraRuns.of(player.getUuid());
        switch (decide(run == null ? null : run.creator(), player.getUuid())) {
            case NOT_IN_INSTANCE -> player.sendMessage(NOT_IN_INSTANCE);
            case NOT_CREATOR -> player.sendMessage(NOT_CREATOR);
            case REQUEUE -> KuudraQueue.queue(player, run.tier());
        }
    }

    /** {@link KuudraQueue}'s local start on the Kuudra server: a fresh run here, everyone moved out of their old one. */
    public static boolean startHere(KuudraTier tier, List<UUID> roster, List<SkyBlockPlayer> members) {
        if (KuudraRuns.remainingSlots() <= 0) return false;
        KuudraRun next;
        try {
            next = KuudraRuns.create(tier, roster);
        } catch (RuntimeException exception) {
            Logger.warn(exception, "Couldn't start a Kuudra run here; queueing through the orchestrator");
            return false;
        }
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            for (SkyBlockPlayer member : members) {
                if (!member.isOnline()) continue;
                KuudraRun previous = KuudraRuns.of(member.getUuid());
                if (previous != null && previous != next) previous.depart(member);
                next.admit(member);
            }
        });
        return true;
    }
}
