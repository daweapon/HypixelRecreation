package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class AbiphoneCallActions {
    private static final Map<String, Consumer<HypixelPlayer>> ACTIONS = new ConcurrentHashMap<>();

    private AbiphoneCallActions() {
    }

    public static void register(String contactId, Consumer<HypixelPlayer> action) {
        ACTIONS.put(contactId, action);
    }

    public static boolean has(String contactId) {
        return ACTIONS.containsKey(contactId);
    }

    public static boolean run(String contactId, HypixelPlayer player) {
        Consumer<HypixelPlayer> action = ACTIONS.get(contactId);
        if (action == null) return false;
        action.accept(player);
        return true;
    }
}
