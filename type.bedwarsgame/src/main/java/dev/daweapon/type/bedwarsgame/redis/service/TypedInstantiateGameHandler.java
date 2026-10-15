package dev.daweapon.type.bedwarsgame.redis.service;

import dev.daweapon.commons.bedwars.BedWarsGameType;
import dev.daweapon.commons.bedwars.map.BedWarsMapsConfig;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.game.InstantiateGamePushProtocol;
import dev.daweapon.commons.protocol.objects.game.InstantiateGamePushProtocol.Request;
import dev.daweapon.commons.protocol.objects.game.InstantiateGamePushProtocol.Response;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.type.bedwarsgame.TypeBedWarsGameLoader;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// Redis handler that creates a new Bedwars game instance on request from the proxy/orchestrator.
public class TypedInstantiateGameHandler implements RedisMessageHandler<Request, Response> {

    private static final InstantiateGamePushProtocol PROTOCOL = new InstantiateGamePushProtocol();

    @Override
    public RedisProtocol<Request, Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public Response handle(Request request, RedisMessageContext context) {
        try {
            BedWarsGameType gameType = BedWarsGameType.valueOf(request.gameType().toUpperCase());

            boolean hasRequestedMap = request.map() != null && !request.map().isEmpty();

            BedWarsMapsConfig.MapEntry mapEntry = null;
            if (TypeBedWarsGameLoader.getMapsConfig() != null) {
                List<BedWarsMapsConfig.MapEntry> maps = TypeBedWarsGameLoader.getMapsConfig().getMaps();
                if (hasRequestedMap) {
                    for (BedWarsMapsConfig.MapEntry entry : maps) {
                        if (entry.getId().equals(request.map()) || entry.getName().equals(request.map())) {
                            if (entry.getConfiguration() != null &&
                                    entry.getConfiguration().getTypes() != null &&
                                !entry.getConfiguration().getTypes().contains(gameType.getMapCompatibilityType())) {
                                return Response.failure("Map does not support game type: " + gameType);
                            }
                            mapEntry = entry;
                            break;
                        }
                    }
                } else {
                    // No specific map requested (queueing a mode): pick a random map that supports it.
                    List<BedWarsMapsConfig.MapEntry> compatible = maps.stream()
                            .filter(e -> e.getConfiguration() != null
                                    && e.getConfiguration().getTypes() != null
                                    && e.getConfiguration().getTypes().contains(gameType.getMapCompatibilityType()))
                            .toList();
                    if (!compatible.isEmpty()) {
                        mapEntry = compatible.get(ThreadLocalRandom.current().nextInt(compatible.size()));
                    }
                }
            }

            if (mapEntry == null) {
                return Response.failure(hasRequestedMap
                        ? "Map not found: " + request.map()
                        : "No compatible maps available for " + gameType);
            }

            BedWarsGame game = TypeBedWarsGameLoader.createGame(mapEntry, gameType);
            if (game == null) {
                return Response.failure("Server at capacity, cannot create new game");
            }

            return Response.success(game.getGameId(), mapEntry.getName(), gameType.toString());
        } catch (Exception e) {
            return Response.failure("Failed to instantiate game: " + e.getMessage());
        }
    }
}
