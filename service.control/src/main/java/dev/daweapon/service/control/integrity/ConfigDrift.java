package dev.daweapon.service.control.integrity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compares a container's configuration tree with the host's, file by file.
 *
 * Game servers copy the host mount over their baked copy once, at boot. So a file that is
 * CHANGED or MISSING_IN_CONTAINER was edited on the host after the container last started, and
 * EXTRA_IN_CONTAINER is a file deleted on the host that the overlay never removes: only a
 * container recreate clears it (found 2026-10-07 when deleted pack textures kept being served).
 */
public final class ConfigDrift {

    public enum Kind {
        CHANGED,
        MISSING_IN_CONTAINER,
        EXTRA_IN_CONTAINER
    }

    public record FileDrift(String path, Kind kind) {
        /** Whether pushing the host copy fixes it; a host deletion cannot be synced. */
        public boolean syncable() {
            return kind != Kind.EXTRA_IN_CONTAINER;
        }
    }

    private static final Pattern SHA256SUM_LINE = Pattern.compile("^([0-9a-f]{64}) [ *](.+)$");

    private ConfigDrift() {
    }

    /**
     * Parses `sha256sum` output into path-relative-to-root to hash.
     *
     * Lines sha256sum escapes (a leading backslash, used for names containing a newline or
     * backslash) are skipped rather than guessed at; such names do not occur in this tree.
     */
    public static Map<String, String> parseSha256sum(String output, String root) {
        Map<String, String> hashes = new LinkedHashMap<>();
        if (output == null) {
            return hashes;
        }
        String prefix = root.endsWith("/") ? root : root + "/";
        for (String line : output.split("\n")) {
            String trimmed = line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
            Matcher matcher = SHA256SUM_LINE.matcher(trimmed);
            if (!matcher.matches()) {
                continue;
            }
            String path = matcher.group(2);
            if (path.startsWith(prefix)) {
                path = path.substring(prefix.length());
            } else if (path.startsWith("./")) {
                path = path.substring(2);
            }
            hashes.put(path, matcher.group(1));
        }
        return hashes;
    }

    public static List<FileDrift> compare(Map<String, String> host, Map<String, String> container,
                                          Predicate<String> inScope, Set<String> ignored) {
        Set<String> paths = new TreeSet<>();
        paths.addAll(host.keySet());
        paths.addAll(container.keySet());

        List<FileDrift> drift = new ArrayList<>();
        for (String path : paths) {
            if (!inScope.test(path) || ignored.contains(path)) {
                continue;
            }
            String hostHash = host.get(path);
            String containerHash = container.get(path);
            if (hostHash == null) {
                drift.add(new FileDrift(path, Kind.EXTRA_IN_CONTAINER));
            } else if (containerHash == null) {
                drift.add(new FileDrift(path, Kind.MISSING_IN_CONTAINER));
            } else if (!hostHash.equals(containerHash)) {
                drift.add(new FileDrift(path, Kind.CHANGED));
            }
        }
        drift.sort(Comparator.comparing(FileDrift::kind).thenComparing(FileDrift::path));
        return drift;
    }

    public static Predicate<String> scopeFor(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return path -> true;
        }
        String normalised = prefix.endsWith("/") ? prefix : prefix + "/";
        return path -> path.startsWith(normalised);
    }
}
