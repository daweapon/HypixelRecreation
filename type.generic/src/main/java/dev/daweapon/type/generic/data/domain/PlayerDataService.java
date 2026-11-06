package dev.daweapon.type.generic.data.domain;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.data.DataHandler;
import dev.daweapon.type.generic.data.DataWriteQueue;
import dev.daweapon.type.generic.user.HypixelPlayer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public final class PlayerDataService {
    private static final long HANDOFF_SAVE_SUPPRESSION_MILLIS = 60_000;
    private static final int LIFECYCLE_LOCK_COUNT = 64;
    private static final long TRANSFER_APPLY_WAIT_MILLIS = 15_000;

    private static final List<PlayerDataDomain<?>> registered = new CopyOnWriteArrayList<>();
    private static final Map<String, Map<UUID, DataHandler>> caches = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> handoffs = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> discards = new ConcurrentHashMap<>();
    private static final Map<UUID, HypixelPlayer> appliedTo = new ConcurrentHashMap<>();
    private static final ReentrantLock[] lifecycleLocks = createLifecycleLocks();

    private PlayerDataService() {}

    private static ReentrantLock[] createLifecycleLocks() {
        ReentrantLock[] locks = new ReentrantLock[LIFECYCLE_LOCK_COUNT];
        for (int i = 0; i < locks.length; i++) locks[i] = new ReentrantLock();
        return locks;
    }

    private static ReentrantLock lifecycleLock(UUID uuid) {
        int index = Math.floorMod(uuid.hashCode(), LIFECYCLE_LOCK_COUNT);
        return lifecycleLocks[index];
    }

    public static void register(PlayerDataDomain<?> domain) {
        registered.add(domain);
        registered.sort(Comparator.comparingInt(PlayerDataDomain::order));
        caches.computeIfAbsent(domain.key().id(), k -> new ConcurrentHashMap<>());
    }

    private static Map<UUID, DataHandler> cache(DomainKey<?> key) {
        return caches.computeIfAbsent(key.id(), k -> new ConcurrentHashMap<>());
    }

    public static void store(DomainKey<?> key, UUID uuid, DataHandler handler) {
        cache(key).put(uuid, handler);
    }

    public static void evict(DomainKey<?> key, UUID uuid) {
        cache(key).remove(uuid);
    }

    public static <H extends DataHandler> H get(DomainKey<H> key, UUID uuid) {
        DataHandler handler = cache(key).get(uuid);
        if (handler == null) throw new RuntimeException("User " + uuid + " does not exist!");
        return key.type().cast(handler);
    }

    public static <H extends DataHandler> Optional<H> find(DomainKey<H> key, UUID uuid) {
        return Optional.ofNullable(cache(key).get(uuid)).map(key.type()::cast);
    }

    public static boolean isLoaded(DomainKey<?> key, UUID uuid) {
        return cache(key).containsKey(uuid);
    }

    public static <H extends DataHandler> List<H> loaded(DomainKey<H> key) {
        List<H> handlers = new ArrayList<>();
        for (DataHandler handler : cache(key).values()) handlers.add(key.type().cast(handler));
        return handlers;
    }

    public static <H extends DataHandler> H await(DomainKey<H> key, UUID uuid, long timeoutMillis) {
        Map<UUID, DataHandler> cache = cache(key);
        DataHandler handler = cache.get(uuid);
        if (handler != null) return key.type().cast(handler);

        long deadline = System.nanoTime() + timeoutMillis * 1_000_000L;
        long sleepNanos = 1_000_000L;
        while (System.nanoTime() < deadline) {
            LockSupport.parkNanos(sleepNanos);
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                return null;
            }
            handler = cache.get(uuid);
            if (handler != null) return key.type().cast(handler);
            sleepNanos = Math.min(sleepNanos * 2, 16_000_000L);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <H extends DataHandler> PlayerDataDomain<H> domain(DomainKey<H> key) {
        for (PlayerDataDomain<?> candidate : registered) {
            if (candidate.key().id().equals(key.id())) return (PlayerDataDomain<H>) candidate;
        }
        throw new IllegalStateException("No player data domain is registered for " + key.id());
    }

    public static void runLifecycleTransition(UUID uuid, Runnable transition) {
        ReentrantLock lock = lifecycleLock(uuid);
        lock.lock();
        try {
            DataWriteQueue.drain(uuid);
            transition.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Runs the transition only if it can start at once: nobody else holds the player's lifecycle lock and no data
     * write is queued. Callers on the tick thread use this to retry later instead of blocking the server.
     */
    public static boolean tryLifecycleTransition(UUID uuid, Runnable transition) {
        ReentrantLock lock = lifecycleLock(uuid);
        if (!lock.tryLock()) return false;
        try {
            if (!DataWriteQueue.isIdle(uuid)) return false;
            transition.run();
            return true;
        } finally {
            lock.unlock();
        }
    }

    static List<PlayerDataDomain<?>> active(ServerType type) {
        List<PlayerDataDomain<?>> out = new ArrayList<>();
        for (PlayerDataDomain<?> domain : registered) {
            if (domain.appliesTo(type)) out.add(domain);
        }
        return out;
    }

    public static void loadAll(ServerType type, UUID uuid) {
        ReentrantLock lock = lifecycleLock(uuid);
        lock.lock();
        try {
            discards.remove(uuid);
            appliedTo.remove(uuid);
            for (PlayerDataDomain<?> domain : active(type)) domain.load(uuid);
        } finally {
            lock.unlock();
        }
    }

    public static void discardAll(ServerType type, UUID uuid) {
        ReentrantLock lock = lifecycleLock(uuid);
        lock.lock();
        try {
            discards.put(uuid, System.currentTimeMillis());
            appliedTo.remove(uuid);
            DataWriteQueue.remove(uuid);
            for (PlayerDataDomain<?> domain : active(type)) {
                try {
                    domain.unload(uuid);
                } catch (Exception e) {
                    Logger.error(e, "Failed to discard domain {} for user {}", domain.key().id(), uuid);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public static void attachAll(ServerType type, HypixelPlayer player) {
        for (PlayerDataDomain<?> domain : active(type)) domain.attach(player);
    }

    public static void applyAll(ServerType type, HypixelPlayer player) {
        for (PlayerDataDomain<?> domain : active(type)) {
            try {
                domain.applyToPlayer(player);
            } catch (Exception e) {
                Logger.error(e, "Failed to apply data domain {} to player {}, skipping so remaining domains still load",
                        domain.getClass().getSimpleName(), player.getUuid());
            }
        }
        appliedTo.put(player.getUuid(), player);
        if (player.isRemoved()) appliedTo.remove(player.getUuid(), player);
    }

    /**
     * True once the loaded data has been put on this player. Until then their live inventory, armour and experience
     * are still empty, and saving them would overwrite the stored profile with nothing.
     */
    private static boolean isApplied(HypixelPlayer player) {
        return appliedTo.get(player.getUuid()) == player;
    }

    private static boolean awaitApplied(HypixelPlayer player, long timeoutMillis) {
        long deadline = System.nanoTime() + timeoutMillis * 1_000_000L;
        long sleepNanos = 1_000_000L;
        while (!isApplied(player)) {
            if (System.nanoTime() >= deadline || Thread.currentThread().isInterrupted()) return false;
            LockSupport.parkNanos(sleepNanos);
            sleepNanos = Math.min(sleepNanos * 2, 16_000_000L);
        }
        return true;
    }

    public static void saveAndUnloadAll(ServerType type, HypixelPlayer player) {
        UUID uuid = player.getUuid();
        ReentrantLock lock = lifecycleLock(uuid);
        lock.lock();
        try {
            boolean discarded = consumeDiscard(uuid);
            boolean handedOff = consumeHandoff(uuid);
            List<PlayerDataDomain<?>> domains = active(type);

            boolean wasApplied = appliedTo.remove(uuid, player);
            DataWriteQueue.drain(uuid);
            if (!wasApplied && !handedOff && !discarded) {
                Logger.warn("Not saving {} on unload: their data was never applied to them", uuid);
            }
            if (wasApplied && !handedOff && !discarded) {
                for (PlayerDataDomain<?> domain : domains) {
                    try {
                        domain.save(player);
                    } catch (Exception e) {
                        Logger.error(e, "Failed to save domain {} for user {}", domain.key().id(), uuid);
                    }
                }
            }

            for (PlayerDataDomain<?> domain : domains) {
                domain.cleanup(player);
                domain.unload(uuid);
            }
        } finally {
            DataWriteQueue.remove(uuid);
            lock.unlock();
        }
    }

    public static void saveAll(ServerType type, HypixelPlayer player) {
        UUID uuid = player.getUuid();
        if (isSaveSuppressed(uuid)) return;

        ReentrantLock lock = lifecycleLock(uuid);
        lock.lock();
        try {
            if (isSaveSuppressed(uuid) || !isApplied(player)) return;
            for (PlayerDataDomain<?> domain : active(type)) {
                if (!isLoaded(domain.key(), uuid)) continue;
                try {
                    domain.save(player);
                } catch (Exception e) {
                    Logger.error(e, "Failed to save domain {} for user {}", domain.key().id(), uuid);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public static void flushForTransfer(ServerType type, HypixelPlayer player) {
        flushForTransfer(type, player, TRANSFER_APPLY_WAIT_MILLIS);
    }

    static void flushForTransfer(ServerType type, HypixelPlayer player, long applyWaitMillis) {
        UUID uuid = player.getUuid();
        if (!awaitApplied(player, applyWaitMillis)) {
            throw new IllegalStateException("Your data is still loading");
        }
        ReentrantLock lock = lifecycleLock(uuid);
        lock.lock();
        try {
            if (!isApplied(player)) throw new IllegalStateException("Your data is still loading");
            DataWriteQueue.drain(uuid);
            for (PlayerDataDomain<?> domain : active(type)) domain.save(player);
            handoffs.put(uuid, System.currentTimeMillis());
        } finally {
            lock.unlock();
        }
    }

    public static void clearHandoff(UUID uuid) {
        handoffs.remove(uuid);
    }

    public static boolean isSaveSuppressed(UUID uuid) {
        return isStamped(handoffs.get(uuid)) || isStamped(discards.get(uuid));
    }

    private static boolean consumeHandoff(UUID uuid) {
        return isStamped(handoffs.remove(uuid));
    }

    private static boolean consumeDiscard(UUID uuid) {
        return isStamped(discards.remove(uuid));
    }

    private static boolean isStamped(Long stampedAt) {
        return stampedAt != null
                && System.currentTimeMillis() - stampedAt <= HANDOFF_SAVE_SUPPRESSION_MILLIS;
    }
}
