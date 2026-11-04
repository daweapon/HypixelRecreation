package dev.daweapon.service.control.backup;

import dev.daweapon.service.control.docker.TarWriter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Where a Redis instance keeps its data, and the files a restore must write to replace it.
 *
 * With AOF on, Redis 7+ loads the multi-part AOF named in {@code <appendfilename>.manifest} and
 * ignores dump.rdb entirely, so writing only the RDB would restore nothing. The restore therefore
 * also writes a fresh manifest whose base file is the backup's RDB (an AOF base may be in RDB
 * format) plus an empty incremental file. Both are given a sequence number no earlier file uses,
 * so nothing already in the AOF directory is overwritten.
 */
public record RedisPersistence(String dir, String dbFilename, boolean appendOnly,
                               String appendDirName, String appendFilename) {

    public static final RedisPersistence DEFAULTS =
            new RedisPersistence("/data", "dump.rdb", false, "appendonlydir", "appendonly.aof");

    /** Parses the reply of {@code redis-cli CONFIG GET <key>} without a TTY: key and value on alternate lines. */
    public static Map<String, String> parseConfigReply(String output) {
        Map<String, String> values = new HashMap<>();
        String[] lines = output.strip().split("\\R");
        for (int i = 0; i + 1 < lines.length; i += 2) {
            values.put(lines[i].trim(), lines[i + 1].trim());
        }
        return values;
    }

    /** Parses {@code redis-cli INFO <section>}: {@code key:value} lines, comments skipped. */
    public static Map<String, String> parseInfo(String output) {
        Map<String, String> values = new HashMap<>();
        for (String line : output.split("\\R")) {
            int colon = line.indexOf(':');
            if (colon > 0 && !line.startsWith("#")) {
                values.put(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
            }
        }
        return values;
    }

    public static RedisPersistence fromConfig(Map<String, String> config) {
        return new RedisPersistence(
                nonBlank(config.get("dir"), DEFAULTS.dir()),
                nonBlank(config.get("dbfilename"), DEFAULTS.dbFilename()),
                "yes".equalsIgnoreCase(config.get("appendonly")),
                nonBlank(config.get("appenddirname"), DEFAULTS.appendDirName()),
                nonBlank(config.get("appendfilename"), DEFAULTS.appendFilename()));
    }

    public String rdbPath() {
        return dir.endsWith("/") ? dir + dbFilename : dir + "/" + dbFilename;
    }

    public static String manifest(String appendFilename, long sequence) {
        return "file " + appendFilename + "." + sequence + ".base.rdb seq " + sequence + " type b\n"
                + "file " + appendFilename + "." + sequence + ".incr.aof seq " + sequence + " type i\n";
    }

    /** The archive to unpack into {@link #dir()} while Redis is stopped. */
    public List<TarWriter.Entry> restoreEntries(Path rdb, long sequence) {
        List<TarWriter.Entry> entries = new ArrayList<>();
        entries.add(TarWriter.Entry.file(dbFilename, rdb));
        if (appendOnly) {
            String prefix = appendDirName + "/" + appendFilename + "." + sequence;
            entries.add(TarWriter.Entry.file(prefix + ".base.rdb", rdb));
            entries.add(TarWriter.Entry.bytes(prefix + ".incr.aof", new byte[0]));
            entries.add(TarWriter.Entry.bytes(appendDirName + "/" + appendFilename + ".manifest",
                    manifest(appendFilename, sequence).getBytes(StandardCharsets.UTF_8)));
        }
        return entries;
    }

    private static String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
