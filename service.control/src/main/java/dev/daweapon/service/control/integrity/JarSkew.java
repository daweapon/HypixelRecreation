package dev.daweapon.service.control.integrity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Decides, per container, whether the jar it is running is the jar it should be running.
 *
 * The jar store is the reference because every deploy persists there first. When the store has
 * no copy of a jar, the containers running it are compared with each other instead: a mixed
 * fleet is the version-skew bug (tracker #85) whichever side is newer.
 */
public final class JarSkew {

    public enum State {
        MATCH,
        SKEW,
        /** No stored jar and no majority among the containers to compare against. */
        NO_REFERENCE,
        /** Running, but the jar inside could not be hashed. */
        UNREADABLE,
        /** Not running, so nothing was hashed; it will load whatever is in it when started. */
        STOPPED
    }

    public record Fingerprint(String container, String jar, boolean running, String sha256, String error) {
    }

    public record Entry(String container, String jar, State state, String containerSha256,
                        String referenceSha256, String reason) {
    }

    private JarSkew() {
    }

    public static List<Entry> evaluate(List<Fingerprint> fingerprints, Map<String, String> storeHashes) {
        Map<String, List<Fingerprint>> byJar = new LinkedHashMap<>();
        for (Fingerprint fingerprint : fingerprints) {
            byJar.computeIfAbsent(fingerprint.jar(), ignored -> new ArrayList<>()).add(fingerprint);
        }

        List<Entry> entries = new ArrayList<>();
        for (Map.Entry<String, List<Fingerprint>> group : byJar.entrySet()) {
            String jar = group.getKey();
            String stored = storeHashes.get(jar);
            String reference = stored != null ? stored : majority(group.getValue());
            int peers = group.getValue().size() - 1;

            for (Fingerprint fingerprint : group.getValue()) {
                entries.add(judge(fingerprint, reference, stored != null, peers));
            }
        }
        return entries;
    }

    private static Entry judge(Fingerprint fingerprint, String reference, boolean fromStore, int peers) {
        if (!fingerprint.running()) {
            return entry(fingerprint, State.STOPPED, reference, "not running; not checked");
        }
        if (fingerprint.sha256() == null) {
            return entry(fingerprint, State.UNREADABLE, reference,
                    fingerprint.error() == null ? "could not hash the jar" : fingerprint.error());
        }
        if (reference == null) {
            return entry(fingerprint, State.NO_REFERENCE, null,
                    "no copy in the jar store and no majority among containers to compare with");
        }
        if (reference.equals(fingerprint.sha256())) {
            return entry(fingerprint, State.MATCH, reference,
                    fromStore ? "matches the jar store" : "matches the other containers running it");
        }
        return entry(fingerprint, State.SKEW, reference, fromStore
                ? "differs from the jar store copy of " + fingerprint.jar()
                : "differs from the " + peers + " other container(s) running " + fingerprint.jar());
    }

    private static Entry entry(Fingerprint fingerprint, State state, String reference, String reason) {
        return new Entry(fingerprint.container(), fingerprint.jar(), state, fingerprint.sha256(),
                reference, reason);
    }

    /** The hash held by a strict majority of readable containers, or null when there is none. */
    static String majority(List<Fingerprint> group) {
        Map<String, Integer> counts = new HashMap<>();
        int readable = 0;
        for (Fingerprint fingerprint : group) {
            if (fingerprint.running() && fingerprint.sha256() != null) {
                counts.merge(fingerprint.sha256(), 1, Integer::sum);
                readable++;
            }
        }
        if (readable < 2) {
            return null;
        }
        for (Map.Entry<String, Integer> count : counts.entrySet()) {
            if (count.getValue() * 2 > readable) {
                return count.getKey();
            }
        }
        return null;
    }

    public static List<String> skewedContainers(List<Entry> entries, String jar) {
        return entries.stream()
                .filter(entry -> entry.state() == State.SKEW && Objects.equals(entry.jar(), jar))
                .map(Entry::container)
                .toList();
    }
}
