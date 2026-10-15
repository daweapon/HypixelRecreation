package dev.daweapon.service.auction;

import dev.daweapon.commons.data.ProfileIndexes;
import dev.daweapon.commons.data.SharedData;
import dev.daweapon.commons.protocol.objects.auctions.AuctionAddItemProtocol;
import dev.daweapon.commons.skyblock.CoopStorage;
import dev.daweapon.commons.skyblock.SkyBlockProfileFields;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import org.bson.Document;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AuctionProfiles {
    public boolean owns(UUID player, UUID profile) {
        return profile != null && profilesOf(player).contains(profile);
    }

    public UUID sellerProfile(AuctionItem item) {
        return item.getSellerProfile() != null ? item.getSellerProfile() : legacyProfile(item, item.getOriginator(), true);
    }

    public UUID legacyProfile(AuctionItem item, UUID player, boolean seller) {
        UUID found = null;
        for (UUID profile : profilesOf(player)) {
            if (!tracks(profileDocument(profile), item.getUuid(), seller)) continue;
            if (found != null) return null;
            found = profile;
        }
        return found;
    }

    public boolean sameCoop(UUID sellerProfile, UUID bidderProfile) {
        if (sellerProfile.equals(bidderProfile)) return true;
        UUID sellerCoop = coopOf(sellerProfile);
        return sellerCoop != null && sellerCoop.equals(coopOf(bidderProfile));
    }

    public boolean auctionEligible(UUID profile) {
        Document document = profileDocument(profile);
        if (document == null) return false;
        String stored = document.getString("profile_mode");
        return stored == null || !stored.replace("\"", "").equalsIgnoreCase("IRONMAN");
    }

    protected List<UUID> profilesOf(UUID player) {
        try {
            return ProfileIndexes.decodeProfiles(SharedData.account().get(player, ProfileIndexes.SKYBLOCK));
        } finally { SharedData.account().unload(player); }
    }

    protected Document profileDocument(UUID profile) {
        try {
            String json = SharedData.profile().get(profile, SkyBlockProfileFields.DOCUMENT);
            return json == null ? null : Document.parse(json);
        } finally { SharedData.profile().unload(profile); }
    }

    protected UUID coopOf(UUID profile) { return CoopStorage.coopIdOfProfile(profile); }

    private static boolean tracks(Document profile, UUID auction, boolean seller) {
        if (profile == null) return false;
        for (String field : seller ? List.of("auction_owned", "auction_inactive_owned") : List.of("auction_bids", "auction_inactive_bids")) {
            String tracked = profile.getString(field);
            if (tracked != null && Arrays.asList(tracked.split(",")).contains(auction.toString())) return true;
        }
        if (seller && profile.getString("auction_escrow") != null) {
            String pending = new JSONObject(profile.getString("auction_escrow")).optString("pendingCreation", null);
            if (pending != null) return new AuctionAddItemProtocol().getSerializer().deserialize(pending).item().getUuid().equals(auction);
        }
        return false;
    }
}
