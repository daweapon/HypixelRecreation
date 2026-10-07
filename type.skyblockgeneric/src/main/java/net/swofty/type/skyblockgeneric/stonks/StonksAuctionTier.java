package net.swofty.type.skyblockgeneric.stonks;

import lombok.Getter;

@Getter
public enum StonksAuctionTier {
    TOP_100("TOP 100", 100, 25),
    TOP_500("TOP 500", 500, 15),
    TOP_1000("TOP 1,000", 1_000, 10),
    TOP_2500("TOP 2,500", 2_500, 5),
    TOP_5000("TOP 5,000", 5_000, 2),
    EVERYONE_ELSE("EVERYONE ELSE", Integer.MAX_VALUE, 1);

    private final String label;
    private final int lowestPlacement;
    private final int stocks;

    StonksAuctionTier(String label, int lowestPlacement, int stocks) {
        this.label = label;
        this.lowestPlacement = lowestPlacement;
        this.stocks = stocks;
    }

    public boolean isOpenEnded() {
        return this == EVERYONE_ELSE;
    }

    public static StonksAuctionTier forPlacement(int placement) {
        if (placement < 1) return EVERYONE_ELSE;
        for (StonksAuctionTier tier : values()) {
            if (placement <= tier.lowestPlacement) return tier;
        }
        return EVERYONE_ELSE;
    }
}
