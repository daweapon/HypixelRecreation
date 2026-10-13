package net.swofty.type.skyblockgeneric.garden.farm;

import net.minestom.server.instance.Instance;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GardenPlotLock {
    private static final Map<UUID, Set<Integer>> LOCKED = new ConcurrentHashMap<>();

    private GardenPlotLock() {
    }

    public static boolean tryLock(Instance instance, int plot) {
        return LOCKED.computeIfAbsent(instance.getUuid(), _ -> ConcurrentHashMap.newKeySet()).add(plot);
    }

    public static void unlock(Instance instance, int plot) {
        Set<Integer> plots = LOCKED.get(instance.getUuid());
        if (plots == null) return;
        plots.remove(plot);
        if (plots.isEmpty()) LOCKED.remove(instance.getUuid(), plots);
    }

    public static boolean isLocked(Instance instance, int plot) {
        Set<Integer> plots = LOCKED.get(instance.getUuid());
        return plots != null && plots.contains(plot);
    }

    public static boolean isLocked(Instance instance, double x, double z) {
        if (LOCKED.isEmpty()) return false;
        int plot = GardenLayout.plotAt(x, z);
        return plot != GardenLayout.NONE && isLocked(instance, plot);
    }
}
