package dev.daweapon.type.skyblockgeneric.minion;

import java.util.List;

public final class MinionOfflineHooks {
    @FunctionalInterface
    public interface Extra {
        void credit(IslandMinionData.IslandMinion minion, long elapsedMillis);
    }

    private static final List<Extra> EXTRAS = List.of(MinionTimedProduction::creditOffline);

    private MinionOfflineHooks() {
    }

    public static void credit(IslandMinionData.IslandMinion minion, long elapsedMillis) {
        if (elapsedMillis <= 0) return;
        EXTRAS.forEach(extra -> extra.credit(minion, elapsedMillis));
    }
}
