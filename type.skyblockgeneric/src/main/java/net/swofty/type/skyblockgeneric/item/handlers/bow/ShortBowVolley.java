package net.swofty.type.skyblockgeneric.item.handlers.bow;

import java.util.ArrayList;
import java.util.List;

public final class ShortBowVolley {
    public static final double HYDRA_ARROW_SPREAD = 2.5;
    private static final double TABLE_COOLDOWN_SECONDS = 0.5;

    private ShortBowVolley() {
    }

    /** Yaw offsets in degrees: the first arrow flies at the crosshair, the rest alternate either side of it. */
    public static List<Double> yawOffsets(int arrows, double spread) {
        List<Double> offsets = new ArrayList<>(arrows);
        for (int i = 0; i < arrows; i++) {
            int ring = (i + 1) / 2;
            offsets.add(i == 0 ? 0 : (i % 2 == 1 ? -1 : 1) * ring * spread);
        }
        return offsets;
    }

    public static List<Double> hydraOffsets(int extraArrows) {
        List<Double> offsets = new ArrayList<>(extraArrows);
        for (int i = 0; i < extraArrows; i++) {
            int ring = i / 2 + 1;
            offsets.add((i % 2 == 0 ? -1 : 1) * ring * HYDRA_ARROW_SPREAD);
        }
        return offsets;
    }

    /**
     * Ticks between shortbow shots. A 0.5s shortbow follows the wiki's Terminator fire-rate table (9/7/5/4 ticks at
     * 0-11/12-40/41-66/67-100 Bonus Attack Speed); other cooldowns scale that table proportionally.
     */
    public static int shotCooldownTicks(double cooldownSeconds, double bonusAttackSpeed) {
        double speed = Math.min(100, bonusAttackSpeed);
        int ticks;
        if (speed <= 11) ticks = 9;
        else if (speed <= 40) ticks = 7;
        else if (speed <= 66) ticks = 5;
        else ticks = 4;
        return Math.max(1, (int) Math.round(ticks * cooldownSeconds / TABLE_COOLDOWN_SECONDS));
    }
}
