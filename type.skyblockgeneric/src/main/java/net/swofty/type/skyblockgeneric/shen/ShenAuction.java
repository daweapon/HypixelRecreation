package net.swofty.type.skyblockgeneric.shen;

import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Shen's auctions for one SkyBlock year. A new round starts every year and ends when the year does,
 * so every server agrees on it from the calendar alone. Shen's Special picks two items (and, under
 * Volume Trading, two more) from the year number, so every server shows the same ones.
 */
public record ShenAuction(int year) {
    public static final long AUCTION_MINIMUM_BID = 1_000_000L;
    public static final long SPECIAL_MINIMUM_BID = 2_000_000L;
    private static final int SPECIALS_PER_YEAR = 2;
    private static final long MILLIS_PER_CALENDAR_UNIT = 50L;

    public static ShenAuction current() {
        return new ShenAuction(SkyBlockCalendar.getYear());
    }

    public List<ShenAuctionItem> specials() {
        return pick(ShenAuctionItem.of(ShenAuctionItem.Section.SPECIAL), year);
    }

    public List<ShenAuctionItem> volumeTradingSpecials() {
        return pick(ShenAuctionItem.of(ShenAuctionItem.Section.VOLUME_TRADING), year * 31L + 7);
    }

    public boolean offers(ShenAuctionItem item, boolean volumeTrading) {
        return switch (item.getSection()) {
            case AUCTION -> true;
            case SPECIAL -> specials().contains(item);
            case VOLUME_TRADING -> volumeTrading && volumeTradingSpecials().contains(item);
        };
    }

    public static long minimumBid(ShenAuctionItem item) {
        return item.getSection() == ShenAuctionItem.Section.AUCTION ? AUCTION_MINIMUM_BID : SPECIAL_MINIMUM_BID;
    }

    public String leaderboardKey(ShenAuctionItem item, boolean ironman) {
        return "shen_auction:" + year + ":" + item.name() + (ironman ? ":ironman" : ":classic");
    }

    public String volumeTradingKey() {
        return "shen_auction:" + year + ":volume_trading";
    }

    public long endElapsed() {
        return (long) year * SkyBlockCalendar.YEAR;
    }

    public boolean hasEnded() {
        return SkyBlockCalendar.getElapsed() >= endElapsed();
    }

    public long millisRemaining() {
        return Math.max(0, endElapsed() - SkyBlockCalendar.getElapsed()) * MILLIS_PER_CALENDAR_UNIT;
    }

    private static List<ShenAuctionItem> pick(List<ShenAuctionItem> pool, long seed) {
        List<ShenAuctionItem> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, new Random(seed));
        return List.copyOf(shuffled.subList(0, Math.min(SPECIALS_PER_YEAR, shuffled.size())));
    }
}
