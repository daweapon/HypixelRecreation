package dev.daweapon.service.control.integrity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resource-pack health (capability 11), decided from what the game servers advertise and what
 * the pack server actually serves.
 *
 * STALE is the failure that has kicked every player before: the pack server is up but does not
 * serve the hash the game servers tell clients to download, or the game servers disagree with
 * each other because one was started later against a changed pack.
 */
public final class PackVerdict {

    public enum State {
        LIVE,
        DEAD,
        STALE
    }

    public record Result(State state, String headline, List<String> problems) {
    }

    private static final Pattern GAME_SERVER_HASH =
            Pattern.compile("Resource pack '([^']+)' built\\. Hash: ([0-9a-f]{40})");
    private static final Pattern PACK_SERVER_URL = Pattern.compile("Pack URL: \\S*/([0-9a-f]{40})\\.zip");

    private PackVerdict() {
    }

    /** The last hash a game server logged for the named pack, or null. */
    public static String advertisedHash(String log, String packName) {
        String found = null;
        Matcher matcher = GAME_SERVER_HASH.matcher(log == null ? "" : log);
        while (matcher.find()) {
            if (matcher.group(1).equals(packName)) {
                found = matcher.group(2);
            }
        }
        return found;
    }

    public static Set<String> servedHashes(String log) {
        Set<String> hashes = new LinkedHashSet<>();
        Matcher matcher = PACK_SERVER_URL.matcher(log == null ? "" : log);
        while (matcher.find()) {
            hashes.add(matcher.group(1));
        }
        return hashes;
    }

    /**
     * @param advertised  game server name to the hash it advertises (only those that logged one)
     * @param probes      advertised hash to the pack server's answer for {@code <hash>.zip}
     * @param verified    advertised hash to whether the downloaded bytes hash to it; may be empty
     */
    public static Result evaluate(boolean packServerRunning, boolean packServerAnswering,
                                  Map<String, String> advertised, Map<String, HttpProbe.Outcome> probes,
                                  Map<String, Boolean> verified) {
        if (!packServerRunning) {
            return new Result(State.DEAD, "Pack server container is not running",
                    List.of("Every player is kicked with \"Required resource pack was not loaded\"."));
        }
        if (!packServerAnswering) {
            return new Result(State.DEAD, "Pack server is not answering HTTP",
                    List.of("The container is running but nothing answered on the pack port."));
        }

        List<String> problems = new ArrayList<>();
        Set<String> distinct = new TreeSet<>(advertised.values());
        if (distinct.size() > 1) {
            problems.add("Game servers advertise " + distinct.size() + " different pack hashes: "
                    + describeSplit(advertised) + ". One was started against a changed pack.");
        }
        for (String hash : distinct) {
            HttpProbe.Outcome probe = probes.get(hash);
            if (probe == null || !probe.ok()) {
                problems.add("Pack server does not serve " + shortHash(hash) + ".zip"
                        + (probe == null ? "" : " (HTTP " + probe.status() + ")")
                        + "; restart hypixel_pack_server so it rebuilds from the current files.");
            }
            Boolean match = verified.get(hash);
            if (Boolean.FALSE.equals(match)) {
                problems.add("Downloaded " + shortHash(hash) + ".zip does not hash to its name.");
            }
        }

        if (!problems.isEmpty()) {
            return new Result(State.STALE, "Pack server serves a different pack than players are sent", problems);
        }
        if (distinct.isEmpty()) {
            return new Result(State.LIVE, "Pack server answering; no running game server logged a pack hash",
                    List.of());
        }
        return new Result(State.LIVE, "Serving " + shortHash(distinct.iterator().next())
                + " to " + advertised.size() + " game server(s)", List.of());
    }

    private static String describeSplit(Map<String, String> advertised) {
        List<String> parts = new ArrayList<>();
        advertised.forEach((server, hash) -> parts.add(server + "=" + shortHash(hash)));
        return String.join(", ", parts);
    }

    static String shortHash(String hash) {
        return hash.length() > 8 ? hash.substring(0, 8) : hash;
    }
}
