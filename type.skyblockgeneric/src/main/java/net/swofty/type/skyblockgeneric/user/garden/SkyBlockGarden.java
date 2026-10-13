package net.swofty.type.skyblockgeneric.user.garden;

import lombok.Getter;
import net.hollowcube.polar.PolarLoader;
import net.hollowcube.polar.PolarWorld;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.world.DimensionType;
import net.swofty.type.skyblockgeneric.data.monogdb.GardenDatabase;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class SkyBlockGarden {
    private static final long ARRIVAL_GRACE_MILLIS = 10_000;
    private static final Map<UUID, SkyBlockGarden> loadedGardens = new ConcurrentHashMap<>();
    private static final AtomicBoolean shutdownHookRegistered = new AtomicBoolean();
    @Getter
    private static volatile GardenPresence presence = GardenPresence.NONE;

    private final UUID gardenId;
    private final GardenDatabase database;
    private CompletableFuture<SharedInstance> loading;
    private SharedInstance gardenInstance;
    private PolarWorld world;
    private int version = GardenWorldStorage.CURRENT_VERSION;
    private long lastSaved;
    private long lastRequested;

    private SkyBlockGarden(UUID gardenId) {
        this.gardenId = gardenId;
        this.database = new GardenDatabase(gardenId.toString());
    }

    public static boolean hasGarden(UUID gardenId) {
        return loadedGardens.containsKey(gardenId);
    }

    public static SkyBlockGarden getOrCreate(UUID gardenId) {
        return loadedGardens.computeIfAbsent(gardenId, SkyBlockGarden::new);
    }

    public static SkyBlockGarden of(SkyBlockPlayer player) {
        return getOrCreate(player.getSkyBlockIsland().getIslandID());
    }

    public static void setPresence(GardenPresence presence) {
        SkyBlockGarden.presence = presence;
    }

    public static Collection<SkyBlockGarden> loaded() {
        return loadedGardens.values();
    }

    public synchronized CompletableFuture<SharedInstance> getSharedInstance() {
        lastRequested = System.currentTimeMillis();
        loadedGardens.putIfAbsent(gardenId, this);
        if (loading != null) return loading;

        CompletableFuture<SharedInstance> future = new CompletableFuture<>();
        loading = future;
        Thread.startVirtualThread(() -> load(future));
        return future;
    }

    private void load(CompletableFuture<SharedInstance> future) {
        try {
            Logger.info("[{}] Starting garden instance load", gardenId);
            GardenWorldStorage.LoadedGardenWorld loaded = GardenWorldStorage.load(database);
            InstanceContainer container = MinecraftServer.getInstanceManager().createInstanceContainer(
                    dimensionType());
            container.setChunkLoader(new PolarLoader(loaded.world()));
            SharedInstance shared = MinecraftServer.getInstanceManager().createSharedInstance(container);

            synchronized (this) {
                world = loaded.world();
                version = loaded.version();
                lastSaved = loaded.lastSaved();
                gardenInstance = shared;
            }
            future.complete(shared);
            Logger.info("[{}] Completed garden instance load", gardenId);
        } catch (Throwable throwable) {
            Logger.error(throwable, "[{}] Failed garden instance load", gardenId);
            synchronized (this) {
                loading = null;
            }
            future.completeExceptionally(throwable);
        }
    }

    public synchronized void runVacantCheck() {
        if (gardenInstance == null || loading == null) {
            if (loading == null) loadedGardens.remove(gardenId, this);
            return;
        }
        if (!gardenInstance.getPlayers().isEmpty()) return;
        if (System.currentTimeMillis() - lastRequested < ARRIVAL_GRACE_MILLIS) return;

        save();
        SharedInstance shared = gardenInstance;
        InstanceContainer container = shared.getInstanceContainer();
        shared.getChunks().forEach(shared::unloadChunk);
        MinecraftServer.getInstanceManager().unregisterInstance(shared);
        MinecraftServer.getInstanceManager().unregisterInstance(container);
        gardenInstance = null;
        world = null;
        loading = null;
        loadedGardens.remove(gardenId, this);
    }

    private synchronized void flush() {
        if (gardenInstance == null || world == null) return;
        save();
    }

    private void save() {
        new PolarLoader(world).saveInstance(gardenInstance);
        GardenWorldStorage.save(database, world, version);
        lastSaved = System.currentTimeMillis();
    }

    private static RegistryKey<DimensionType> dimensionType() {
        return MinecraftServer.getDimensionTypeRegistry().getKey(Key.key("skyblock:island"));
    }

    public static void runVacantLoop(Scheduler scheduler) {
        registerShutdownHook();
        scheduler.submitTask(() -> {
            loadedGardens.values().forEach(SkyBlockGarden::runVacantCheck);
            return TaskSchedule.tick(4);
        }, ExecutionType.TICK_END);
    }

    private static void registerShutdownHook() {
        if (!shutdownHookRegistered.compareAndSet(false, true)) return;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.info("Saving {} loaded SkyBlock garden(s) before shutdown", loadedGardens.size());
            loadedGardens.values().forEach(garden -> {
                try {
                    garden.flush();
                } catch (Throwable throwable) {
                    Logger.error(throwable, "[{}] Failed to save garden during shutdown", garden.gardenId);
                }
            });
        }, "skyblock-garden-shutdown-save"));
    }
}
