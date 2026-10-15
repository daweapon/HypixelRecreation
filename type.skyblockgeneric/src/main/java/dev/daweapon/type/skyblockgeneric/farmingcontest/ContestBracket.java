package dev.daweapon.type.skyblockgeneric.farmingcontest;

import lombok.Getter;

/**
 * Contest placements and their rewards, from the wiki's Jacob's Farming Contest table. The two
 * percentages are the share of participants each bracket covers without and with Finnegan's
 * GOATed perk.
 */
@Getter
public enum ContestBracket {
    PARTICIPATION("<7>", "PARTICIPATION", 0, 0, 0, 0, 0, 1, 0, 0),
    BRONZE("<c>", "BRONZE", 60, 70, 1, 0, 0, 10, 1, 40),
    SILVER("<f>", "SILVER", 30, 40, 0, 1, 0, 15, 1, 50),
    GOLD("<6>", "GOLD", 10, 20, 0, 0, 1, 25, 2, 60),
    PLATINUM("<3>", "PLATINUM", 5, 10, 1, 0, 1, 30, 2, 70),
    DIAMOND("<b>", "DIAMOND", 2, 5, 0, 1, 1, 35, 3, 80);

    public static final int MINIMUM_COLLECTED = 100;

    private final String color;
    private final String displayName;
    private final int topPercent;
    private final int goatedTopPercent;
    private final int bronzeMedals;
    private final int silverMedals;
    private final int goldMedals;
    private final int jacobsTickets;
    private final int carnivalTickets;
    private final int bits;

    ContestBracket(String color, String displayName, int topPercent, int goatedTopPercent,
                   int bronzeMedals, int silverMedals, int goldMedals,
                   int jacobsTickets, int carnivalTickets, int bits) {
        this.color = color;
        this.displayName = displayName;
        this.topPercent = topPercent;
        this.goatedTopPercent = goatedTopPercent;
        this.bronzeMedals = bronzeMedals;
        this.silverMedals = silverMedals;
        this.goldMedals = goldMedals;
        this.jacobsTickets = jacobsTickets;
        this.carnivalTickets = carnivalTickets;
        this.bits = bits;
    }

    public String formatted() {
        return color + "<l>" + displayName;
    }

    public boolean earnsMedal() {
        return this != PARTICIPATION;
    }

    public boolean atLeast(ContestBracket other) {
        return ordinal() >= other.ordinal();
    }

    /**
     * Each bracket covers its share of participants plus one more player, as the wiki's trivia
     * notes, so a lone participant always lands in Diamond.
     */
    public static ContestBracket forPlacement(int rank, long participants, boolean goated) {
        if (rank <= 0 || participants <= 0) return PARTICIPATION;
        ContestBracket[] brackets = values();
        for (int i = brackets.length - 1; i > 0; i--) {
            ContestBracket bracket = brackets[i];
            int percent = goated ? bracket.goatedTopPercent : bracket.topPercent;
            long cutoff = (long) Math.floor(participants * percent / 100D) + 1;
            if (rank <= cutoff) return bracket;
        }
        return PARTICIPATION;
    }
}
