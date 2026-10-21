package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import net.minestom.server.entity.Entity;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

final class PerMob<T> {
    private final Map<Entity, T> states = Collections.synchronizedMap(new WeakHashMap<>());

    T of(Entity mob, Supplier<T> initial) {
        return states.computeIfAbsent(mob, ignored -> initial.get());
    }

    void clear(Entity mob) {
        states.remove(mob);
    }
}
