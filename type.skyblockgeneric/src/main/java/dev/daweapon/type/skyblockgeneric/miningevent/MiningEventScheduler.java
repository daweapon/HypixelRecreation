package dev.daweapon.type.skyblockgeneric.miningevent;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MiningEventScheduler {
    private static MiningEventZone zone;
    private static long nextCycleAt;
    private static @Nullable MiningEventType pending;
    private static long pendingStartsAt;

    private MiningEventScheduler() {
    }

    public static synchronized void start(MiningEventZone eventZone) {
        if (zone != null) return;
        zone = eventZone;
        MiningEventConfig config = MiningEventConfig.get();
        nextCycleAt = System.currentTimeMillis() + (long) (config.number(3, "first_event_minutes") * 60_000);
        MinecraftServer.getSchedulerManager().buildTask(MiningEventScheduler::tick)
                .repeat(TaskSchedule.seconds(1)).schedule();
    }

    public static @Nullable MiningEventZone zone() {
        return zone;
    }

    public static synchronized long millisUntilNextCycle() {
        return Math.max(0, nextCycleAt - System.currentTimeMillis());
    }

    public static synchronized @Nullable MiningEventType pending() {
        return pending;
    }

    public static synchronized long millisUntilPendingStarts() {
        return pending == null ? 0 : Math.max(0, pendingStartsAt - System.currentTimeMillis());
    }

    public static synchronized boolean force(MiningEventType type, boolean ignoreRequirements) {
        if (zone == null || !type.runsIn(zone)) return false;
        pending = null;
        begin(type, ignoreRequirements);
        return true;
    }

    public static synchronized void stopCurrent() {
        MiningEvent event = MiningEvents.current().orElse(null);
        if (event == null) return;
        MiningEvents.setCurrent(null);
        event.onEnd(false);
    }

    private static synchronized void tick() {
        try {
            long now = System.currentTimeMillis();
            MiningEvent current = MiningEvents.current().orElse(null);
            if (current != null) {
                current.tick(now);
                if (current.isOver(now)) {
                    MiningEvents.setCurrent(null);
                    current.onEnd(false);
                }
            }
            if (pending != null && now >= pendingStartsAt) {
                MiningEventType type = pending;
                pending = null;
                begin(type, false);
            } else if (pending == null && now >= nextCycleAt) {
                scheduleNext(now);
            }
        } catch (Exception exception) {
            Logger.error(exception, "Mining event tick failed");
        }
    }

    private static void scheduleNext(long now) {
        MiningEventConfig config = MiningEventConfig.get();
        nextCycleAt += (long) (config.number(20, "cadence_minutes") * 60_000);
        if (nextCycleAt <= now) nextCycleAt = now + (long) (config.number(20, "cadence_minutes") * 60_000);
        MiningEventType type = pickType();
        if (type == null) return;
        int warning = config.integer(20, "warning_seconds");
        pending = type;
        pendingStartsAt = now + warning * 1000L;
        MiningEventMessages.warning(type, zone, warning, true);
    }

    private static @Nullable MiningEventType pickType() {
        MiningEventConfig config = MiningEventConfig.get();
        List<MiningEventType> pool = new ArrayList<>();
        for (MiningEventType type : MiningEventType.values()) {
            if (type.runsIn(zone) && config.weight(type) > 0) pool.add(type);
        }
        if (pool.isEmpty()) return null;
        int total = pool.stream().mapToInt(config::weight).sum();
        int roll = ThreadLocalRandom.current().nextInt(total);
        for (MiningEventType type : pool) {
            roll -= config.weight(type);
            if (roll < 0) return type;
        }
        return pool.getLast();
    }

    private static void begin(MiningEventType type, boolean ignoreRequirements) {
        MiningEvent previous = MiningEvents.current().orElse(null);
        if (previous != null) {
            MiningEvents.setCurrent(null);
            previous.onEnd(false);
        }
        MiningEvent event = create(type);
        if (!ignoreRequirements && !event.canStart()) {
            event.onCancelled();
            return;
        }
        MiningEvents.setCurrent(event);
        event.begin(System.currentTimeMillis(), MiningEventConfig.get().durationMillis(type));
        MiningEvents.players().forEach(player -> dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetService.fire(player, dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetTrigger.EVENT_START, type.name()));
    }

    private static MiningEvent create(MiningEventType type) {
        return switch (type) {
            case DOUBLE_POWDER -> new DoublePowderEvent(zone);
            case BETTER_TOGETHER -> new BetterTogetherEvent(zone);
            case MITHRIL_GOURMAND -> new MithrilGourmandEvent(zone);
            case GOBLIN_RAID -> new GoblinRaidEvent(zone);
            case RAFFLE -> new RaffleEvent(zone);
        };
    }
}
