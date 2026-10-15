package dev.daweapon.service.bazaar.endpoints;

import com.mongodb.client.model.Filters;
import dev.daweapon.commons.protocol.objects.bazaar.BazaarCancelProtocol;
import dev.daweapon.commons.protocol.objects.bazaar.BazaarCancelProtocol.CancelMessage;
import dev.daweapon.commons.protocol.objects.bazaar.BazaarCancelProtocol.CancelResponse;
import dev.daweapon.service.bazaar.BazaarMarket;
import dev.daweapon.service.bazaar.OrderDatabase;
import dev.daweapon.commons.redis.RedisMessageHandler;
import org.tinylog.Logger;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that cancels a player's bazaar order, removing it from both storage and the in-memory market.
public class EndpointCancelBazaarOrder implements RedisMessageHandler<
        CancelMessage, CancelResponse> {

    @Override
    public BazaarCancelProtocol protocol() {
        return new BazaarCancelProtocol();
    }

    @Override
    public CancelResponse handle(CancelMessage msg, RedisMessageContext context) {
        var result = OrderDatabase.ordersCollection.deleteOne(
                Filters.and(
                        Filters.eq("_id", msg.orderId().toString()),
                        Filters.eq("owner", msg.playerUuid().toString()),
                        Filters.eq("profileUuid", msg.profileUuid().toString())
                )
        );
        BazaarMarket.get().submitDelete(msg.orderId(), msg.playerUuid(), msg.profileUuid());
        Logger.info("Deleted order {} for player {} and profile {}",
                msg.orderId(), msg.playerUuid(), msg.profileUuid());

        boolean success = result.getDeletedCount() > 0;
        return new CancelResponse(success, success ? null : "Cancel failed");
    }
}
