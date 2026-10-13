package net.swofty.type.skyblockgeneric.garden.visitor;

import net.minestom.server.coordinate.Pos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VisitorSlots {
    public static final Pos[] POSITIONS = {
            new Pos(-14.5, 71, -27.5, 20, 0),
            new Pos(-22.5, 71, -15.5, 0, 0),
            new Pos(-20.5, 71, -15.5, 0, 0),
            new Pos(-18.5, 71, -15.5, 0, 0),
            new Pos(1.5, 71, -29.5, 14, 0)
    };
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
