package dev.daweapon.service.auction.endpoints;

import com.mongodb.MongoWriteException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import dev.daweapon.commons.skyblock.auctions.AuctionRules;
import dev.daweapon.commons.protocol.objects.auctions.AuctionAddItemProtocol;
import dev.daweapon.service.auction.AuctionActiveDatabase;
import dev.daweapon.service.auction.AuctionProfiles;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisMessageContext;
import org.bson.Document;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class EndpointAddItem implements RedisMessageHandler<
        AuctionAddItemProtocol.AuctionAddItemMessage,
        AuctionAddItemProtocol.AuctionAddItemResponse> {
    private final AuctionProfiles profiles;

    public EndpointAddItem() { this(new AuctionProfiles()); }
    EndpointAddItem(AuctionProfiles profiles) { this.profiles = profiles; }

    @Override
    public AuctionAddItemProtocol protocol() {
        return new AuctionAddItemProtocol();
    }

    @Override
    public AuctionAddItemProtocol.AuctionAddItemResponse handle(AuctionAddItemProtocol.AuctionAddItemMessage message,
                                                               RedisMessageContext context) {
        AuctionItem proposed = message.item();
        UUID id = proposed.getUuid();
        if (message.expected() == null) {
            Document creation = proposed.toDocument().append("category", message.category().name());
            Document existing = AuctionActiveDatabase.collection.find(Filters.eq("_id", id.toString())).first();
            if (existing != null) return creationResult(id, creation, existing);
            UUID sellerProfile = profiles.sellerProfile(proposed);
            if (sellerProfile != null) {
                if (!profiles.owns(proposed.getOriginator(), sellerProfile) || !profiles.auctionEligible(sellerProfile))
                    return rejected(id);
                proposed.setSellerProfile(sellerProfile);
                creation = proposed.toDocument().append("category", message.category().name());
            }
            // A delayed retry must commit the same listing even after its end time.
            if (!proposed.getBids().isEmpty() || proposed.getStartingPrice() <= 0)
                return rejected(id);
            Document document = new Document(creation).append("creation", creation)
                    .append("created_at", System.currentTimeMillis());
            try {
                AuctionActiveDatabase.collection.insertOne(document);
                return new AuctionAddItemProtocol.AuctionAddItemResponse(id, true, null);
            } catch (MongoWriteException exception) {
                if (exception.getError().getCode() == 11000) {
                    return creationResult(id, creation,
                            AuctionActiveDatabase.collection.find(Filters.eq("_id", id.toString())).first());
                }
                throw exception;
            }
        }

        return new AuctionAddItemProtocol.AuctionAddItemResponse(id, false,
                "Bids require the profile-aware auction protocol. Reopen the auction and try again.");
    }

    static org.bson.conversions.Bson bidUpdate(AuctionAddItemProtocol.AuctionAddItemMessage message,
                                              Document stored, long now) {
        AuctionItem proposed = message.item();
        var expected = message.expected();
        var bids = expected.bids();
        if (bids == null || !Objects.equals(stored.getLong("end"), expected.endTime())
                || !Objects.equals(stored.getList("bids", String.class), bids.stream().map(AuctionItem.Bid::toString).toList())
                || stored.getLong("end") <= now || proposed.isBin() != stored.getBoolean("bin")
                || !proposed.getOriginator().toString().equals(stored.getString("originator"))
                || proposed.getStartingPrice().longValue() != ((Number) stored.get("starting-price")).longValue()
                || proposed.getBids().size() != bids.size() + 1
                || !proposed.getBids().subList(0, bids.size()).equals(bids)) return null;

        var bid = proposed.getBids().getLast();
        if (bid.uuid() == null || bid.uuid().toString().equals(stored.getString("originator"))
                || bid.value() == null || bid.value() <= 0) return null;
        var highest = bids.stream().max(Comparator.comparingLong(AuctionItem.Bid::value)).orElse(null);
        if (proposed.isBin()) {
            Number created = stored.get("created_at", Number.class);
            if (!bids.isEmpty() || bid.value().longValue() != proposed.getStartingPrice().longValue()
                    || created != null && now - created.longValue() < AuctionRules.BIN_PURCHASE_GRACE) return null;
        } else if (bid.value() < AuctionRules.minimumBid(proposed.getStartingPrice(), highest == null ? null : highest.value())) {
            return null;
        }


        long end = proposed.isBin() ? now
                : (expected.endTime() - now < 120_000 ? Math.addExact(now, 120_000) : expected.endTime());
        return Updates.combine(Updates.push("bids", new AuctionItem.Bid(now, bid.uuid(), bid.value()).toString()),
                Updates.set("end", end));
    }

    private static AuctionAddItemProtocol.AuctionAddItemResponse creationResult(UUID id, Document creation, Document stored) {
        return stored != null && sameCreation(creation, stored.get("creation", Document.class))
                ? new AuctionAddItemProtocol.AuctionAddItemResponse(id, true, null) : rejected(id);
    }

    private static boolean sameCreation(Document requested, Document saved) {
        if (saved == null) return false;
        Document normalized = new Document(saved);
        if (normalized.get("starting-price") instanceof Number price) normalized.put("starting-price", price.longValue());
        Document proposed = new Document(requested);
        if (proposed.get("starting-price") instanceof Number price) proposed.put("starting-price", price.longValue());
        return proposed.equals(normalized);
    }

    private static AuctionAddItemProtocol.AuctionAddItemResponse rejected(UUID id) {
        return new AuctionAddItemProtocol.AuctionAddItemResponse(id, false,
                "This auction changed, ended, or rejected the bid. Refresh it and try again.");
    }
}
