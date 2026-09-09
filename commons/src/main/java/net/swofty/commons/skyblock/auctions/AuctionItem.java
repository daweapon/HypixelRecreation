package net.swofty.commons.skyblock.auctions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.commons.protocol.serializers.AuctionItemSerializer;
import net.swofty.commons.protocol.serializers.UnderstandableSkyBlockItemSerializer;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
// A single Auction House listing: the item, its seller, timing, BIN/starting price, and accumulated bids, with Mongo/JSON (de)serialization.
public class AuctionItem {
    private UUID uuid;
    private UUID originator;
    private UUID sellerProfile;
    private UnderstandableSkyBlockItem item;
    private long endTime;

    private boolean isBin;
    private Long startingPrice;

    private List<Bid> bids;

    public AuctionItem() {
        this.bids = new ArrayList<>();
    }

    public AuctionItem(UnderstandableSkyBlockItem item, UUID originator, long endTime, boolean isBin, Long startingPrice) {
        this.uuid = UUID.randomUUID();
        this.originator = originator;
        this.item = item;
        this.endTime = endTime;
        this.isBin = isBin;
        this.startingPrice = startingPrice;

        this.bids = new ArrayList<>();
    }

    @JsonValue
    public String toSerializedString() {
        return new AuctionItemSerializer<>().serialize(this);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AuctionItem fromSerializedString(String json) {
        return new AuctionItemSerializer<>().deserialize(json);
    }

    public Document toDocument() {
        Document document = new Document()
                .append("_id", uuid.toString())
                .append("originator", originator.toString())
                .append("item", new UnderstandableSkyBlockItemSerializer().serialize(item))
                .append("end", endTime)
                .append("bin", isBin)
                .append("starting-price", startingPrice)
                .append("bids", bids.stream().map(Bid::toString).toList());
        if (sellerProfile != null) document.append("seller_profile", sellerProfile.toString());
        return document;
    }

    public static AuctionItem fromDocument(Document document) {
        AuctionItem item = new AuctionItem();
        item.setUuid(UUID.fromString(document.getString("_id")));
        item.setOriginator(UUID.fromString(document.getString("originator")));
        if (document.getString("seller_profile") != null) item.setSellerProfile(UUID.fromString(document.getString("seller_profile")));
        item.setItem(new UnderstandableSkyBlockItemSerializer().deserialize(document.getString("item")));
        item.setEndTime(document.getLong("end"));
        item.setBin(document.getBoolean("bin"));
        item.setStartingPrice(((Number) document.get("starting-price")).longValue());
        item.setBids(document.getList("bids", String.class).stream().map(Bid::fromString).toList());
        return item;
    }

    public record Bid(Long timestamp, UUID uuid, Long value) {
        public @NotNull String toString() {
            return timestamp + "," + uuid + "," + value;
        }

        public static Bid fromString(String string) {
            String[] parts = string.split(",");
            return new Bid(Long.parseLong(parts[0]), UUID.fromString(parts[1]), Long.parseLong(parts[2]));
        }
    }
}
