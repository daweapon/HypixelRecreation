package dev.daweapon.commons.protocol.objects.orchestrator;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class OrchestratorQueueProtocol extends RedisProtocol
        <OrchestratorQueueProtocol.QueueRequest, OrchestratorQueueProtocol.QueueResponse> {
    private static final Serializer<QueueRequest> SERIALIZER = new JacksonSerializer<>(QueueRequest.class);
    private static final Serializer<QueueResponse> RETURN_SERIALIZER = new JacksonSerializer<>(QueueResponse.class);

    @Override
    public Serializer<QueueRequest> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<QueueResponse> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record QueueRequest() {}

    public record WaitingTicket(ServerType type, int position, int length, @Nullable String mode,
                                @Nullable String originServer, List<UUID> players, long waitedMillis) {
        public WaitingTicket {
            players = players == null ? List.of() : List.copyOf(players);
        }
    }

    public record QueueResponse(List<WaitingTicket> waiting) {
        public QueueResponse {
            waiting = waiting == null ? List.of() : List.copyOf(waiting);
        }
    }
}
