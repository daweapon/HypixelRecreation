package dev.daweapon.service.bazaar.endpoints;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.bazaar.BazaarSellProtocol;
import dev.daweapon.service.bazaar.BazaarMarket;
import dev.daweapon.commons.redis.RedisMessageHandler;
import org.tinylog.Logger;

import java.util.UUID;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that submits a new sell order to the bazaar market.
public class EndpointBazaarSellOrder implements RedisMessageHandler<
        BazaarSellProtocol.BazaarSellMessage,
        BazaarSellProtocol.BazaarSellResponse> {

    @Override
    public RedisProtocol protocol() {
        return new BazaarSellProtocol();
    }

    @Override
    public BazaarSellProtocol.BazaarSellResponse handle(BazaarSellProtocol.BazaarSellMessage msg, RedisMessageContext context) {

        String itemName    = msg.itemName();
        UUID   playerUUID  = msg.playerUUID();
        UUID   profileUUID = msg.profileUUID();
        double price       = msg.price();
        double amount      = msg.amount();

        try {
            BazaarMarket.get().submitSell(itemName, playerUUID, profileUUID, price, amount);
            Logger.info("Sell order submitted for {} by {} (profile: {}) — price={}, amount={}",
                    itemName, playerUUID, profileUUID, price, amount);
            return new BazaarSellProtocol.BazaarSellResponse(true, null);
        } catch (Exception e) {
            Logger.error(e, "Failed to submit sell order for {} by {}", itemName, playerUUID);
            return new BazaarSellProtocol.BazaarSellResponse(false, "Sell order failed");
        }
    }
}
