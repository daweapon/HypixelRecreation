package dev.daweapon.commons.protocol.objects.orchestrator;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.List;

public class OrchestratorStatusProtocol extends RedisProtocol
        <OrchestratorStatusProtocol.StatusRequest, OrchestratorStatusProtocol.StatusResponse> {
    private static final Serializer<StatusRequest> SERIALIZER = new JacksonSerializer<>(StatusRequest.class);
    private static final Serializer<StatusResponse> RETURN_SERIALIZER = new JacksonSerializer<>(StatusResponse.class);

    @Override
    public Serializer<StatusRequest> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<StatusResponse> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record StatusRequest() {}

    public record TypeStatus(ServerType type, int servers, int instances, int freeInstanceSlots,
                             int onlinePlayers, int maxPlayers, int queued) {}

    public record StatusResponse(List<TypeStatus> types) {
        public StatusResponse {
            types = types == null ? List.of() : List.copyOf(types);
        }
    }
}
