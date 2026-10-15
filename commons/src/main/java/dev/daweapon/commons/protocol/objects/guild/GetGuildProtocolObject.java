package dev.daweapon.commons.protocol.objects.guild;

import dev.daweapon.commons.guild.GuildData;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.UUID;

public class GetGuildProtocolObject extends RedisProtocol<GetGuildProtocolObject.GetGuildMessage, GetGuildProtocolObject.GetGuildResponse> {
    @Override
    public Serializer<GetGuildMessage> getSerializer() {
        return new JacksonSerializer<>(GetGuildMessage.class);
    }

    @Override
    public Serializer<GetGuildResponse> getReturnSerializer() {
        return new JacksonSerializer<>(GetGuildResponse.class);
    }

    public record GetGuildMessage(UUID memberUUID) {
    }

    public record GetGuildResponse(GuildData guild) {
    }
}
