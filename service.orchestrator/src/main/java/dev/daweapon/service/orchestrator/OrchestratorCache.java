package dev.daweapon.service.orchestrator;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.protocol.objects.orchestrator.GameHeartbeatProtocol;
import dev.daweapon.type.game.game.GameObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

// In-memory matchmaking state: tracks game servers (via heartbeats) and active games, and implements the logic
// for finding or instantiating a joinable game/server for a requested game type, map, and mode.
public class OrchestratorCache {
    private static final Map<String, GameServerState> serversByShortName = new ConcurrentHashMap<>();
    private static final Map<UUID, GameWithServer> gamesByGameId = new ConcurrentHashMap<>();
    private static final Map<UUID, List<SlotReservation>> reservationsByGameId = new ConcurrentHashMap<>();
    private static final Map<UUID, List<SlotReservation>> playerReservationsByServer = new ConcurrentHashMap<>();
    private static final long HEARTBEAT_TTL_MS = 10000; // 10s
    private static final long RESERVATION_TTL_MS = 8000; // 8s

    public static void handleHeartbeat(UUID uuid,
                                       String shortName,
                                       ServerType type,
                                       int maxPlayers,
                                       int onlinePlayers,
                                       List<GameObject> games) {
        handleHeartbeat(uuid, shortName, type, maxPlayers, onlinePlayers, games, List.of(), null);
    }

    public static void handleHeartbeat(UUID uuid,
                                       String shortName,
                                       ServerType type,
                                       int maxPlayers,
                                       int onlinePlayers,
                                       List<GameObject> games,
                                       List<GameHeartbeatProtocol.MapAdvertisement> mapAdvertisements,
                                       Integer remainingGameSlots) {
        GameServerState state = new GameServerState(
            uuid,
            shortName,
            type,
            maxPlayers,
            onlinePlayers,
            Instant.now().toEpochMilli(),
            mapAdvertisements != null ? List.copyOf(mapAdvertisements) : List.of(),
            remainingGameSlots
        );
        serversByShortName.put(shortName, state);

        // Remove old games from this server
        gamesByGameId.entrySet().removeIf(entry -> entry.getValue().serverUuid().equals(uuid));

        // Add current games with server tracking
        for (GameObject game : games) {
            gamesByGameId.put(game.getGameId(), new GameWithServer(game, uuid, shortName));
        }
    }

    /**
     * Generic version - finds an existing joinable game for any server type
     */
    public static GameWithServer findExisting(ServerType serverType, int maxPlayers, String map) {
        return findExisting(serverType, maxPlayers, map, 1);
    }

    /**
     * Finds an existing joinable game with at least neededSlots available slots
     */
    public static GameWithServer findExisting(ServerType serverType, int maxPlayers, String map, int neededSlots) {
        return findExisting(serverType, maxPlayers, map, neededSlots, null);
    }

    /**
     * Finds an existing joinable game with at least neededSlots available slots, filtered by game type name
     */
    public static GameWithServer findExisting(ServerType serverType, int maxPlayers, String map, int neededSlots, String gameTypeName) {
        return findExisting(serverType, maxPlayers, map, neededSlots, gameTypeName, false);
    }

    /**
     * Finds an existing joinable game with at least neededSlots available slots.
     * If requirePlayers is true, only considers games that already have players.
     */
    public static synchronized GameWithServer findExisting(ServerType serverType,
                                                           int maxPlayers,
                                                           String map,
                                                           int neededSlots,
                                                           String gameTypeName,
                                                           boolean requirePlayers) {
        cleanup();

        List<GameWithServer> candidates = new ArrayList<>();
        for (GameWithServer gameWithServer : gamesByGameId.values()) {
            GameObject game = gameWithServer.game();
            int reservedSlots = getReservedSlots(game.getGameId());
            int availableSlots = maxPlayers - game.getInvolvedPlayers().size() - reservedSlots;
            if (game.getType() == serverType &&
                game.isAcceptingJoins() &&
                availableSlots >= neededSlots &&
                (map == null || game.getMap().equals(map)) &&
                (gameTypeName == null || gameTypeName.equals(game.getGameTypeName())) &&
                (!requirePlayers || !game.getInvolvedPlayers().isEmpty())) {
                candidates.add(gameWithServer);
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        // Prefer games with more players (closer to starting)
        candidates.sort(Comparator.comparingInt((GameWithServer g) -> g.game().getInvolvedPlayers().size()).reversed());
        GameWithServer selected = candidates.getFirst();
        if (neededSlots > 0) {
            reserveSlots(selected.game().getGameId(), neededSlots);
        }
        return selected;
    }
    /**
     * Generic version - finds a server with capacity to host a new game for any server type
     */
    public static GameServerState instantiateServer(ServerType serverType, int maxPlayers) {
        cleanup();

        List<GameServerState> candidates = new ArrayList<>();
        for (GameServerState server : serversByShortName.values()) {
            if (server.type() == serverType &&
                server.availableSlots() >= maxPlayers) {
                candidates.add(server);
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        // Prefer servers with more available capacity
        candidates.sort(Comparator.comparingInt(GameServerState::availableSlots).reversed());
        int topAvail = candidates.getFirst().availableSlots();

        List<GameServerState> topServers = new ArrayList<>();
        for (GameServerState server : candidates) {
            if (server.availableSlots() == topAvail) {
                topServers.add(server);
            } else {
                break;
            }
        }

        return topServers.get(ThreadLocalRandom.current().nextInt(topServers.size()));
    }

    /**
     * Picks the server to host a new game: prefer servers advertising free game
     * slots (most free first); when every server is full, fall back to the least
     * filled one so the game still lands somewhere.
     */
    public static GameServerState pickServerForNewGame(ServerType serverType) {
        cleanup();
        List<GameServerState> all = new ArrayList<>();
        for (GameServerState server : serversByShortName.values()) {
            if (server.type() == serverType) all.add(server);
        }
        if (all.isEmpty()) return null;

        List<GameServerState> free = all.stream()
                .filter(server -> server.remainingGameSlots() == null || server.remainingGameSlots() > 0)
                .toList();
        if (!free.isEmpty()) {
            return free.stream()
                    .max(Comparator.comparingInt(server ->
                            server.remainingGameSlots() == null ? Integer.MAX_VALUE : server.remainingGameSlots()))
                    .orElse(null);
        }
        return all.stream()
                .min(Comparator.comparingLong(server -> gamesByGameId.values().stream()
                        .filter(game -> game.serverUuid().equals(server.uuid())).count()))
                .orElse(null);
    }

    public static Set<String> getMaps(ServerType type, String gameTypeName) {
        cleanup();

        Set<String> maps = new HashSet<>();

        for (GameWithServer gameWithServer : gamesByGameId.values()) {
            GameObject game = gameWithServer.game();
            if (game.getType() == type && (gameTypeName == null || gameTypeName.equals(game.getGameTypeName()))) {
                maps.add(game.getMap());
            }
        }

        return maps;
    }

    /**
     * Find a game that a player is part of (either active or disconnected).
     * Used for the rejoin system.
     */
    public static GameWithServer findPlayerGame(UUID playerUuid) {
        cleanup();

        for (GameWithServer gameWithServer : gamesByGameId.values()) {
            GameObject game = gameWithServer.game();
            // Check active players
            if (game.getInvolvedPlayers().contains(playerUuid)) {
                return gameWithServer;
            }
            // Check disconnected players
            if (game.getDisconnectedPlayers() != null && game.getDisconnectedPlayers().contains(playerUuid)) {
                return gameWithServer;
            }
        }
        return null;
    }

    public static void cleanup() {
        long now = Instant.now().toEpochMilli();
        cleanupReservations(now);

        // Remove stale servers
        serversByShortName.values().removeIf(server ->
            now - server.lastHeartbeat() > HEARTBEAT_TTL_MS);

        // Remove games from servers that are no longer available
        Set<UUID> availableServerUuids = new HashSet<>();
        for (GameServerState server : serversByShortName.values()) {
            availableServerUuids.add(server.uuid());
        }

        gamesByGameId.values().removeIf(gameWithServer ->
            !availableServerUuids.contains(gameWithServer.serverUuid()));

        reservationsByGameId.entrySet().removeIf(entry -> !gamesByGameId.containsKey(entry.getKey()));
    }

    private static void reserveSlots(UUID gameId, int slots) {
        if (slots <= 0) {
            return;
        }

        long expiresAt = Instant.now().toEpochMilli() + RESERVATION_TTL_MS;
        reservationsByGameId
            .computeIfAbsent(gameId, ignored -> new CopyOnWriteArrayList<>())
            .add(new SlotReservation(slots, expiresAt));
    }

    /** Holds room on a server for players who were just sent to it but are not in its heartbeat yet. */
    public static void reservePlayerSlots(UUID serverUuid, int slots) {
        if (slots <= 0) {
            return;
        }
        playerReservationsByServer
            .computeIfAbsent(serverUuid, ignored -> new CopyOnWriteArrayList<>())
            .add(new SlotReservation(slots, Instant.now().toEpochMilli() + RESERVATION_TTL_MS));
    }

    public static int getReservedPlayerSlots(UUID serverUuid) {
        return sumActive(playerReservationsByServer.get(serverUuid));
    }

    private static int getReservedSlots(UUID gameId) {
        return sumActive(reservationsByGameId.get(gameId));
    }

    private static int sumActive(List<SlotReservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return 0;
        }

        long now = Instant.now().toEpochMilli();
        int total = 0;
        for (SlotReservation reservation : reservations) {
            if (reservation.expiresAtMs() > now) {
                total += reservation.slots();
            }
        }
        return total;
    }

    private static void cleanupReservations(long now) {
        for (Map<UUID, List<SlotReservation>> reservationsByKey : List.of(reservationsByGameId, playerReservationsByServer)) {
            reservationsByKey.entrySet().removeIf(entry -> {
                List<SlotReservation> reservations = entry.getValue();
                reservations.removeIf(reservation -> reservation.expiresAtMs() <= now);
                return reservations.isEmpty();
            });
        }
    }

    private record SlotReservation(int slots, long expiresAtMs) {
    }

    public record GameServerState(UUID uuid,
                                  String shortName,
                                  ServerType type,
                                  int maxPlayers,
                                  int onlinePlayers,
                                  long lastHeartbeat,
                                  List<GameHeartbeatProtocol.MapAdvertisement> mapAdvertisements,
                                  Integer remainingGameSlots) {

        public GameServerState {
            mapAdvertisements = mapAdvertisements != null ? List.copyOf(mapAdvertisements) : List.of();
        }

        public int availableSlots() {
            return Math.max(0, maxPlayers - onlinePlayers);
        }
    }

    public record GameWithServer(GameObject game, UUID serverUuid, String serverShortName) {
    }

    // Getter methods for debugging/monitoring
    public static Collection<GameServerState> getAllServers() {
        cleanup();
        return new ArrayList<>(serversByShortName.values());
    }

    public static Collection<GameWithServer> getAllActiveGames() {
        cleanup();
        return new ArrayList<>(gamesByGameId.values());
    }

    public static GameServerState getServerByUuid(UUID serverUuid) {
        cleanup();
        return serversByShortName.values().stream()
            .filter(server -> server.uuid().equals(serverUuid))
            .findFirst()
            .orElse(null);
    }

    /**
     * Get player and game counts filtered by server type and optionally by game type name.
     *
     * @param type         The server type (e.g., SKYBLOCK_CATACOMBS, SKYBLOCK_KUUDRA)
     * @param gameTypeName Optional game type name to filter by (e.g., "CLASSIC", "SOLO"). Pass null for all.
     * @return GameCountStats with player count and game count
     */
    public static GameCountStats getGameCounts(ServerType type, String gameTypeName) {
        return getGameCounts(type, gameTypeName, null);
    }

    /**
     * Get player and game counts filtered by server type, game type name, and map name.
     *
     * @param type         The server type (e.g., SKYBLOCK_CATACOMBS, SKYBLOCK_KUUDRA)
     * @param gameTypeName Optional game type name to filter by (e.g., "CLASSIC", "SOLO"). Pass null for all.
     * @param mapName      Optional map name to filter by. Pass null for all maps.
     * @return GameCountStats with player count and game count
     */
    public static GameCountStats getGameCounts(ServerType type, String gameTypeName, String mapName) {
        cleanup();
        int playerCount = 0;
        int gameCount = 0;

        for (GameWithServer gameWithServer : gamesByGameId.values()) {
            GameObject game = gameWithServer.game();
            if (game.getType() == type) {
                if (gameTypeName == null || gameTypeName.equals(game.getGameTypeName())) {
                    if (mapName == null || mapName.equals(game.getMap())) {
                        playerCount += game.getInvolvedPlayers().size();
                        gameCount++;
                    }
                }
            }
        }

        return new GameCountStats(playerCount, gameCount);
    }

    public record GameCountStats(int playerCount, int gameCount) {
    }

}
