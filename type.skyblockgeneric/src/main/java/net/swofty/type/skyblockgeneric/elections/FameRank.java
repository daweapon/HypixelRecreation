package net.swofty.type.skyblockgeneric.elections;

import lombok.Getter;

// Fame Rank tiers and their Mayor Election vote grants, per the SkyBlock Wiki's Fame Rank table.
// Fame itself is account-wide (earned 1:1 per Bit spent at Elizabeth), matching real Hypixel.
@Getter
public enum FameRank {
    NEW_PLAYER("New Player", 0, 1, 1.0),
    SETTLER("Settler", 20_000, 1, 1.1),
    CITIZEN("Citizen", 80_000, 2, 1.2),
    CONTRIBUTOR("Contributor", 200_000, 3, 1.3),
    PHILANTHROPIST("Philanthropist", 400_000, 5, 1.4),
    PATRON("Patron", 800_000, 10, 1.6),
    FAMOUS_PLAYER("Famous Player", 1_500_000, 20, 1.8),
    ATTACHE("Attaché", 3_000_000, 25, 1.9),
    AMBASSADOR("Ambassador", 10_000_000, 50, 2.0),
    STATESPERSON("Statesperson", 20_000_000, 75, 2.04),
    SENATOR("Senator", 33_000_000, 100, 2.08),
    DIGNITARY("Dignitary", 50_000_000, 100, 2.12),
    COUNCILOR("Councilor", 72_000_000, 100, 2.16),
    MINISTER("Minister", 100_000_000, 100, 2.2),
    PREMIER("Premier", 135_000_000, 100, 2.22),
    CHANCELLOR("Chancellor", 178_000_000, 100, 2.24),
    SUPREME("Supreme", 230_000_000, 100, 2.26),
    OVERSEER("Overseer", 250_000_000, 100, 2.28),
    REGENT("Regent", 290_000_000, 100, 2.3),
    VICEROY("Viceroy", 330_000_000, 100, 2.32),
    SOVEREIGN("Sovereign", 370_000_000, 100, 2.34),
    ARCHON("Archon", 410_000_000, 100, 2.36),
    IMPERATOR("Imperator", 450_000_000, 100, 2.38),
    PARAGON("Paragon", 500_000_000, 100, 2.4);

    private final String displayName;
    private final long fameRequired;
    private final int votes;
    private final double bitsMultiplier;

    FameRank(String displayName, long fameRequired, int votes, double bitsMultiplier) {
        this.displayName = displayName;
        this.fameRequired = fameRequired;
        this.votes = votes;
        this.bitsMultiplier = bitsMultiplier;
    }

    public FameRank next() {
        int ordinal = ordinal();
        FameRank[] all = values();
        return ordinal + 1 < all.length ? all[ordinal + 1] : null;
    }

    public static FameRank forFame(long fame) {
        FameRank current = NEW_PLAYER;
        for (FameRank rank : values()) {
            if (fame >= rank.fameRequired) current = rank;
        }
        return current;
    }
}
