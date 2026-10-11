package net.swofty.type.skyblockgeneric.glacite;

import java.util.List;

public final class ColdMath {
    public static final double MIN_COLD = -100;
    public static final double BASE_SECONDS_PER_POINT = 5;
    public static final double WATER_VULNERABILITY = 3;
    public static final double CAMPFIRE_COLD = 50;

    public static final List<Threshold> THRESHOLDS = List.of(
            new Threshold(-25, "<9><l>BRRR! <r><b>It's getting really cold in here! But you've got to keep moving..."),
            new Threshold(-50, "<9><l>BRRR! <r><b>It's so cold that you can barely feel your fingers. Moving is getting difficult..."),
            new Threshold(-75, "<9><l>BRRR! <r><b>Your movement slows to a crawl as the cold threatens to take over. Time to get out of here..."),
            new Threshold(-90, "<9><l>BRRR! <r><b>You're freezing! All you can think about is getting out of here to a warm campfire..."));

    private ColdMath() {}

    public static double secondsPerPoint(double resistance, double vulnerability) {
        return BASE_SECONDS_PER_POINT * (1 + resistance * 0.01) / (1 + vulnerability);
    }

    public static double miningSpeedMultiplier(double cold, double resistance) {
        double effective = Math.min(0, cold + Math.max(0, resistance));
        double penalty = -effective * 0.005;
        double cap = Math.floor(-cold / 25) * 0.25;
        return Math.max(0, 1 - Math.max(penalty, cap));
    }

    public static double clamp(double cold) {
        return Math.max(MIN_COLD, Math.min(0, cold));
    }

    public static boolean freezesToDeath(double cold) {
        return cold <= MIN_COLD;
    }

    public static List<Threshold> crossed(double before, double after) {
        return THRESHOLDS.stream().filter(t -> before > t.cold() && after <= t.cold()).toList();
    }

    public record Threshold(int cold, String message) {}
}
