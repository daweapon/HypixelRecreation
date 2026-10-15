package dev.daweapon.commons.protocol.objects.punishment;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class UnpunishPlayerProtocol
        extends RedisProtocol<UnpunishPlayerProtocol.UnpunishPlayerMessage,
        UnpunishPlayerProtocol.UnpunishPlayerResponse> {
    private static final Serializer<UnpunishPlayerMessage> SERIALIZER =
            new JacksonSerializer<>(UnpunishPlayerMessage.class);
    private static final Serializer<UnpunishPlayerResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(UnpunishPlayerResponse.class);

    @Override
    public Serializer<UnpunishPlayerMessage> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<UnpunishPlayerResponse> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record UnpunishPlayerMessage(
            @NotNull UUID target,
            @NotNull UUID staff,
            @NotNull String type
    ) {}

    public record UnpunishPlayerResponse(
            boolean success,
            @Nullable String errorMessage
    ) {}
}
