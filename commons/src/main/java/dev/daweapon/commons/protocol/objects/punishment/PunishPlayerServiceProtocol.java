package dev.daweapon.commons.protocol.objects.punishment;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.commons.punishment.PunishmentReason;
import dev.daweapon.commons.punishment.PunishmentTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class PunishPlayerServiceProtocol
        extends RedisProtocol<PunishPlayerServiceProtocol.PunishPlayerMessage,
        PunishPlayerServiceProtocol.PunishPlayerResponse> {
    private static final Serializer<PunishPlayerMessage> SERIALIZER =
            new JacksonSerializer<>(PunishPlayerMessage.class);
    private static final Serializer<PunishPlayerResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(PunishPlayerResponse.class);

    @Override
    public Serializer<PunishPlayerMessage> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<PunishPlayerResponse> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record PunishPlayerMessage(
            @NotNull UUID target,
            @NotNull String type,
            @NotNull PunishmentReason reason,
            @NotNull UUID staff,
            List<PunishmentTag> tags,
            long expiresAt
    ) {}

    public record PunishPlayerResponse(
            boolean success,
            @Nullable String punishmentId,
            @Nullable ErrorCode errorCode,
            @Nullable String errorMessage
    ) {}

    public enum ErrorCode {
        INVALID_TYPE,
        DATABASE_ERROR,
        INVALID_EXPIRY,
        ALREADY_PUNISHED,
        UNKNOWN_ERROR
    }
}
