package net.swofty.type.skyblockgeneric.stonks;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointStonksAuctions;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.stonks.StonksAuctionProfile.Entry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Richard's Stonks Auction. Bids are coins spent for good, kept per profile and mirrored into a
 * Redis sorted set per auction, so every server ranks against the same bidders. When the month
 * ends each bidder's placement decides how much Stock of Stonks Richard hands out.
 */
public final class StonksAuctions {
    public static final String RICHARD = "<e>[NPC] Richard<f>: ";
    private static final long LEADERBOARD_TTL_SECONDS = Duration.ofDays(30).toSeconds();
    private static final Set<UUID> resolving = ConcurrentHashMap.newKeySet();

    private StonksAuctions() {}

    public static void start() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                if (player.getSkyblockDataHandler() != null) finaliseEnded(player);
            }
            return TaskSchedule.seconds(5);
        });
    }

    public static boolean isRunning() {
        return ElectionPerks.isActive(SkyBlockMayor.Perk.STOCK_EXCHANGE);
    }

    public static StonksAuctionProfile profile(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    private static void save(SkyBlockPlayer player, StonksAuctionProfile profile) {
        datapoint(player).setValue(profile);
    }

    private static DatapointStonksAuctions datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.STONKS_AUCTIONS, DatapointStonksAuctions.class);
    }

    public static UUID bidder(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().getCurrentProfileId();
    }

    public static long currentBid(SkyBlockPlayer player) {
        return profile(player).entryFor(StonksAuction.current()).map(Entry::getBid).orElse(0L);
    }

    public static Optional<String> bid(SkyBlockPlayer player, long newBid) {
        if (!isRunning()) return Optional.of("<c>The Stonks Auction only runs while Diaz is Mayor!");
        if (!LeaderboardService.isInitialized()) return Optional.of("<c>The Stonks Auction is unavailable right now, try again later!");
        StonksAuction auction = StonksAuction.current();
        StonksAuctionProfile profile = profile(player);
        Entry entry = profile.bidOn(auction);
        long previous = entry.getBid();
        if (newBid < StonksAuction.MINIMUM_BID) {
            return Optional.of("<c>The minimum bid is <6>" + StringUtility.commaify(StonksAuction.MINIMUM_BID) + " coins<c>!");
        }
        if (newBid <= previous) return Optional.of("<c>Your new bid must be higher than your current bid!");
        long difference = newBid - previous;
        if (player.getCoins() < difference) return Optional.of("<c>You don't have enough coins to bid that much!");

        player.removeCoins(difference);
        entry.setBid(newBid);
        save(player, profile);
        player.sendMessage("<a>You bid <6>" + StringUtility.commaify(newBid) + " coins <a>in the <d>Stonks Auction<a>!");

        UUID bidder = bidder(player);
        Thread.startVirtualThread(() -> {
            if (LeaderboardService.setExpiringScore(auction.leaderboardKey(), bidder, newBid, LEADERBOARD_TTL_SECONDS)) return;
            MinecraftServer.getSchedulerManager().scheduleNextTick(() -> refundFailedBid(player, auction, previous, newBid));
        });
        return Optional.empty();
    }

    private static void refundFailedBid(SkyBlockPlayer player, StonksAuction auction, long previous, long failedBid) {
        if (!player.isOnline() || player.getSkyblockDataHandler() == null) return;
        StonksAuctionProfile profile = profile(player);
        Entry entry = profile.entryFor(auction).orElse(null);
        if (entry == null || entry.getBid() != failedBid) return;
        entry.setBid(previous);
        if (previous == 0) profile.getEntries().remove(entry);
        save(player, profile);
        player.addCoins(failedBid - previous);
        player.sendMessage("<c>Your Stonks Auction bid couldn't be placed, so your coins were refunded.");
    }

    public static void finaliseEnded(SkyBlockPlayer player) {
        List<Entry> pending = profile(player).getEntries().stream()
                .filter(entry -> !entry.isFinalised() && entry.getBid() > 0 && entry.auction().hasEnded())
                .toList();
        if (pending.isEmpty() || !LeaderboardService.isInitialized()) return;
        UUID uuid = player.getUuid();
        if (!resolving.add(uuid)) return;
        UUID bidder = bidder(player);

        Thread.startVirtualThread(() -> {
            Map<String, Integer> placements = new HashMap<>();
            try {
                for (Entry entry : pending) {
                    placements.put(entry.getAuctionId(), LeaderboardService.getPlayerRank(entry.auction().leaderboardKey(), bidder));
                }
            } finally {
                MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                    try {
                        if (player.isOnline() && player.getSkyblockDataHandler() != null) applyPlacements(player, placements);
                    } finally {
                        resolving.remove(uuid);
                    }
                });
            }
        });
    }

    private static void applyPlacements(SkyBlockPlayer player, Map<String, Integer> placements) {
        StonksAuctionProfile profile = profile(player);
        for (Entry entry : profile.getEntries()) {
            Integer placement = placements.get(entry.getAuctionId());
            if (placement == null || entry.isFinalised()) continue;
            StonksAuctionTier tier = StonksAuctionTier.forPlacement(placement);
            entry.setPlacement(Math.max(placement, 0));
            entry.setStocks(tier.getStocks());
            player.sendMessage(RICHARD + "The <d>Stonks Auction <f>for <a>" + entry.auction().label() + " <f>has ended!");
            player.sendMessage(RICHARD + (placement > 0 ? "You placed <e>#" + StringUtility.commaify(placement) + "<f>, " : "You ")
                    + "earned <5>Stock of Stonks <8>x" + tier.getStocks() + "<f>. Come claim them from me in the <b>Trade Center<f>!");
        }
        save(player, profile);
    }

    public static int claim(SkyBlockPlayer player) {
        StonksAuctionProfile profile = profile(player);
        int claimed = 0;
        for (Entry entry : profile.claimable()) {
            claimed += entry.getStocks();
            entry.setClaimed(true);
        }
        if (claimed == 0) return 0;
        player.addAndUpdateItem(ItemType.STOCK_OF_STONKS, claimed);
        profile.trimHistory();
        save(player, profile);
        ServerType type = HypixelConst.getTypeLoader().getType();
        Thread.startVirtualThread(() -> PlayerDataService.saveAll(type, player));
        player.sendMessage("<a>You claimed <5>Stock of Stonks <8>x" + claimed + "<a>!");
        return claimed;
    }
}
