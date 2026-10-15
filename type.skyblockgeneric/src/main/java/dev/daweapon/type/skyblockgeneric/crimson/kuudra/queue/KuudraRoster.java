package dev.daweapon.type.skyblockgeneric.crimson.kuudra.queue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * The party a Kuudra run is created for, carried in the orchestrator request's map field so the run knows
 * who to expect before anyone arrives.
 */
public final class KuudraRoster {
    private static final String PREFIX = "roster:";

    private KuudraRoster() {
    }

    public static String encode(List<UUID> members) {
        if (members.isEmpty()) throw new IllegalArgumentException("A Kuudra roster needs at least one player");
        return PREFIX + String.join(",", members.stream().map(UUID::toString).toList());
    }

    public static List<UUID> decode(String encoded) {
        if (encoded == null || !encoded.startsWith(PREFIX)) return List.of();
        String body = encoded.substring(PREFIX.length());
        if (body.isBlank()) return List.of();
        return Arrays.stream(body.split(",")).map(String::trim).map(UUID::fromString).distinct().toList();
    }
}
