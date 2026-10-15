package dev.daweapon.commons.protocol.objects.bazaar;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class BazaarProcessPendingTransactionsProtocol extends RedisProtocol<
        BazaarProcessPendingTransactionsProtocol.BazaarProcessPendingTransactionsMessage,
        BazaarProcessPendingTransactionsProtocol.BazaarProcessPendingTransactionsResponse> {
    private static final Serializer<BazaarProcessPendingTransactionsMessage> SERIALIZER =
            new JacksonSerializer<>(BazaarProcessPendingTransactionsMessage.class);
    private static final Serializer<BazaarProcessPendingTransactionsResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(BazaarProcessPendingTransactionsResponse.class);

    @Override
    public Serializer<BazaarProcessPendingTransactionsMessage> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<BazaarProcessPendingTransactionsResponse> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record BazaarProcessPendingTransactionsMessage(UUID playerUUID, UUID profileUUID, List<String> transactionIds) {}

    public record BazaarProcessPendingTransactionsResponse(int processedCount, int failedCount, List<String> successfulTransactionIds, List<String> failedTransactionIds, boolean success, @Nullable String error) {}
}
