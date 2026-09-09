package net.swofty.type.skyblockgeneric.auction;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.auctions.AuctionAddItemProtocol;
import net.swofty.commons.protocol.objects.auctions.AuctionBidProtocol;
import net.swofty.commons.protocol.objects.auctions.AuctionBidProtocol.*;
import net.swofty.commons.protocol.objects.auctions.AuctionFetchItemProtocol;
import net.swofty.commons.skyblock.auctions.AuctionCategories;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyPlayer;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.data.datapoints.DatapointDouble;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.SkyBlockDomain;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAuctionOperations;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAuctionStatistics;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointUUIDList;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class AuctionBidding {
    private static final Set<UUID> submitting = ConcurrentHashMap.newKeySet();
    private static final AuctionBidProtocol protocol = new AuctionBidProtocol();

    private AuctionBidding() {}

    public static void submit(SkyBlockPlayer player, UUID profile, UUID auction, long amount, boolean bin) {
        start(player, profile, () -> prepare(player, profile, auction, amount, bin));
    }

    public static void recover(SkyBlockPlayer player) {
        var handler = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
        if (handler != null) start(player, handler.getCurrentProfileId(), null);
    }

    private static void start(SkyBlockPlayer player, UUID profile, Supplier<Request> proposal) {
        if (!submitting.add(profile)) {
            if (proposal != null) player.sendMessage("<e>Your previous auction request is still being checked.");
            retryLater(player, profile);
            return;
        }
        Thread.startVirtualThread(() -> {
            boolean retry = false;
            try {
                Request request = onPlayer(player, profile, () -> {
                    var handler = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
                    var state = operations(handler).getValue();
                    return state.pending() == null ? null : protocol.getSerializer().deserialize(state.pending());
                });
                if (request == null && proposal != null) {
                    var prepared = proposal.get();
                    if (prepared == null) return;
                    request = onPlayer(player, profile, () -> {
                        var handler = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
                        var operations = operations(handler);
                        if (operations.getValue().pending() != null) {
                            return protocol.getSerializer().deserialize(operations.getValue().pending());
                        }
                        if (player.isIronman()) {
                            player.sendMessage("<c>Ironman profiles cannot use the Auction House.");
                            return null;
                        }
                        var item = prepared.bid().item();
                        if (item.getBids().stream().limit(prepared.bid().expected().bids().size())
                                .anyMatch(bid -> bid.uuid().equals(player.getUuid()))
                                && !active(handler).contains(item.getUuid())) {
                            player.sendMessage("<c>Continue bidding on the profile that placed your earlier bid.");
                            return null;
                        }
                        var purse = handler.get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class);
                        if (!canReserve(purse, amount(prepared))) {
                            player.sendMessage("<c>You do not have enough coins for this bid.");
                            return null;
                        }
                        commit(player, handler, () -> stage(operations, purse, prepared));
                        player.closeInventory();
                        player.sendMessage("<e>Checking your auction request...");
                        return prepared;
                    });
                }
                if (request == null) return;
                if (!profile.equals(request.profile()) || !player.getUuid().equals(request.player())) {
                    throw new IllegalStateException("Auction operation belongs to another profile or player");
                }
                retry = true;
                var response = new ProxyService(ServiceType.AUCTION_HOUSE).handleRequest(protocol, request).get(10, TimeUnit.SECONDS);
                validate(request, response);
                Request submitted = request;
                Boolean settled = onPlayer(player, profile, () -> {
                    var handler = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
                    var operations = operations(handler);
                    if (operations.getValue().pending() == null) return false;
                    commit(player, handler, () -> settle(operations,
                            handler.get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class), active(handler),
                            handler.get(SkyBlockDataHandler.Data.AUCTION_INACTIVE_BIDS, DatapointUUIDList.class).getValue(),
                            handler.get(SkyBlockDataHandler.Data.AUCTION_STATISTICS, DatapointAuctionStatistics.class).getValue(),
                            submitted, response));
                    player.sendMessage(response.success()
                            ? (submitted.bid().item().isBin() ? "<a>Purchase confirmed! Collect your item in View Bids."
                            : "<a>Your auction bid was placed!") : "<c>" + response.error());
                    return true;
                });
                retry = false;
                if (Boolean.TRUE.equals(settled) && response.success()) Thread.startVirtualThread(() -> notifyPlayers(player, submitted));
            } catch (Exception exception) {
                Logger.error(exception, "Auction bid recovery pending for profile {}", profile);
                if (player.isOnline()) player.sendMessage("<e>Your auction request is saved. It will be checked again automatically on its original profile.");
                retry = true;
            } finally {
                submitting.remove(profile);
                if (retry) retryLater(player, profile);
            }
        });
    }

    private static Request prepare(SkyBlockPlayer player, UUID profile, UUID auction, long amount, boolean bin) {
        try {
            var response = new ProxyService(ServiceType.AUCTION_HOUSE).handleRequest(new AuctionFetchItemProtocol(),
                    new AuctionFetchItemProtocol.AuctionFetchItemMessage(auction)).get(10, TimeUnit.SECONDS);
            if (!response.success() || response.item() == null) {
                player.sendMessage("<c>This auction could not be found. No coins were reserved.");
                return null;
            }
            AuctionItem item = response.item();
            if (item.isBin() != bin || bin && amount != item.getStartingPrice()
                    || item.getEndTime() <= System.currentTimeMillis()) {
                player.sendMessage("<c>This auction changed or ended. Reopen it to try again.");
                return null;
            }
            var ownerCoop = item.getSellerProfile() == null ? null : CoopDatabase.getFromMemberProfile(item.getSellerProfile());
            var buyerCoop = CoopDatabase.getFromMemberProfile(profile);
            if (item.getOriginator().equals(player.getUuid())
                    || ownerCoop != null && buyerCoop != null && ownerCoop.isSameAs(buyerCoop)) {
                player.sendMessage("<c>You cannot bid on your own or your co-op's auction.");
                return null;
            }
            var expected = AuctionAddItemProtocol.ExpectedState.of(item);
            item.setBids(new ArrayList<>(item.getBids()));
            item.getBids().add(new AuctionItem.Bid(System.currentTimeMillis(), player.getUuid(), amount));
            return new Request(UUID.randomUUID(), player.getUuid(), profile,
                    new AuctionAddItemProtocol.AuctionAddItemMessage(item, AuctionCategories.TOOLS, expected));
        } catch (Exception exception) {
            Logger.warn(exception, "Unable to load auction {} before reserving coins", auction);
            player.sendMessage("<c>The Auction House could not be reached. No coins were reserved; try again shortly.");
            return null;
        }
    }

    static void stage(DatapointAuctionOperations operations, DatapointDouble purse, Request request) {
        if (operations.getValue().pending() != null) throw new IllegalStateException("An auction request is already pending");
        String serialized = protocol.getSerializer().serialize(request);
        synchronized (purse) {
            if (!canReserve(purse, amount(request))) throw new IllegalStateException("Insufficient auction funds");
            purse.setFrom(new DatapointDouble("coins", purse.getValue() - amount(request)));
            operations.setFrom(new DatapointAuctionOperations(operations.getKey(), operations.getValue().next(serialized)));
        }
    }

    static void settle(DatapointAuctionOperations operations, DatapointDouble purse, List<UUID> active,
                       List<UUID> inactive, Request request, Response response) {
        settle(operations, purse, active, inactive, null, request, response);
    }

    static void settle(DatapointAuctionOperations operations, DatapointDouble purse, List<UUID> active,
                       List<UUID> inactive, DatapointAuctionStatistics.AuctionStatistics statistics,
                       Request request, Response response) {
        validate(request, response);
        if (operations.getValue().pending() == null) throw new IllegalStateException("No pending auction operation");
        var pending = protocol.getSerializer().deserialize(operations.getValue().pending());
        if (!pending.operation().equals(request.operation())) throw new IllegalStateException("Another auction operation is pending");
        if (response.success()) {
            if (!active.contains(response.auction()) && !inactive.contains(response.auction())) active.add(response.auction());
            if (statistics != null && !request.bid().item().isBin()) {
                statistics.increment(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.TOTAL_BIDS);
                statistics.maximum(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.HIGHEST_BID_MADE,
                        amount(request));
            }
        } else {
            synchronized (purse) { purse.setFrom(new DatapointDouble("coins", purse.getValue() + amount(pending))); }
        }
        operations.setFrom(new DatapointAuctionOperations(operations.getKey(), operations.getValue().next(null)));
    }

    private static void validate(Request request, Response response) {
        if (response == null || !request.operation().equals(response.operation())
                || !request.profile().equals(response.profile()) || !request.bid().item().getUuid().equals(response.auction())) {
            throw new IllegalStateException("Unexpected auction operation response");
        }
    }

    static long amount(Request request) {
        var bids = request.bid().item().getBids();
        var submitted = bids.getLast();
        long committed = bids.stream().limit(bids.size() - 1L)
                .filter(bid -> submitted.uuid().equals(bid.uuid()))
                .mapToLong(AuctionItem.Bid::value)
                .max().orElse(0L);
        return Math.subtractExact(submitted.value(), committed);
    }
    private static boolean canReserve(DatapointDouble purse, long amount) {
        return amount > 0 && Double.isFinite(purse.getValue()) && purse.getValue() >= amount;
    }
    private static DatapointAuctionOperations operations(SkyBlockDataHandler handler) {
        return handler.get(SkyBlockDataHandler.Data.AUCTION_OPERATIONS, DatapointAuctionOperations.class);
    }
    private static List<UUID> active(SkyBlockDataHandler handler) {
        return handler.get(SkyBlockDataHandler.Data.AUCTION_ACTIVE_BIDS, DatapointUUIDList.class).getValue();
    }
    private static void commit(SkyBlockPlayer player, SkyBlockDataHandler handler, Runnable mutation) {
        try {
            handler.commitAuctionOperation(operations(handler).getValue(), player, mutation);
        } catch (Exception exception) {
            player.kick("<e>Reconnect to recover your saved auction request.");
            throw exception;
        }
    }
    private static <T> T onPlayer(SkyBlockPlayer player, UUID profile, Supplier<T> action) throws Exception {
        var result = new CompletableFuture<T>();
        player.scheduler().scheduleNextTick(() -> {
            try {
                PlayerDataService.runLifecycleTransition(player.getUuid(), () -> {
                    var handler = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
                    result.complete(player.isOnline() && handler != null && profile.equals(handler.getCurrentProfileId())
                            ? action.get() : null);
                });
            } catch (Exception exception) { result.completeExceptionally(exception); }
        });
        return result.get(15, TimeUnit.SECONDS);
    }
    private static void retryLater(SkyBlockPlayer player, UUID profile) {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!player.isOnline()) return;
            var handler = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
            if (handler != null && profile.equals(handler.getCurrentProfileId())) start(player, profile, null);
        }).delay(TaskSchedule.seconds(20)).schedule();
    }
    private static void notifyPlayers(SkyBlockPlayer player, Request request) {
        try {
            var item = request.bid().item();
            var owner = new ProxyPlayer(item.getOriginator());
            if (owner.isOnline().get(5, TimeUnit.SECONDS)) {
                owner.sendMessage(Text.of("<click:run:'/ahview {0}'><e>{1} {2} your auction for <6>{3} coins<e>!</click>",
                        item.getUuid().toString(), player.getFullDisplayName(), item.isBin() ? "purchased" : "bid on", amount(request)));
            }
            if (!item.isBin()) {
                for (UUID bidder : request.bid().expected().bids().stream().map(AuctionItem.Bid::uuid).distinct().toList()) {
                    if (bidder.equals(player.getUuid())) continue;
                    var other = new ProxyPlayer(bidder);
                    if (other.isOnline().get(5, TimeUnit.SECONDS)) other.sendMessage(Text.of(
                            "<click:run:'/ahview {0}'><e>You were outbid! The new bid is <6>{1} coins<e>.</click>", item.getUuid().toString(), amount(request)));
                }
            }
        } catch (Exception exception) { Logger.warn(exception, "Auction notification failed for {}", request.operation()); }
    }
}
