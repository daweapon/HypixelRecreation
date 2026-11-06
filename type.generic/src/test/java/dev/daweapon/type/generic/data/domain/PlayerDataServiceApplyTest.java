package dev.daweapon.type.generic.data.domain;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.data.DataHandler;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A server bounce (Catacombs to Hub and straight back by the run-rejoin) once saved the Hub player's inventory while
 * their loaded data was still being put on them: the save captured the still-empty inventory and wrote it over the
 * profile. These pin down that nothing captures a player's live state before their data has been applied.
 */
class PlayerDataServiceApplyTest {
    private static final ServerType TYPE = ServerType.SKYBLOCK_CATACOMBS;
    private static final List<String> INVENTORY = List.of("HYPERION", "ASPECT_OF_THE_DRAGONS", "ENDER_PEARL");
    private static final InventoryDomain DOMAIN = new InventoryDomain();

    @BeforeAll
    static void initializeServer() {
        MinecraftServer.init();
        PlayerDataService.register(DOMAIN);
    }

    @Test
    void transferBeforeApplyWaitsForTheInventoryInsteadOfSavingItEmpty() throws Exception {
        HypixelPlayer player = player();
        UUID uuid = player.getUuid();
        DOMAIN.stored.put(uuid, INVENTORY);
        PlayerDataService.loadAll(TYPE, uuid);

        CountDownLatch applying = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        DOMAIN.beforeApply.put(uuid, () -> {
            applying.countDown();
            await(release);
        });
        CompletableFuture<Void> apply = CompletableFuture.runAsync(() -> PlayerDataService.applyAll(TYPE, player));
        assertTrue(applying.await(5, TimeUnit.SECONDS));

        CompletableFuture<Void> transfer = CompletableFuture.runAsync(() -> PlayerDataService.flushForTransfer(TYPE, player));
        Thread.sleep(200);
        assertFalse(transfer.isDone());
        assertEquals(INVENTORY, DOMAIN.stored.get(uuid));

        release.countDown();
        apply.get(5, TimeUnit.SECONDS);
        transfer.get(5, TimeUnit.SECONDS);
        assertEquals(INVENTORY, DOMAIN.stored.get(uuid));
        assertTrue(PlayerDataService.isSaveSuppressed(uuid));
        PlayerDataService.saveAndUnloadAll(TYPE, player);
    }

    @Test
    void transferThatNeverSeesTheDataAppliedFailsWithoutWritingAnything() {
        HypixelPlayer player = player();
        UUID uuid = player.getUuid();
        DOMAIN.stored.put(uuid, INVENTORY);
        PlayerDataService.loadAll(TYPE, uuid);

        assertThrows(IllegalStateException.class, () -> PlayerDataService.flushForTransfer(TYPE, player, 50));
        assertEquals(INVENTORY, DOMAIN.stored.get(uuid));
        assertFalse(PlayerDataService.isSaveSuppressed(uuid));
        PlayerDataService.saveAndUnloadAll(TYPE, player);
    }

    @Test
    void disconnectBeforeApplyUnloadsWithoutOverwritingTheProfile() {
        HypixelPlayer player = player();
        UUID uuid = player.getUuid();
        DOMAIN.stored.put(uuid, INVENTORY);
        PlayerDataService.loadAll(TYPE, uuid);

        PlayerDataService.saveAll(TYPE, player);
        PlayerDataService.saveAndUnloadAll(TYPE, player);

        assertEquals(INVENTORY, DOMAIN.stored.get(uuid));
        assertFalse(PlayerDataService.isLoaded(DOMAIN.key(), uuid));
    }

    @Test
    void appliedPlayersStillSaveTheirLatestState() {
        HypixelPlayer player = player();
        UUID uuid = player.getUuid();
        DOMAIN.stored.put(uuid, INVENTORY);
        PlayerDataService.loadAll(TYPE, uuid);
        PlayerDataService.applyAll(TYPE, player);

        DOMAIN.live.put(uuid, List.of("HYPERION"));
        PlayerDataService.saveAll(TYPE, player);
        assertEquals(List.of("HYPERION"), DOMAIN.stored.get(uuid));

        DOMAIN.live.put(uuid, List.of());
        PlayerDataService.saveAndUnloadAll(TYPE, player);
        assertEquals(List.of(), DOMAIN.stored.get(uuid));
    }

    @Test
    void anEarlierSessionsApplyDoesNotCountForTheNextSession() {
        HypixelPlayer first = player();
        UUID uuid = first.getUuid();
        DOMAIN.stored.put(uuid, INVENTORY);
        PlayerDataService.loadAll(TYPE, uuid);
        PlayerDataService.applyAll(TYPE, first);
        PlayerDataService.saveAndUnloadAll(TYPE, first);

        HypixelPlayer second = player(uuid);
        PlayerDataService.loadAll(TYPE, uuid);
        PlayerDataService.applyAll(TYPE, first);
        DOMAIN.live.remove(uuid);

        PlayerDataService.saveAndUnloadAll(TYPE, second);
        assertEquals(INVENTORY, DOMAIN.stored.get(uuid));
    }

    private static HypixelPlayer player() {
        return player(UUID.randomUUID());
    }

    private static HypixelPlayer player(UUID uuid) {
        return new HypixelPlayer(new SilentConnection(), new GameProfile(uuid, "tester"));
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class InventoryDomain implements PlayerDataDomain<Handler> {
        private final DomainKey<Handler> key = new DomainKey<>("test-apply-inventory", Handler.class);
        private final Map<UUID, List<String>> stored = new ConcurrentHashMap<>();
        private final Map<UUID, List<String>> live = new ConcurrentHashMap<>();
        private final Map<UUID, Runnable> beforeApply = new ConcurrentHashMap<>();

        @Override
        public DomainKey<Handler> key() {
            return key;
        }

        @Override
        public boolean appliesTo(ServerType type) {
            return type == TYPE;
        }

        @Override
        public void load(UUID uuid) {
            PlayerDataService.store(key, uuid, new Handler(uuid));
        }

        @Override
        public void applyToPlayer(HypixelPlayer player) {
            Runnable hook = beforeApply.remove(player.getUuid());
            if (hook != null) hook.run();
            live.put(player.getUuid(), stored.getOrDefault(player.getUuid(), List.of()));
        }

        @Override
        public void save(HypixelPlayer player) {
            if (!PlayerDataService.isLoaded(key, player.getUuid())) return;
            stored.put(player.getUuid(), live.getOrDefault(player.getUuid(), List.of()));
        }

        @Override
        public void unload(UUID uuid) {
            PlayerDataService.evict(key, uuid);
        }
    }

    private static final class Handler extends DataHandler {
        private Handler(UUID uuid) {
            super(uuid);
        }

        @Override
        public DataHandler fromDocument(Document document) {
            return this;
        }

        @Override
        public Document toDocument() {
            return new Document();
        }

        @Override
        public void runOnLoad(HypixelPlayer player) {
        }

        @Override
        public void runOnSave(HypixelPlayer player) {
        }
    }

    private static final class SilentConnection extends PlayerConnection {
        @Override
        public void sendPacket(SendablePacket packet) {
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return new InetSocketAddress(0);
        }
    }
}
