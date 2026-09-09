package net.swofty.service.auction.endpoints;

import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.service.auction.AuctionProfiles;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

final class AuctionTestProfiles extends AuctionProfiles {
    static UUID profile(UUID player) { return UUID.nameUUIDFromBytes(("auction-test-profile:" + player).getBytes(StandardCharsets.UTF_8)); }
    @Override public boolean owns(UUID player, UUID profile) { return profile != null; }
    @Override public UUID sellerProfile(AuctionItem item) { return item.getSellerProfile() == null ? profile(item.getOriginator()) : item.getSellerProfile(); }
    @Override public UUID legacyProfile(AuctionItem item, UUID player, boolean seller) { return profile(player); }
    @Override public boolean sameCoop(UUID sellerProfile, UUID bidderProfile) { return false; }
    @Override public boolean auctionEligible(UUID profile) { return true; }
}
