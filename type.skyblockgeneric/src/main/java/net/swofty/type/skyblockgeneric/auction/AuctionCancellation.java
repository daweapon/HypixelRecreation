package net.swofty.type.skyblockgeneric.auction;

import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.auctions.AuctionCancelProtocol;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.SkyBlockDomain;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class AuctionCancellation {
    private AuctionCancellation() {}
    public static void cancel(SkyBlockPlayer player, UUID auction, UUID profile) {
        player.closeInventory();
        Thread.startVirtualThread(() -> {
            try {
                var response = new ProxyService(ServiceType.AUCTION_HOUSE).handleRequest(new AuctionCancelProtocol(),
                        new AuctionCancelProtocol.Request(auction, player.getUuid(), profile)).get(10, TimeUnit.SECONDS);
                if (!response.success()) { player.sendMessage("<c>" + response.error()); return; }
                PlayerDataService.runLifecycleTransition(player.getUuid(), () -> {
                    var handler = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
                    if (player.isOnline() && handler != null && profile.equals(handler.getCurrentProfileId())) {
                        player.sendMessage("<a>Auction cancelled. Collecting your item...");
                        AuctionClaims.claim(player, auction, profile, true);
                    }
                });
            } catch (Exception failure) {
                Logger.error(failure, "Auction cancellation failed for {}", auction);
                player.sendMessage("<c>The auction could not be cancelled. Reopen it and try again.");
            }
        });
    }
}
