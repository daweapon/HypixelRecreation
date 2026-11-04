package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class AbiphoneContactListeners {
    private static final Map<String, List<Consumer<SkyBlockPlayer>>> ADDED = new ConcurrentHashMap<>();
    private static final Map<String, List<Consumer<SkyBlockPlayer>>> REMOVED = new ConcurrentHashMap<>();

    private AbiphoneContactListeners() {
    }

    public static void onAdded(String contactId, Consumer<SkyBlockPlayer> listener) {
        ADDED.computeIfAbsent(contactId, _ -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public static void onRemoved(String contactId, Consumer<SkyBlockPlayer> listener) {
        REMOVED.computeIfAbsent(contactId, _ -> new CopyOnWriteArrayList<>()).add(listener);
    }

    static void added(SkyBlockPlayer player, String contactId) {
        ADDED.getOrDefault(contactId, List.of()).forEach(listener -> listener.accept(player));
    }

    static void removed(SkyBlockPlayer player, String contactId) {
        REMOVED.getOrDefault(contactId, List.of()).forEach(listener -> listener.accept(player));
    }
}
