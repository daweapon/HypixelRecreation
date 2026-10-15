package dev.daweapon.service.bazaar.endpoints;

import dev.daweapon.commons.protocol.objects.bazaar.BazaarBuyProtocol;
import dev.daweapon.service.bazaar.BazaarMarket;
import dev.daweapon.commons.redis.RedisMessageHandler;
import org.tinylog.Logger;

import java.util.UUID;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that submits a new buy order to the bazaar market.
public class EndpointBazaarBuyOrder implements RedisMessageHandler<
        BazaarBuyProtocol.BazaarBuyMessage,
        BazaarBuyProtocol.BazaarBuyResponse> {

    @Override
    public BazaarBuyProtocol protocol() {
        return new BazaarBuyProtocol();
    }

    @Override
    public BazaarBuyProtocol.BazaarBuyResponse handle(BazaarBuyProtocol.BazaarBuyMessage msg, RedisMessageContext context) {

        String itemName    = msg.itemName();
        UUID   playerUUID  = msg.playerUUID();
        UUID   profileUUID = msg.profileUUID();
        double price       = msg.price();
        double amount      = msg.amount();

        try {
            BazaarMarket.get().submitBuy(itemName, playerUUID, profileUUID, price, amount);
            Logger.info("Buy order submitted for {} by {} (profile: {}) — price={}, amount={}",
                    itemName, playerUUID, profileUUID, price, amount);
            return new BazaarBuyProtocol.BazaarBuyResponse(true, null);
        } catch (Exception e) {
            Logger.error(e, "Failed to submit buy order for {} by {}", itemName, playerUUID);
            return new BazaarBuyProtocol.BazaarBuyResponse(false, "Buy order failed");
        }
    }
}
