package net.swofty.type.skyblockgeneric.auction;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.auctions.AuctionAddItemProtocol;
import net.swofty.commons.skyblock.auctions.AuctionCategories;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.skyblock.auctions.AuctionRules;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.data.datapoints.DatapointDouble;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.SkyBlockDomain;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAuctionEscrow;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAuctionStatistics;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointUUIDList;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AuctionCategoryComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class AuctionCreation {
    private static final Set<UUID> submitting = ConcurrentHashMap.newKeySet();

    private AuctionCreation() {}

    public static void submit(SkyBlockPlayer player) {
        submit(player, player.getSkyblockDataHandler().getCurrentProfileId());
    }

    public static void submit(SkyBlockPlayer player, UUID profile) { submit(player, profile, true); }

    public static void recover(SkyBlockPlayer player) {
        var handler = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
        if (handler != null && handler.get(SkyBlockDataHandler.Data.AUCTION_ESCROW, DatapointAuctionEscrow.class).getValue().isPending()) {
            submit(player, handler.getCurrentProfileId(), false);
        }
    }

    private static void submit(SkyBlockPlayer player, UUID profile, boolean create) {
        if (!submitting.add(profile)) {
            if (create) player.sendMessage("<e>Your auction creation is already being checked.");
            retryLater(player, profile);
            return;
        }
        Thread.startVirtualThread(() -> {
            try {
                var service = new ProxyService(ServiceType.AUCTION_HOUSE);
                if (!service.isOnline().get(5, TimeUnit.SECONDS)) {
                    player.sendMessage("<c>The Auction House is offline. Your escrow is unchanged.");
                    return;
                }
                var request = new AtomicReference<AuctionAddItemProtocol.AuctionAddItemMessage>();
                PlayerDataService.runLifecycleTransition(player.getUuid(), () -> {
                    var handler = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
                    if (!player.isOnline() || handler == null || !profile.equals(handler.getCurrentProfileId())) return;
                    var escrow = handler.get(SkyBlockDataHandler.Data.AUCTION_ESCROW, DatapointAuctionEscrow.class).getValue();
                    synchronized (escrow) {
                        if (!escrow.isPending()) {
                            if (!create || escrow.getItem() == null) return;
                            if (!AuctionEligibility.canList(escrow.getItem())) {
                                player.sendMessage("<c>This item cannot be auctioned.");
                                return;
                            }
                            var item = new AuctionItem(escrow.getItem().toUnderstandable(), player.getUuid(),
                                    Math.addExact(System.currentTimeMillis(), escrow.getDuration()), escrow.isBin(), escrow.getPrice());
                            item.setSellerProfile(profile);
                            if (escrow.getPrice() <= 0 || escrow.getDuration() <= 0) throw new IllegalArgumentException("Invalid listing");
                            var category = AuctionCategoryResolver.resolve(escrow.getItem());
                            var message = new AuctionAddItemProtocol.AuctionAddItemMessage(item, category);
                            long fee = AuctionRules.listingFee(escrow.getPrice(), escrow.getDuration(), escrow.isBin());
                            var purse = handler.get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class);
                            if (!stage(escrow, purse, message, fee)) {
                                player.sendMessage("<c>You do not have enough coins for the listing fee.");
                                return;
                            }
                        }
                    }
                    save(player);
                    var pending = new AuctionAddItemProtocol().getSerializer().deserialize(escrow.getPendingCreation());
                    if (!pending.item().getOriginator().equals(player.getUuid())
                            || pending.item().getSellerProfile() != null && !profile.equals(pending.item().getSellerProfile())) {
                        throw new IllegalStateException("Auction creation belongs to another profile");
                    }
                    request.set(pending);
                    if (create) player.closeInventory();
                });
                if (request.get() == null) return;
                var result = service.handleRequest(new AuctionAddItemProtocol(), request.get()).get(10, TimeUnit.SECONDS);
                PlayerDataService.runLifecycleTransition(player.getUuid(), () -> {
                    var handler = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
                    if (!player.isOnline() || handler == null || !profile.equals(handler.getCurrentProfileId())) return;
                    var datapoint = handler.get(SkyBlockDataHandler.Data.AUCTION_ESCROW, DatapointAuctionEscrow.class);
                    var escrow = datapoint.getValue();
                    if (!escrow.isPending()) return;
                    settle(escrow, handler.get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class),
                            handler.get(SkyBlockDataHandler.Data.AUCTION_ACTIVE_OWNED, DatapointUUIDList.class).getValue(),
                            handler.get(SkyBlockDataHandler.Data.AUCTION_STATISTICS, DatapointAuctionStatistics.class).getValue(), result);
                    save(player);
                    player.sendMessage(result.success() ? "<a>Your auction was created!" :
                            "<c>The listing was rejected. Your fee was refunded and your item is back in auction escrow.");
                });
            } catch (Exception exception) {
                Logger.error(exception, "Auction creation could not be confirmed for profile {}", profile);
                player.sendMessage("<e>Auction creation could not be confirmed. Reopen Create Auction to check your escrow or retry a pending listing.");
            } finally {
                submitting.remove(profile);
                retryLater(player, profile);
            }
        });
    }

    private static void retryLater(SkyBlockPlayer player, UUID profile) {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!player.isOnline()) return;
            var handler = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
            if (handler != null && profile.equals(handler.getCurrentProfileId())) recover(player);
        }).delay(TaskSchedule.seconds(20)).schedule();
    }

    static boolean stage(DatapointAuctionEscrow.AuctionEscrow escrow, DatapointDouble purse,
                         AuctionAddItemProtocol.AuctionAddItemMessage message, long fee) {
        synchronized (escrow) {
            if (escrow.isPending()) return true;
            String serialized = new AuctionAddItemProtocol().getSerializer().serialize(message);
            if (AuctionCoinReservation.reserve(purse, fee) == null) return false;
            escrow.setPendingCreation(serialized);
            escrow.setPendingFee(fee);
            escrow.setItem(null);
            return true;
        }
    }

    static void settle(DatapointAuctionEscrow.AuctionEscrow escrow, DatapointDouble purse,
                       java.util.List<UUID> owned, AuctionAddItemProtocol.AuctionAddItemResponse result) {
        settle(escrow, purse, owned, null, result);
    }

    static void settle(DatapointAuctionEscrow.AuctionEscrow escrow, DatapointDouble purse,
                       java.util.List<UUID> owned, DatapointAuctionStatistics.AuctionStatistics statistics,
                       AuctionAddItemProtocol.AuctionAddItemResponse result) {
        synchronized (escrow) {
            if (!escrow.isPending()) return;
            var pending = new AuctionAddItemProtocol().getSerializer().deserialize(escrow.getPendingCreation());
            if (!pending.item().getUuid().equals(result.uuid())) throw new IllegalStateException("Unexpected creation response");
            if (result.success()) {
                if (!owned.contains(result.uuid())) owned.add(result.uuid());
                if (statistics != null) {
                    statistics.increment(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.AUCTIONS_CREATED);
                    statistics.add(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.COINS_SPENT_ON_FEES,
                            escrow.getPendingFee());
                }
            } else {
                var restored = new SkyBlockItem(pending.item().getItem());
                synchronized (purse) { purse.setValue(purse.getValue() + escrow.getPendingFee()); }
                escrow.setItem(restored);
            }
            escrow.setPendingCreation(null);
            escrow.setPendingFee(0);
        }
    }

    private static void save(SkyBlockPlayer player) {
        PlayerDataService.domain(SkyBlockDomain.KEY).save(player);
    }
}
