package dev.daweapon.type.skyblockgeneric.crimson.volcano;

import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;

@Getter
public enum VolcanoExplosivity {
    CHILL("CHILL", "<b>", 27.12),
    LOW("LOW", "<a>", 23.75),
    DISRUPTIVE("DISRUPTIVE", "<e>", 20.34),
    MEDIUM("MEDIUM", "<6>", 16.95),
    HIGH("HIGH", "<c>", 6.78),
    EXPLOSIVE("EXPLOSIVE", "<4>", 3.39),
    CATACLYSMIC("CATACLYSMIC", "<4><l>", 1.69),
    ;

    private final String displayName;
    private final String colour;
    private final double chancePercent;

    VolcanoExplosivity(String displayName, String colour, double chancePercent) {
        this.displayName = displayName;
        this.colour = colour;
        this.chancePercent = chancePercent;
    }

    public String coloured() {
        return colour + displayName;
    }

    public static VolcanoExplosivity roll() {
        double total = 0;
        for (VolcanoExplosivity value : values()) total += value.chancePercent;
        double pick = ThreadLocalRandom.current().nextDouble(total);
        for (VolcanoExplosivity value : values()) {
            pick -= value.chancePercent;
            if (pick < 0) return value;
        }
        return CHILL;
    }
}
