package dev.daweapon.type.skyblockgeneric.launch;

import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.function.LongSupplier;

/**
 * A {@code public: true|false} switch kept in a config file. Every server re-reads the file when it changes
 * (checked at most every {@link #RECHECK_MILLIS}), so launching is a config edit with no rebuild or restart.
 * A missing or unreadable file means not launched.
 */
public class LaunchFlag {
    public static final String KEY = "public";
    public static final long RECHECK_MILLIS = 5_000;

    private final String name;
    private final Path file;
    private final LongSupplier clock;
    private volatile boolean launched;
    private volatile FileTime readAt;
    private volatile long checkedAt = Long.MIN_VALUE;

    public LaunchFlag(String name, Path file, LongSupplier clock) {
        this.name = name;
        this.file = file;
        this.clock = clock;
    }

    public synchronized boolean isPublic() {
        long now = clock.getAsLong();
        if (checkedAt != Long.MIN_VALUE && now - checkedAt < RECHECK_MILLIS) return launched;
        checkedAt = now;
        try {
            if (!Files.isRegularFile(file)) {
                launched = false;
                readAt = null;
                return false;
            }
            FileTime modified = Files.getLastModifiedTime(file);
            if (modified.equals(readAt)) return launched;
            boolean next = read(file);
            if (next != launched) Logger.info("{} launch flag is now {}", name, next ? "ON (public)" : "OFF (staff only)");
            launched = next;
            readAt = modified;
        } catch (Exception exception) {
            Logger.warn(exception, "Couldn't read {}; {} stays staff-only", file, name);
            launched = false;
        }
        return launched;
    }

    public static boolean read(Path file) throws Exception {
        try (InputStream stream = Files.newInputStream(file)) {
            Object root = new Yaml().load(stream);
            return root instanceof Map<?, ?> map && Boolean.TRUE.equals(map.get(KEY));
        }
    }
}
