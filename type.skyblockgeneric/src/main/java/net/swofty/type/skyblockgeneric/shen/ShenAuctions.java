package net.swofty.type.skyblockgeneric.shen;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.bank.PersonalBankService;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointShenAuctions;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shen.ShenAuctionProfile.Entry;
import net.swofty.type.skyblockgeneric.shen.ShenAuctionProfile.Outcome;
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
 * Shen's Auction and Shen's Special. Bids are kept per profile and mirrored into a Redis sorted set
 * per item, year and game mode, so every server ranks against the same bidders. The top bidders win
 * the item when the year ends; everyone else gets their coins back. Volume Trading is remembered
 * per year in Redis, so a Diaz term that covers any part of the year doubles that year's winners.
 */
public final class ShenAuctions {
    public static final String DAMIA = "<e>[NPC] Damia<f>: ";
    public static final int AUCTION_MILESTONE = 10;
    public static final int SPECIAL_MILESTONE = 20;
    private static final long LEADERBOARD_TTL_SECONDS = Duration.ofDays(30).toSeconds();
    private static final UUID VOLUME_TRADING_MARKER = new UUID(0, 0);
    private static final Set<UUID> resolving = ConcurrentHashMap.newKeySet();

    private ShenAuctions() {}

    public static void start() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            rememberVolumeTrading();
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                if (player.getSkyblockDataHandler() != null) finaliseEnded(player);
            }
            return TaskSchedule.seconds(5);
        });
    }

    public static boolean isVolumeTrading() {
        return ElectionPerks.isActive(SkyBlockMayor.Perk.VOLUME_TRADING);
    }

    private static void rememberVolumeTrading() {
        if (!isVolumeTrading() || !LeaderboardService.isInitialized()) return;
        String key = ShenAuction.current().volumeTradingKey();
        Thread.startVirtualThread(() -> LeaderboardService.setExpiringScore(key, VOLUME_TRADING_MARKER, 1, LEADERBOARD_TTL_SECONDS));
    }

    /** Blocking Redis read; call it off the main thread. */
    public static boolean wasVolumeTrading(ShenAuction auction) {
        return LeaderboardService.getPlayerScore(auction.volumeTradingKey(), VOLUME_TRADING_MARKER) != null;
    }

    /**
     * Blocking Redis read; call it off the main thread. Once the winners' list is full a new bid
     * has to beat its lowest bid.
     */
    public static long minimumBid(ShenAuction auction, ShenAuctionItem item, boolean ironman, boolean volumeTrading) {
        long floor = ShenAuction.minimumBid(item);
        Double lowestWinning = LeaderboardService.scoreAtRank(auction.leaderboardKey(item, ironman), item.winners(ironman, volumeTrading));
        return lowestWinning == null ? floor : Math.max(floor, lowestWinning.longValue() + 1);
    }

    public static int museumMilestone(SkyBlockPlayer player) {
        return PersonalBankService.data(player).getMuseumMilestone();
    }

    public static int requiredMilestone(ShenAuctionItem item) {
        return item.getSection() == ShenAuctionItem.Section.AUCTION ? AUCTION_MILESTONE : SPECIAL_MILESTONE;
    }

    public static boolean canView(SkyBlockPlayer player, ShenAuctionItem item) {
        return museumMilestone(player) >= requiredMilestone(item);
    }

    public static ShenAuctionProfile profile(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    private static void save(SkyBlockPlayer player, ShenAuctionProfile profile) {
        datapoint(player).setValue(profile);
    }

    private static DatapointShenAuctions datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SHEN_AUCTIONS, DatapointShenAuctions.class);
    }

    public static UUID bidder(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().getCurrentProfileId();
    }

    public static long currentBid(SkyBlockPlayer player, ShenAuctionItem item) {
        return profile(player).entryFor(ShenAuction.current(), item).map(Entry::getBid).orElse(0L);
    }

    public static Optional<String> bid(SkyBlockPlayer player, ShenAuctionItem item, long newBid, long minimumBid) {
        ShenAuction auction = ShenAuction.current();
        if (!canView(player, item)) {
            return Optional.of("<c>You need Museum Milestone " + requiredMilestone(item) + " to bid on this!");
        }
        if (!item.isAvailable()) return Optional.of("<c>" + item.getUnavailableReason());
        if (!auction.offers(item, isVolumeTrading())) return Optional.of("<c>Shen isn't auctioning that this year!");
        if (!LeaderboardService.isInitialized()) return Optional.of("<c>Shen's Auction is unavailable right now, try again later!");

        boolean ironman = player.isIronman();
        ShenAuctionProfile profile = profile(player);
        Entry entry = profile.bidOn(auction, item, ironman);
        long previous = entry.getBid();
        long minimum = Math.max(ShenAuction.minimumBid(item), minimumBid);
        if (newBid < minimum) {
            return Optional.of("<c>The minimum bid is <6>" + StringUtility.commaify(minimum) + " coins<c>!");
        }
        if (newBid <= previous) return Optional.of("<c>Your new bid must be higher than your current bid!");
        long difference = newBid - previous;
        if (player.getCoins() < difference) return Optional.of("<c>You don't have enough coins to bid that much!");

        player.removeCoins(difference);
        entry.setBid(newBid);
        save(player, profile);
        player.sendMessage("<a>You bid <6>" + StringUtility.commaify(newBid) + " coins <a>on " + item.getDisplayName() + "<a>!");

        UUID bidder = bidder(player);
        String key = auction.leaderboardKey(item, ironman);
        Thread.startVirtualThread(() -> {
            if (LeaderboardService.setExpiringScore(key, bidder, newBid, LEADERBOARD_TTL_SECONDS)) return;
            MinecraftServer.getSchedulerManager().scheduleNextTick(() -> refundFailedBid(player, auction, item, previous, newBid));
        });
        return Optional.empty();
    }

    private static void refundFailedBid(SkyBlockPlayer player, ShenAuction auction, ShenAuctionItem item, long previous, long failedBid) {
        if (!player.isOnline() || player.getSkyblockDataHandler() == null) return;
        ShenAuctionProfile profile = profile(player);
        Entry entry = profile.entryFor(auction, item).orElse(null);
        if (entry == null || entry.getBid() != failedBid) return;
        entry.setBid(previous);
        if (previous == 0) profile.getEntries().remove(entry);
        save(player, profile);
        player.addCoins(failedBid - previous);
        player.sendMessage("<c>Your bid in Shen's Auction couldn't be placed, so your coins were refunded.");
    }

    public static void finaliseEnded(SkyBlockPlayer player) {
        List<Entry> pending = profile(player).getEntries().stream()
                .filter(entry -> entry.isPending() && entry.getBid() > 0 && entry.auction().hasEnded())
                .toList();
        if (pending.isEmpty() || !LeaderboardService.isInitialized()) return;
        UUID uuid = player.getUuid();
        if (!resolving.add(uuid)) return;
        UUID bidder = bidder(player);

        Thread.startVirtualThread(() -> {
            Map<Entry, Boolean> won = new HashMap<>();
            try {
                for (Entry entry : pending) {
                    ShenAuction auction = entry.auction();
                    int winners = entry.getItem().winners(entry.isIronman(), wasVolumeTrading(auction));
                    int rank = LeaderboardService.getPlayerRank(auction.leaderboardKey(entry.getItem(), entry.isIronman()), bidder);
                    won.put(entry, rank >= 1 && rank <= winners);
                }
            } finally {
                MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                    try {
                        if (player.isOnline() && player.getSkyblockDataHandler() != null) applyOutcomes(player, won);
                    } finally {
                        resolving.remove(uuid);
                    }
                });
            }
        });
    }

    private static void applyOutcomes(SkyBlockPlayer player, Map<Entry, Boolean> won) {
        ShenAuctionProfile profile = profile(player);
        won.forEach((resolved, didWin) -> profile.entryFor(resolved.auction(), resolved.getItem())
                .filter(Entry::isPending)
                .ifPresent(entry -> {
                    entry.setOutcome(didWin ? Outcome.WON : Outcome.LOST);
                    player.sendMessage(DAMIA + (didWin
                            ? "You won " + entry.getItem().getDisplayName() + " <f>in <6>Shen's Auction<f>! Come claim it from me!"
                            : "You didn't win " + entry.getItem().getDisplayName() + " <f>in <6>Shen's Auction<f>. Come claim your <6>"
                            + StringUtility.commaify(entry.getBid()) + " coins <f>back from me!"));
                }));
        save(player, profile);
    }

    public static int claimItems(SkyBlockPlayer player) {
        ShenAuctionProfile profile = profile(player);
        int claimed = 0;
        for (Entry entry : profile.claimable(Outcome.WON)) {
            if (entry.getItem().itemType() == null) continue;
            SkyBlockItem item = new SkyBlockItem(entry.getItem().itemType());
            if (entry.isIronman()) item.getAttributeHandler().setSoulBound(true);
            player.addAndUpdateItem(item);
            entry.setClaimed(true);
            claimed++;
            player.sendMessage("<a>You claimed " + entry.getItem().getDisplayName() + "<a>!");
        }
        finishClaim(player, profile, claimed > 0);
        return claimed;
    }

    public static long claimRefunds(SkyBlockPlayer player) {
        ShenAuctionProfile profile = profile(player);
        long refunded = 0;
        for (Entry entry : profile.claimable(Outcome.LOST)) {
            refunded += entry.getBid();
            entry.setClaimed(true);
        }
        if (refunded > 0) {
            player.addCoins(refunded);
            player.sendMessage("<a>You claimed <6>" + StringUtility.commaify(refunded) + " coins <a>from your lost bids!");
        }
        finishClaim(player, profile, refunded > 0);
        return refunded;
    }

    private static void finishClaim(SkyBlockPlayer player, ShenAuctionProfile profile, boolean changed) {
        if (!changed) return;
        profile.trimHistory();
        save(player, profile);
        ServerType type = HypixelConst.getTypeLoader().getType();
        Thread.startVirtualThread(() -> PlayerDataService.saveAll(type, player));
    }
}
