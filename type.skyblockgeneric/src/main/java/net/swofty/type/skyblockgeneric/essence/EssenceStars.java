package net.swofty.type.skyblockgeneric.essence;

/**
 * Star display after an upgraded item's name: gold for the first five, then each further star
 * recolours one of the five, pink for 6-10 and aqua for 11-15.
 */
public final class EssenceStars {
    private static final String STAR = "✪";

    private EssenceStars() {
    }

    public static String display(int stars) {
        if (stars <= 0) return "";
        if (stars <= 5) return "<6>" + STAR.repeat(stars);
        if (stars <= 10) return "<d>" + STAR.repeat(stars - 5) + "<6>" + STAR.repeat(10 - stars);
        int capped = Math.min(stars, 15);
        return "<b>" + STAR.repeat(capped - 10) + "<d>" + STAR.repeat(15 - capped);
    }
}
