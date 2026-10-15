package dev.daweapon.anticheat.event;

import org.tinylog.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Lightweight reflection-based event bus that dispatches events to registered @ListenerMethod handlers.
public class AnticheatEventHandler {
    private static final HashMap<Class<?>, List<EventMethodEntry>> cachedEvents = new HashMap<>();

    private record EventMethodEntry(Method method, AntiCheatListener instance, ListenerMethod anticheatEvent) { }

    public static void registerEventMethods(AntiCheatListener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ListenerMethod.class)) {
                ListenerMethod anticheatEvent = method.getAnnotation(ListenerMethod.class);

                if (!cachedEvents.containsKey(method.getParameterTypes()[0])) {
                    cachedEvents.put(method.getParameterTypes()[0], new ArrayList<>());
                }
                cachedEvents.get(method.getParameterTypes()[0]).add(new EventMethodEntry(method, listener, anticheatEvent));
            }
        }
    }

    public static void callEvent(Object event) {
        if (!cachedEvents.containsKey(event.getClass())) return;

        for (EventMethodEntry entry : cachedEvents.get(event.getClass())) {
            try {
                entry.method.invoke(entry.instance, event);
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                Logger.error(cause, "Failed to invoke event listener method {} for event {}",
                        entry.method.getName(), event.getClass().getSimpleName());
            }
        }
    }
}
