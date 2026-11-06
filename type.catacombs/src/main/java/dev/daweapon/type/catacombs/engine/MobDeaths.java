package dev.daweapon.type.catacombs.engine;

import net.minestom.server.instance.Instance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** Tells the run that owns a mob's instance that the mob died, for the end-of-run stats. */
public final class MobDeaths {
    private static final Map<Instance, Consumer<CatacombsMob>> LISTENERS = new ConcurrentHashMap<>();

    private MobDeaths() {
    }

    public static void listen(Instance instance, Consumer<CatacombsMob> listener) {
        LISTENERS.put(instance, listener);
    }

    public static void forget(Instance instance) {
        LISTENERS.remove(instance);
    }

    public static void record(CatacombsMob mob, Instance instance) {
        Consumer<CatacombsMob> listener = LISTENERS.get(instance);
        if (listener != null) listener.accept(mob);
    }
}
