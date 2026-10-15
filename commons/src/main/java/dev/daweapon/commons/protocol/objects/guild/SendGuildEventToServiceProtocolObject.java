package dev.daweapon.commons.protocol.objects.guild;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

public class SendGuildEventToServiceProtocolObject extends RedisProtocol<SendGuildEventToServiceProtocolObject.SendGuildEventToServiceMessage, SendGuildEventToServiceProtocolObject.SendGuildEventToServiceResponse> {
    @Override
    public Serializer<SendGuildEventToServiceMessage> getSerializer() {
        return new JacksonSerializer<>(SendGuildEventToServiceMessage.class);
    }

    @Override
    public Serializer<SendGuildEventToServiceResponse> getReturnSerializer() {
        return new JacksonSerializer<>(SendGuildEventToServiceResponse.class);
    }

    public record SendGuildEventToServiceMessage(String eventType, String eventData) {
    }

    public record SendGuildEventToServiceResponse(boolean success) {
    }
}
