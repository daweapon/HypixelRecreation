package dev.daweapon.type.skyblockgeneric.drill;

import org.jetbrains.annotations.Nullable;

public final class DrillAnvilRouting {
    public enum Target {
        DRILL,
        INPUT,
        NONE
    }

    private DrillAnvilRouting() {
    }

    public static boolean isInput(@Nullable String itemId) {
        return DrillParts.get(itemId) != null || MachineFuel.isFuel(itemId);
    }

    public static Target route(boolean drill, @Nullable String itemId) {
        if (drill) return Target.DRILL;
        return isInput(itemId) ? Target.INPUT : Target.NONE;
    }

    public static int mergeable(int existing, int incoming, int maxStack) {
        return Math.max(0, Math.min(incoming, maxStack - existing));
    }
}
