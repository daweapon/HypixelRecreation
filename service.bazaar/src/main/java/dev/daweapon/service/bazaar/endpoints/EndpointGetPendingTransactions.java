package dev.daweapon.service.bazaar.endpoints;

import dev.daweapon.commons.protocol.objects.bazaar.BazaarGetPendingTransactionsProtocol;
import dev.daweapon.commons.protocol.objects.bazaar.BazaarGetPendingTransactionsProtocol.BazaarGetPendingTransactionsMessage;
import dev.daweapon.commons.protocol.objects.bazaar.BazaarGetPendingTransactionsProtocol.BazaarGetPendingTransactionsResponse;
import dev.daweapon.commons.protocol.objects.bazaar.BazaarGetPendingTransactionsProtocol.PendingTransactionInfo;
import dev.daweapon.service.bazaar.PendingTransactionsDatabase;
import dev.daweapon.commons.redis.RedisMessageHandler;
import org.tinylog.Logger;

import java.util.List;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that lists a player's unhandled pending bazaar transactions for a given profile.
public class EndpointGetPendingTransactions implements RedisMessageHandler<
        BazaarGetPendingTransactionsMessage,
        BazaarGetPendingTransactionsResponse> {

    @Override
    public BazaarGetPendingTransactionsProtocol protocol() {
        return new BazaarGetPendingTransactionsProtocol();
    }

    @Override
    public BazaarGetPendingTransactionsResponse handle(BazaarGetPendingTransactionsMessage msg, RedisMessageContext context) {

        List<PendingTransactionsDatabase.PendingTransaction> pendingTransactions =
                PendingTransactionsDatabase.getPendingTransactions(msg.playerUUID(), msg.profileUUID());

        List<PendingTransactionInfo> transactionInfos = pendingTransactions.stream()
                .map(pt -> new PendingTransactionInfo(
                        pt.getId(),
                        pt.getTransaction().getClass().getSimpleName(),
                        pt.getTransaction().toJSON().toMap(),
                        pt.getCreatedAt()
                ))
                .toList();

        Logger.debug("Retrieved {} pending transactions for player {} on profile {}",
                transactionInfos.size(), msg.playerUUID(), msg.profileUUID());

        return new BazaarGetPendingTransactionsResponse(transactionInfos, true, null);
    }
}
