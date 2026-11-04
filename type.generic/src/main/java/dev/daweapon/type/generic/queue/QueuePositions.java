package dev.daweapon.type.generic.queue;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class QueuePositions {
    private static final long FORGET_AFTER_MILLIS = 120_000;
    private static final Map<UUID, Position> POSITIONS = new ConcurrentHashMap<>();

    private QueuePositions() {
    }

    public record Position(String activity, int position, int length, long at) {
    }

    public static String chatLine(String activity, int position) {
        return "<e>You are <a>#" + position + " <e>in the queue for <c>" + activity + "<e>.";
    }

    public static String sidebarLine(int position) {
        return "<f>Position: <a>#" + position;
    }

    public static boolean update(UUID player, String activity, int position, int length) {
        Position previous = POSITIONS.put(player, new Position(activity, position, length, System.currentTimeMillis()));
        return previous == null || previous.position() != position || !previous.activity().equals(activity);
    }

    public static @Nullable Position of(UUID player) {
        Position position = POSITIONS.get(player);
        if (position == null) return null;
        if (System.currentTimeMillis() - position.at() > FORGET_AFTER_MILLIS) {
            POSITIONS.remove(player);
            return null;
        }
        return position;
    }

    public static void clear(UUID player) {
        POSITIONS.remove(player);
    }
}
