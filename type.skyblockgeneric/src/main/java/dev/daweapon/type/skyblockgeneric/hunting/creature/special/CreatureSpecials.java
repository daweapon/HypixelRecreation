package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CreatureSpecials {
    private static final Map<String, CreatureSpecial> REGISTRY = new HashMap<>();

    static {
        SpecialRegistrations.registerAll();
    }

    private CreatureSpecials() {
    }

    public static void register(String id, CreatureSpecial special) {
        REGISTRY.put(id, special);
    }

    public static Optional<CreatureSpecial> of(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(REGISTRY.get(id));
    }

    public static boolean registered(String id) {
        return REGISTRY.containsKey(id);
    }
}
