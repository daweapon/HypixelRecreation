package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.levels.sources;

import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Mirrors the "Recently Viewed" shortcut on Hypixel's Ways to Level Up menu. Held in memory
// only, so the list starts empty again after a server restart.
public final class RecentlyViewedXPTasks {
    private static final int LIMIT = 7;
    private static final Map<UUID, Deque<SkyBlockXPSource>> VIEWED = new ConcurrentHashMap<>();

    private RecentlyViewedXPTasks() {
    }

    public static void record(UUID player, SkyBlockXPSource source) {
        Deque<SkyBlockXPSource> viewed = VIEWED.computeIfAbsent(player, _ -> new ArrayDeque<>());
        synchronized (viewed) {
            viewed.remove(source);
            viewed.addFirst(source);
            while (viewed.size() > LIMIT) viewed.removeLast();
        }
    }

    public static List<SkyBlockXPSource> get(UUID player) {
        Deque<SkyBlockXPSource> viewed = VIEWED.get(player);
        if (viewed == null) return List.of();
        synchronized (viewed) {
            return List.copyOf(viewed);
        }
    }

    public static void clear(UUID player) {
        VIEWED.remove(player);
    }
}
