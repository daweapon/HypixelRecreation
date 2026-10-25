package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class PrimalFearChat {
    private static final Map<UUID, Map<Object, Predicate<String>>> LISTENERS = new ConcurrentHashMap<>();

    private PrimalFearChat() {
    }

    public static void listen(SkyBlockPlayer player, Object owner, Predicate<String> listener) {
        LISTENERS.computeIfAbsent(player.getUuid(), _ -> new ConcurrentHashMap<>()).put(owner, listener);
    }

    public static void stop(UUID player, Object owner) {
        Map<Object, Predicate<String>> listeners = LISTENERS.get(player);
        if (listeners == null) return;
        listeners.remove(owner);
        if (listeners.isEmpty()) LISTENERS.remove(player);
    }

    public static void observe(SkyBlockPlayer player, String message) {
        if (!LISTENERS.containsKey(player.getUuid())) return;
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            Map<Object, Predicate<String>> listeners = LISTENERS.get(player.getUuid());
            if (listeners == null) return;
            for (Map.Entry<Object, Predicate<String>> entry : Map.copyOf(listeners).entrySet()) {
                if (entry.getValue().test(message.trim())) stop(player.getUuid(), entry.getKey());
            }
        });
    }
}
