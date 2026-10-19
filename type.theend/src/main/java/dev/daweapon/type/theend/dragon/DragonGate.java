package dev.daweapon.type.theend.dragon;

import java.util.ArrayList;
import java.util.List;

public final class DragonGate {
    private static final double[] THRESHOLDS = {300_000, 550_000, 800_000, 1_000_000};
    private static final String[] MESSAGES = {
            "The Dragon's Gate is weakening!",
            "The Dragon's Gate is starting to budge!",
            "The Dragon's Gate is nearly open!",
            "The Dragon's Gate has opened!"};

    private DragonGate() {
    }

    public static String[] messagesFor(int alreadyAnnounced, double totalDamage) {
        List<String> due = new ArrayList<>();
        for (int stage = alreadyAnnounced; stage < THRESHOLDS.length; stage++) {
            if (totalDamage < THRESHOLDS[stage]) break;
            due.add(MESSAGES[stage]);
        }
        return due.toArray(String[]::new);
    }
}
