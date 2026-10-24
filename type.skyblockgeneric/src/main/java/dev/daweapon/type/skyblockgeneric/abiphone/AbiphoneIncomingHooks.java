package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class AbiphoneIncomingHooks {
    public record Hook(Predicate<SkyBlockPlayer> wants, Consumer<SkyBlockPlayer> pickUp) {
    }

    private static final Map<String, Hook> HOOKS = new ConcurrentHashMap<>();

    private AbiphoneIncomingHooks() {
    }

    public static void register(String contactId, Hook hook) {
        HOOKS.put(contactId, hook);
    }

    public static Hook get(String contactId) {
        return HOOKS.get(contactId);
    }
}
