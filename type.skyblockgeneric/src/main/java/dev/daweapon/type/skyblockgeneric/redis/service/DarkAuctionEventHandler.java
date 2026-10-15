package dev.daweapon.type.skyblockgeneric.redis.service;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.darkauction.DarkAuctionEventPushProtocol;
import dev.daweapon.commons.protocol.objects.darkauction.DarkAuctionEventPushProtocol.Request;
import dev.daweapon.commons.protocol.objects.darkauction.DarkAuctionEventPushProtocol.Response;
import dev.daweapon.commons.protocol.objects.darkauction.DarkAuctionEventProtocol;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.darkauction.DarkAuctionHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tinylog.Logger;
import dev.daweapon.commons.redis.RedisMessageContext;

public class DarkAuctionEventHandler implements RedisMessageHandler<Request, Response> {

    private static final DarkAuctionEventPushProtocol PROTOCOL = new DarkAuctionEventPushProtocol();

    @Override
    public RedisProtocol<Request, Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public Response handle(Request request, RedisMessageContext context) {
        if (HypixelConst.getTypeLoader().getType() != ServerType.SKYBLOCK_HUB) {
            return null;
        }

        try {
            DarkAuctionEventProtocol.EventType eventType = DarkAuctionEventProtocol.EventType.valueOf(request.eventType());

            Logger.info("Received Dark Auction event: {}", eventType);

            JSONObject message = toJson(request);

            switch (eventType) {
                case AUCTION_START -> DarkAuctionHandler.handleAuctionStart(message);
                case AUCTION_BEGIN -> DarkAuctionHandler.handleAuctionBegin(message);
                case ROUND_START -> DarkAuctionHandler.handleRoundStart(message);
                case BID_PLACED -> DarkAuctionHandler.handleBidPlaced(message);
                case ROUND_END -> DarkAuctionHandler.handleRoundEnd(message);
                case AUCTION_END -> DarkAuctionHandler.handleAuctionEnd(message);
            }

            return Response.success(DarkAuctionHandler.getPlayersInAuctionCount());
        } catch (Exception e) {
            Logger.error(e, "Error handling Dark Auction event");
            return Response.failure(e.getMessage());
        }
    }

    private JSONObject toJson(Request request) {
        JSONObject json = new JSONObject();
        json.put("eventType", request.eventType());
        json.put("auctionId", request.auctionId());
        json.put("phase", request.phase());
        json.put("currentRound", request.currentRound());
        json.put("currentItemType", request.currentItemType());
        json.put("currentBid", request.currentBid());
        json.put("highestBidderId", request.highestBidderId());
        json.put("highestBidderName", request.highestBidderName());
        json.put("countdown", request.countdown());

        JSONArray itemsArray = new JSONArray();
        if (request.roundItems() != null) {
            for (String item : request.roundItems()) {
                itemsArray.put(item);
            }
        }
        json.put("roundItems", itemsArray);

        if (request.refundPlayerId() != null) {
            json.put("refundPlayerId", request.refundPlayerId());
            json.put("refundAmount", request.refundAmount());
        }

        if (request.newBidderId() != null) {
            json.put("newBidderId", request.newBidderId());
            json.put("newBidAmount", request.newBidAmount());
        }

        return json;
    }
}
