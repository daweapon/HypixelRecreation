package net.swofty.service.auction.endpoints;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.swofty.commons.protocol.objects.auctions.AuctionBidProtocol;
import net.swofty.commons.protocol.objects.auctions.AuctionBidProtocol.*;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.service.auction.AuctionActiveDatabase;
import net.swofty.service.auction.AuctionProfiles;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.service.auction.AuctionInactiveDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EndpointBid implements RedisMessageHandler<Request, Response> {
    private final AuctionProfiles profileLookup;

    public EndpointBid() { this(new AuctionProfiles()); }
    EndpointBid(AuctionProfiles profileLookup) { this.profileLookup = profileLookup; }

    @Override public AuctionBidProtocol protocol() { return new AuctionBidProtocol(); }

    @Override public Response handle(Request request, RedisMessageContext context) {
        if (request.operation() == null || request.player() == null || request.profile() == null
                || request.bid() == null || request.bid().expected() == null || request.bid().item() == null
                || request.bid().item().getBids().isEmpty()
                || !request.player().equals(request.bid().item().getBids().getLast().uuid())) {
            throw new IllegalArgumentException("Invalid auction operation");
        }
        var id = request.bid().item().getUuid();
        String receiptKey = "bid_operations." + request.operation();
        String fingerprint = fingerprint(request);
        for (int attempt = 0; attempt < 16; attempt++) {
            var collection = AuctionActiveDatabase.collection;
            var stored = collection.find(Filters.eq("_id", id.toString())).first();
            boolean archived = false;
            if (stored == null) {
                collection = AuctionInactiveDatabase.collection;
                stored = collection.find(Filters.eq("_id", id.toString())).first();
                archived = true;
            }
            // Absence is not a durable rejection: an archive transition may be in progress.
            if (stored == null) throw new IllegalStateException("Auction unavailable; retry this operation");
            Document receipts = stored.get("bid_operations", Document.class);
            Document receipt = receipts == null ? null : receipts.get(request.operation().toString(), Document.class);
            if (receipt != null) {
                if (!fingerprint.equals(receipt.getString("request"))) {
                    throw new IllegalArgumentException("Auction operation ID reused with a different request");
                }
                return protocol().getReturnSerializer().deserialize(receipt.getString("response"));
            }
            Document profiles = stored.get("bid_profiles", Document.class);
            String boundProfile = profiles == null ? null : profiles.getString(request.player().toString());
            AuctionItem listing = AuctionItem.fromDocument(stored);
            UUID sellerProfile = profileLookup.sellerProfile(listing);
            if (boundProfile == null && listing.getBids().stream().anyMatch(bid -> request.player().equals(bid.uuid()))) {
                UUID legacyProfile = profileLookup.legacyProfile(listing, request.player(), false);
                boundProfile = legacyProfile == null ? "unknown" : legacyProfile.toString();
            }
            boolean eligible = sellerProfile != null && profileLookup.owns(request.player(), request.profile())
                    && profileLookup.auctionEligible(request.profile())
                    && !profileLookup.sameCoop(sellerProfile, request.profile());
            long now = System.currentTimeMillis();
            Bson bidUpdate = archived || !eligible || boundProfile != null && !boundProfile.equals(request.profile().toString())
                    ? null : EndpointAddItem.bidUpdate(request.bid(), stored, now);
            Response response = new Response(request.operation(), id, request.profile(), bidUpdate != null,
                    bidUpdate == null ? rejectionMessage(request, stored, archived, sellerProfile, boundProfile, now) : null);
            var updates = new ArrayList<Bson>();
            updates.add(Updates.set(receiptKey, new Document("request", fingerprint)
                    .append("response", protocol().getReturnSerializer().serialize(response))));
            var conditions = new ArrayList<>(List.of(Filters.eq("_id", id.toString()),
                    Filters.exists(receiptKey, false), Filters.eq("end", stored.get("end")),
                    Filters.eq("bids", stored.get("bids")), Filters.eq("seller_profile", stored.get("seller_profile"))));
            if (bidUpdate != null) {
                conditions.add(Filters.gt("end", System.currentTimeMillis()));
                updates.add(bidUpdate);
                updates.add(Updates.set("seller_profile", sellerProfile.toString()));
                updates.add(Updates.set("bid_profiles." + request.player(), request.profile().toString()));
            }
            if (collection.updateOne(Filters.and(conditions), Updates.combine(updates)).getModifiedCount() == 1) return response;
        }
        throw new IllegalStateException("Concurrent auction updates; retry this operation");
    }

    private String rejectionMessage(Request request, Document stored, boolean archived, UUID sellerProfile,
                                    String boundProfile, long now) {
        if (archived || stored.getLong("end") <= now) return "This auction has ended. Your coins were returned.";
        if (sellerProfile == null) return "This auction's seller profile could not be verified. Your coins were returned.";
        if (!profileLookup.owns(request.player(), request.profile()))
            return "Your selected profile could not be verified. Your coins were returned.";
        if (!profileLookup.auctionEligible(request.profile()))
            return "Ironman profiles cannot use the Auction House. Your coins were returned.";
        if (profileLookup.sameCoop(sellerProfile, request.profile()))
            return "You cannot bid on your own or your co-op's auction. Your coins were returned.";
        if (boundProfile != null && !boundProfile.equals(request.profile().toString()))
            return "Continue bidding on the profile that placed your earlier bid. Your coins were returned.";
        if (request.bid().item().isBin()) {
            Number created = stored.get("created_at", Number.class);
            if (created != null && now - created.longValue() < net.swofty.commons.skyblock.auctions.AuctionRules.BIN_PURCHASE_GRACE) {
                long remaining = Math.max(1L, Math.ceilDiv(
                        net.swofty.commons.skyblock.auctions.AuctionRules.BIN_PURCHASE_GRACE - (now - created.longValue()), 1_000L));
                return "New BIN auctions cannot be purchased for 20 seconds. Try again in " + remaining
                        + (remaining == 1 ? " second." : " seconds.") + " Your coins were returned.";
            }
        }
        return "This auction changed or rejected the bid. Your coins were returned.";
    }

    private static String fingerprint(Request request) {
        var item = request.bid().item();
        var expected = request.bid().expected();
        var identity = new Document("operation", request.operation().toString())
                .append("player", request.player().toString()).append("profile", request.profile().toString())
                .append("auction", item.getUuid().toString()).append("owner", item.getOriginator().toString())
                .append("bin", item.isBin()).append("price", item.getStartingPrice().longValue())
                .append("expectedEnd", expected.endTime())
                .append("expectedBids", expected.bids().stream().map(Object::toString).toList())
                .append("bids", item.getBids().stream().map(Object::toString).toList());
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(identity.toJson().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
    }

}
