package net.swofty.service.auction.endpoints;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.swofty.commons.protocol.objects.auctions.AuctionClaimProtocol;
import net.swofty.commons.protocol.objects.auctions.AuctionClaimProtocol.*;
import net.swofty.commons.redis.*;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.skyblock.auctions.AuctionRules;
import net.swofty.service.auction.*;
import org.bson.Document;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.UUID;

public class EndpointClaim implements RedisMessageHandler<Request, Response> {
    private final AuctionProfiles profiles;

    public EndpointClaim() { this(new AuctionProfiles()); }
    EndpointClaim(AuctionProfiles profiles) { this.profiles = profiles; }

    @Override public AuctionClaimProtocol protocol() { return new AuctionClaimProtocol(); }
    @Override public Response handle(Request request, RedisMessageContext context) {
        if (request.auction()==null || request.player()==null || request.profile()==null) return rejected("Invalid claim.");
        MongoCollection<Document> collection=AuctionActiveDatabase.collection;
        var query=Filters.eq("_id",request.auction().toString());
        Document stored=collection.find(query).first();
        if (stored==null) { collection=AuctionInactiveDatabase.collection; stored=collection.find(query).first(); }
        if (stored==null) return rejected("This auction could not be found.");
        if (!profiles.owns(request.player(), request.profile())) return rejected("This profile does not belong to you.");
        var bidProfiles = stored.get("bid_profiles", Document.class);
        String bidProfile = bidProfiles == null ? null : bidProfiles.getString(request.player().toString());
        if (bidProfile != null && !bidProfile.equals(request.profile().toString())) return rejected("Claim this auction on its original profile.");
        String field="claims."+request.player();
        var claims=stored.get("claims",Document.class);
        if (claims!=null && claims.containsKey(request.player().toString())) return existing(claims.getString(request.player().toString()),request);
        AuctionItem item=AuctionItem.fromDocument(stored);
        var winner=item.getBids().stream().max(Comparator.comparingLong(AuctionItem.Bid::value)).orElse(null);
        if (item.getEndTime()>System.currentTimeMillis() && !(item.isBin() && winner!=null)) return rejected("This auction has not ended.");
        boolean seller=request.player().equals(item.getOriginator());
        UUID ownerProfile = profiles.sellerProfile(item);
        UUID entitlementProfile = seller ? ownerProfile : bidProfile != null ? UUID.fromString(bidProfile)
                : profiles.legacyProfile(item, request.player(), false);
        if (!request.profile().equals(entitlementProfile)) return rejected("Claim this auction on its original profile.");
        long coins=0;
        boolean receivesItem=false;
        if (seller) {
            if (winner==null) receivesItem=true;
            else coins=Math.subtractExact(winner.value(), AuctionRules.collectionTax(winner.value(), request.collectionTaxMultiplier()));
        } else {
            boolean participated=false;
            for (var bid:item.getBids()) if (request.player().equals(bid.uuid())) {
                participated=true; coins=Math.addExact(coins,bid.value());
            }
            if (!participated) return rejected("You have no claim on this auction.");
            if (winner!=null && request.player().equals(winner.uuid())) {
                coins=Math.subtractExact(coins,winner.value()); receivesItem=true;
            }
        }
        var receipt=new Receipt(UUID.nameUUIDFromBytes((request.auction()+":"+request.player()).getBytes(StandardCharsets.UTF_8)),
                request.auction(),request.player(),request.profile(),seller,coins,
                receivesItem ? item.getItem().serialize() : null, winner == null ? 0 : winner.value(),
                winner != null, item.getItem().serialize());
        var response=new Response(true,null,receipt);
        String serialized=protocol().getReturnSerializer().serialize(response);
        var result=collection.updateOne(Filters.and(query,Filters.exists(field,false),Filters.eq("end",stored.getLong("end")),
                Filters.eq("bids",stored.getList("bids",String.class)), Filters.eq("seller_profile", stored.get("seller_profile"))),
                Updates.combine(Updates.set(field,serialized), seller ? Updates.set("seller_profile", ownerProfile.toString())
                        : Updates.set("bid_profiles." + request.player(), entitlementProfile.toString())));
        if (result.getModifiedCount()==1) return response;
        var latest=collection.find(query).first();
        var saved=latest==null ? null : latest.get("claims",Document.class);
        return saved!=null && saved.containsKey(request.player().toString())
                ? existing(saved.getString(request.player().toString()),request) : rejected("Auction changed. Reopen it and try again.");
    }
    private Response existing(String serialized, Request request) {
        var response=protocol().getReturnSerializer().deserialize(serialized);
        return response.receipt().profile().equals(request.profile()) ? response : rejected("Claim this auction on its original profile.");
    }
    private Response rejected(String error) { return new Response(false,error,null); }
}
