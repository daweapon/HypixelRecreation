package dev.daweapon.commons.config;

import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

/**
 * Copies the mounted configuration over the one baked into the image, before anything reads it.
 *
 * The server reads {@code ./configuration}, which the Dockerfile fills at build time. Compose
 * mounts the host's live configuration at {@code ./configuration_files}, which nothing read
 * back - so editing a file on the host and restarting silently kept serving the baked copy,
 * with no error to explain it. This makes the mount authoritative, which is what mounting it
 * was for.
 *
 * Overlay only: files present in the mount replace their baked counterparts, and anything the
 * mount does not contain is left alone, so a partial or missing mount degrades to the baked
 * configuration rather than wiping it.
 */
public final class ConfigurationOverlay {
    private static final Path SOURCE = Path.of("./configuration_files");
    private static final Path TARGET = Path.of("./configuration");

    // entrypoint.sh injects the proxy forwarding secret into the baked copy. The mount may carry
    // a stale or blank secret, and overwriting it breaks proxy auth, so the baked line is kept.
    private static final String ROOT_CONFIG = "config.yml";
    private static final String SECRET_KEY = "velocity-secret:";

    private ConfigurationOverlay() {
    }

    public static void apply() {
        try {
            if (!Files.isDirectory(SOURCE)) return;
            Files.createDirectories(TARGET);

            int[] counts = new int[2];
            try (Stream<Path> files = Files.walk(SOURCE)) {
                files.filter(Files::isRegularFile).forEach(source -> {
                    Path relative = SOURCE.relativize(source);
                    if (relative.getFileName().toString().equals(ROOT_CONFIG)) return;
                    try {
                        Path destination = TARGET.resolve(relative);
                        Files.createDirectories(destination.getParent());
                        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        counts[0]++;
                    } catch (IOException e) {
                        counts[1]++;
                        Logger.warn("Could not overlay configuration file " + relative + ": " + e.getMessage());
                    }
                });
            }

            mergeRootConfig();

            Logger.info("Applied mounted configuration over the baked copy: "
                    + counts[0] + " files" + (counts[1] == 0 ? "" : ", " + counts[1] + " failed"));
        } catch (Exception e) {
            // Never prevent boot; the baked configuration remains usable.
            Logger.warn("Could not apply the mounted configuration, continuing with the baked copy: " + e);
        }
    }

    private static void mergeRootConfig() throws IOException {
        Path source = SOURCE.resolve(ROOT_CONFIG);
        Path target = TARGET.resolve(ROOT_CONFIG);
        if (!Files.isRegularFile(source) || !Files.isRegularFile(target)) return;

        String bakedSecret = Files.readAllLines(target).stream()
                .filter(line -> line.stripLeading().startsWith(SECRET_KEY))
                .findFirst()
                .orElse(null);
        if (bakedSecret == null) {
            Logger.warn("Baked " + ROOT_CONFIG + " has no " + SECRET_KEY + " line; keeping the baked copy");
            return;
        }

        List<String> merged = Files.readAllLines(source).stream()
                .map(line -> line.stripLeading().startsWith(SECRET_KEY) ? bakedSecret : line)
                .toList();
        if (merged.stream().noneMatch(bakedSecret::equals)) {
            Logger.warn("Mounted " + ROOT_CONFIG + " has no " + SECRET_KEY + " line; keeping the baked copy");
            return;
        }

        Files.write(target, merged);
        Logger.info("Applied mounted " + ROOT_CONFIG + ", keeping the baked forwarding secret");
    }
}
