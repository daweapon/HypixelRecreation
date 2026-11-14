package dev.daweapon.commons.protocol.objects.replay;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ChooseReplayProtocolObject extends RedisProtocol
        <ChooseReplayProtocolObject.ChooseReplayMessage,
            ChooseReplayProtocolObject.ChooseReplayResponse> {

    @Override
    public Serializer<ChooseReplayMessage> getSerializer() {
        return new JacksonSerializer<>(ChooseReplayMessage.class);
    }

    @Override
    public Serializer<ChooseReplayResponse> getReturnSerializer() {
        return new JacksonSerializer<>(ChooseReplayResponse.class);
    }

    public record ChooseReplayMessage(UUID player, String replayId, @Nullable String shareCode) {
        public ChooseReplayMessage(UUID player, String replayId) {
            this(player, replayId, null);
        }
    }

    public record ChooseReplayResponse(boolean error) {
    }
}
