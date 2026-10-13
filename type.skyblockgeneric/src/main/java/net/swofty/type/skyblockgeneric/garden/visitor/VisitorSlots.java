package net.swofty.type.skyblockgeneric.garden.visitor;

import net.minestom.server.coordinate.Pos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VisitorSlots {
    public static final Pos[] POSITIONS = {
            new Pos(5.5, 72, -18.5, 0, 0),
            new Pos(5.5, 72, -15.5, 0, 0),
            new Pos(5.5, 72, -12.5, 0, 0),
            new Pos(6.5, 71, -8.5, 0, 0),
            new Pos(9.5, 71, -8.5, 0, 0)
    };
    public static final Pos ENTRANCE = new Pos(12.5, 71, -8.5, 90, 0);
    public static final long ARRIVAL_WINDOW_MILLIS = 20_000;
    private static final Map<UUID, Map<String, Integer>> SLOTS = new ConcurrentHashMap<>();

    private VisitorSlots() {
    }

    public static void refresh(UUID player, VisitorData data) {
        Map<String, Integer> slots = new HashMap<>();
        int slot = 0;
        for (VisitorData.Present present : data.visible()) {
            slots.put(present.getId(), slot++);
        }
        SLOTS.put(player, slots);
    }

    public static void clear(UUID player) {
        SLOTS.remove(player);
    }

    public static int slotOf(UUID player, String visitorId) {
        Map<String, Integer> slots = SLOTS.get(player);
        Integer slot = slots == null ? null : slots.get(visitorId);
        return slot == null ? -1 : slot;
    }
}
