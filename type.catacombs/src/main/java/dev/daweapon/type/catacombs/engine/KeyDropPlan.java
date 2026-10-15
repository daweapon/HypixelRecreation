package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Which key a critical-path room gives when it is cleared: the room whose outgoing door is a Wither door gives a
 * Wither Key and the room before the Blood Room gives the Blood Key.
 */
public final class KeyDropPlan {
    private final Map<Integer, KeyType> keyByRoom;

    private KeyDropPlan(Map<Integer, KeyType> keyByRoom) {
        this.keyByRoom = Map.copyOf(keyByRoom);
    }

    public static KeyDropPlan of(FloorLayout layout) {
        Map<Integer, KeyType> keys = new HashMap<>();
        List<Integer> path = layout.criticalPath();
        for (int step = 0; step < path.size() - 1; step++) {
            int from = path.get(step);
            int to = path.get(step + 1);
            for (LayoutDoor door : layout.doors()) {
                if (!door.connects(from) || !door.connects(to)) continue;
                if (door.witherDoor()) keys.put(from, KeyType.WITHER);
                if (door.bloodDoor()) keys.put(from, KeyType.BLOOD);
            }
        }
        return new KeyDropPlan(keys);
    }

    public Optional<KeyType> keyFor(int room) {
        return Optional.ofNullable(keyByRoom.get(room));
    }

    public Map<Integer, KeyType> all() {
        return keyByRoom;
    }
}
