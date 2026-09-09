package net.swofty.commons.protocol.serializers;

import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.commons.protocol.Serializer;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

// Serializes a single AuctionItem to/from JSON, including its nested SkyBlock item and bid history.
public class AuctionItemSerializer<T> implements Serializer<AuctionItem> {

    @Override
    public String serialize(AuctionItem value) {
        UUID uuid = value.getUuid();
        UUID originator = value.getOriginator();
        UnderstandableSkyBlockItem item = value.getItem();
        long endTime = value.getEndTime();
        boolean isBin = value.isBin();
        Long startingPrice = value.getStartingPrice();

        JSONObject obj = new JSONObject();
        obj.put("uuid", uuid.toString());
        obj.put("originator", originator.toString());
        if (value.getSellerProfile() != null) obj.put("seller_profile", value.getSellerProfile().toString());
        obj.put("item", new UnderstandableSkyBlockItemSerializer().serialize(item));
        obj.put("end", endTime);
        obj.put("bin", isBin);
        obj.put("starting-price", startingPrice);
        obj.put("bids", value.getBids().stream().map(AuctionItem.Bid::toString).toList());
        return obj.toString();
    }

    @Override
    public AuctionItem deserialize(String json) {
        JSONObject obj = new JSONObject(json);
        UUID uuid = UUID.fromString(obj.getString("uuid"));
        UUID originator = UUID.fromString(obj.getString("originator"));
        UnderstandableSkyBlockItem item = new UnderstandableSkyBlockItemSerializer().deserialize(obj.getString("item"));
        long endTime = obj.getLong("end");
        boolean isBin = obj.getBoolean("bin");
        Long startingPrice = obj.getLong("starting-price");
        List<AuctionItem.Bid> bids = obj.getJSONArray("bids").toList().stream()
                .map(Object::toString)
                .map(AuctionItem.Bid::fromString)
                .toList();

        AuctionItem auctionItem = new AuctionItem();
        auctionItem.setUuid(uuid);
        auctionItem.setOriginator(originator);
        if (obj.has("seller_profile") && !obj.isNull("seller_profile")) auctionItem.setSellerProfile(UUID.fromString(obj.getString("seller_profile")));
        auctionItem.setItem(item);
        auctionItem.setEndTime(endTime);
        auctionItem.setBin(isBin);
        auctionItem.setStartingPrice(startingPrice);
        auctionItem.setBids(bids);
        return auctionItem;
    }

    @Override
    public AuctionItem clone(AuctionItem value) {
        return deserialize(serialize(value));
    }
}