package net.swofty.commons.skyblock.auctions;

public final class AuctionRules {
    public static final long MAX_PRICE = 50_000_000_000L;
    public static final long MAX_DURATION = 336L * 3_600_000L;
    public static final long BIN_PURCHASE_GRACE = 20_000L;
    public static final long BROWSER_DELIST_LEAD = 285_000L;

    private AuctionRules() {}

    public static long minimumBid(long startingPrice, Long highestBid) {
        if (highestBid == null) return startingPrice;
        return Math.addExact(highestBid, Math.max(1, Math.floorDiv(highestBid, 40) + (highestBid % 40 == 0 ? 0 : 1)));
    }

    public static long durationFee(long duration) {
        if (duration <= 0 || duration > MAX_DURATION) throw new IllegalArgumentException("Invalid auction duration");
        long[] hours = {1, 6, 12, 24, 48, 336};
        long[] fees = {20, 45, 100, 350, 1_200, 55_200};
        double requested = duration / 3_600_000D;
        if (requested <= hours[0]) return fees[0];
        for (int i = 1; i < hours.length; i++) {
            if (requested <= hours[i]) {
                double ratio = (requested - hours[i - 1]) / (hours[i] - hours[i - 1]);
                return Math.round(fees[i - 1] + ratio * (fees[i] - fees[i - 1]));
            }
        }
        return fees[fees.length - 1];
    }

    public static long listingFee(long price, long duration, boolean bin) {
        if (price <= 0 || price > MAX_PRICE) throw new IllegalArgumentException("Invalid auction price");
        long rateBasisPoints = bin ? price < 10_000_000L ? 100 : price < 100_000_000L ? 200 : 250 : 500;
        return Math.addExact(percentage(price, rateBasisPoints), durationFee(duration));
    }

    public static long collectionTax(long salePrice) {
        return collectionTax(salePrice, 1);
    }

    public static long collectionTax(long salePrice, int multiplier) {
        if (salePrice <= 1_000_000L) return 0;
        return Math.min(percentage(salePrice, 100L * Math.max(1, multiplier)), salePrice - 1_000_000L);
    }

    private static long percentage(long value, long basisPoints) {
        return Math.floorDiv(Math.multiplyExact(value, basisPoints), 10_000L);
    }
}
