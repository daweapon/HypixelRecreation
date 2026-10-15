package dev.daweapon.commons.protocol.objects.orchestrator;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.List;

public class ListGamesProtocol extends RedisProtocol
        <ListGamesProtocol.ListGamesMessage, ListGamesProtocol.ListGamesResponse> {
    private static final Serializer<ListGamesMessage> SERIALIZER =
            new JacksonSerializer<>(ListGamesMessage.class);
    private static final Serializer<ListGamesResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(ListGamesResponse.class);

    @Override
    public Serializer<ListGamesMessage> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<ListGamesResponse> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record ListGamesMessage(ServerType type) { }

    public record GameSummary(String gameId, String gameTypeName, String map,
                              int playerCount, boolean acceptingJoins) { }

    public record ServerGames(String shortName, String serverUuid, int onlinePlayers, int maxPlayers,
                              Integer remainingGameSlots, List<GameSummary> games) { }

    public record ListGamesResponse(List<ServerGames> servers, boolean success, String error) { }
}
