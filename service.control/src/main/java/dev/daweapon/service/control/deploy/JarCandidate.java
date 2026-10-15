package dev.daweapon.service.control.deploy;

import java.time.Instant;
import java.util.List;

/** A jar sitting in the drop folder, ready to deploy. */
public record JarCandidate(
        String name,
        long sizeBytes,
        String sha256,
        Instant modifiedAt,
        List<String> defaultTargets,
        boolean bindMounted,
        /** True when the identical jar is already staged, so deploying would be a no-op. */
        boolean matchesStaged
) {
}
