package dev.daweapon.dungeons.catacombs.classes;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/** The classes a party is running with; a class only one member plays gets double strength. */
public record DungeonClassParty(Map<UUID, DungeonClassType> members) {
    public static final int LONE_CLASS_MULTIPLIER = 2;

    public DungeonClassParty {
        members = Map.copyOf(members);
    }

    public DungeonClassType classOf(UUID member) {
        return members.get(member);
    }

    public int count(DungeonClassType type) {
        return (int) members.values().stream().filter(type::equals).count();
    }

    public boolean isLone(DungeonClassType type) {
        return count(type) == 1;
    }

    public int strength(UUID member) {
        DungeonClassType type = members.get(member);
        return type != null && isLone(type) ? LONE_CLASS_MULTIPLIER : 1;
    }

    public Collection<DungeonClassType> teammatesOf(UUID member) {
        return members.entrySet().stream().filter(entry -> !entry.getKey().equals(member)).map(Map.Entry::getValue).toList();
    }

    public Map<DungeonClassType, Integer> counts() {
        Map<DungeonClassType, Integer> counts = new EnumMap<>(DungeonClassType.class);
        members.values().forEach(type -> counts.merge(type, 1, Integer::sum));
        return counts;
    }
}
