package net.swofty.type.skyblockgeneric.experiments;

import java.util.List;

public record SuperpairsResult(long stakeXp, long pairXp, int pairsFound, int pairsTotal, List<PairPrize> prizes) {
    public long totalXp() {
        return stakeXp + pairXp;
    }
}
