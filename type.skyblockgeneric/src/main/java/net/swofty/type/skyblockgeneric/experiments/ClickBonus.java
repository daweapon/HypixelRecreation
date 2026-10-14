package net.swofty.type.skyblockgeneric.experiments;

public record ClickBonus(int... thresholds) {
    public int clicksFor(int chain, int serumsDrunk) {
        int bonus = 0;
        for (int threshold : thresholds) {
            if (chain >= threshold - serumsDrunk) bonus++;
        }
        return bonus;
    }
}
