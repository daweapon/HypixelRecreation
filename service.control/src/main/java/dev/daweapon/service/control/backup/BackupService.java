package dev.daweapon.service.control.backup;

import com.google.gson.JsonObject;
import dev.daweapon.service.control.docker.DockerApi;
import dev.daweapon.service.control.docker.DockerException;
import dev.daweapon.service.control.fleet.DependencyStatus;
import dev.daweapon.service.control.fleet.FleetService;
import dev.daweapon.service.control.fleet.FleetSnapshot;
import dev.daweapon.service.control.fleet.SharedDependency;
import dev.daweapon.service.control.http.Json;
import dev.daweapon.service.control.watchdog.Alert;
import dev.daweapon.service.control.watchdog.AlertDispatcher;
import org.tinylog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * Scheduled and on-demand backups of Mongo and Redis.
 *
 * The agent deliberately holds no database credentials or client, so both dumps are taken
 * inside the database containers through Docker exec and copied out through the archive
 * endpoint: `mongodump --archive --gzip` in the Mongo container, and a Redis BGSAVE whose RDB is
 * then copied out of the Redis container.
 *
 * Backups land in {@code <BACKUP_DIR>/control/<id>/}. Anything else in BACKUP_DIR (the hand-made
 * dumps the NAS already keeps) is listed but never pruned or restored from.
 */
public final class BackupService {
    private static final Path ROOT = Path.of(System.getenv().getOrDefault("BACKUP_DIR", "/app/backups"));
    private static final String MANAGED = "control";
    private static final String MANIFEST = "manifest.json";
    private static final String SETTINGS = "settings.json";

    static final Duration MONGO_BUDGET = Duration.ofMinutes(30);
    static final Duration REDIS_BUDGET = Duration.ofMinutes(10);
    private static final Duration SHORT_EXEC = Duration.ofSeconds(30);

    private final DockerApi docker;
    private final FleetService fleetService;
    private final AlertDispatcher alerts;
    private final ZoneId zone;
    private final ReentrantLock runLock = new ReentrantLock();
    private final ExecutorService runner = Executors.newVirtualThreadPerTaskExecutor();
    private final AtomicReference<BackupRecord> running = new AtomicReference<>();

    private volatile BackupSchedule schedule;
    private volatile int retain;
    private volatile Instant nextRunAt;
    private volatile BackupRecord lastResult;
    private volatile boolean lastScheduledFailed;
    private ScheduledExecutorService ticker;

    public BackupService(DockerApi docker, FleetService fleetService, AlertDispatcher alerts) {
        this.docker = docker;
        this.fleetService = fleetService;
        this.alerts = alerts;
        this.zone = resolveZone();
        this.schedule = BackupSchedule.parse(System.getenv("BACKUP_SCHEDULE"));
        this.retain = parseRetain(System.getenv("BACKUP_RETAIN"), BackupRetention.DEFAULT_RETAIN);
        loadSettings();
    }

    public void start() {
        ticker = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "control-backup-schedule");
            thread.setDaemon(true);
            return thread;
        });
        nextRunAt = schedule.next(Instant.now(), zone);
        ticker.scheduleWithFixedDelay(this::tick, 60, 60, TimeUnit.SECONDS);
        Logger.info("Backups: {} into {} (available: {}), schedule {}, keeping {}, next run {}",
                MANAGED, ROOT, isAvailable(), schedule.spec(), retain, nextRunAt);
        if (!isAvailable()) {
            Logger.warn("Backup directory {} is not mounted or not writable - add "
                    + "'./backups:/app/backups' to service_control's volumes", ROOT);
        }
    }

    public void stop() {
        if (ticker != null) {
            ticker.shutdownNow();
        }
    }

    private void tick() {
        try {
            Instant due = nextRunAt;
            if (due == null || Instant.now().isBefore(due)) {
                return;
            }
            nextRunAt = schedule.next(Instant.now(), zone);
            BackupRecord result = runBlocking(BackupRecord.Trigger.SCHEDULED, "schedule");
            reportScheduled(result);
        } catch (Exception e) {
            Logger.error(e, "Scheduled backup tick failed");
        }
    }

    /** Pages on a failed scheduled backup, and announces the next good one. */
    private void reportScheduled(BackupRecord result) {
        String key = "backup:scheduled";
        if (result.status() != BackupRecord.Status.COMPLETE) {
            lastScheduledFailed = true;
            String detail = result.parts().stream()
                    .filter(p -> !p.ok())
                    .map(p -> p.kind() + ": " + p.message())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("no parts were written");
            alerts.dispatch(Alert.warning(key, "Scheduled backup " + result.status(), detail));
        } else if (lastScheduledFailed) {
            lastScheduledFailed = false;
            alerts.dispatch(Alert.recovery(key, "Scheduled backups recovered",
                    "Backup " + result.id() + " completed."));
        }
    }

    public boolean isAvailable() {
        return Files.isDirectory(ROOT) && Files.isWritable(ROOT);
    }

    public Path root() {
        return ROOT;
    }

    private Path managedDir() {
        return ROOT.resolve(MANAGED);
    }

    public boolean isRunning() {
        return running.get() != null;
    }

    /** Starts a backup in the background; refused while another is running. */
    public BackupRecord startBackup(BackupRecord.Trigger trigger, String actor) {
        if (!isAvailable()) {
            throw new IllegalStateException("Backup directory " + ROOT + " is not mounted into "
                    + "service_control - add './backups:/app/backups' to its volumes");
        }
        if (runLock.isLocked()) {
            throw new IllegalStateException("A backup is already running");
        }
        BackupRecord record = newRecord(trigger, actor);
        runner.submit(() -> {
            try {
                run(record);
            } catch (Exception e) {
                Logger.error(e, "Backup failed");
            }
        });
        return record;
    }

    /** Runs one backup to completion on the calling thread, waiting for any backup in progress. */
    public BackupRecord runBlocking(BackupRecord.Trigger trigger, String actor) {
        return run(newRecord(trigger, actor));
    }

    private static BackupRecord newRecord(BackupRecord.Trigger trigger, String actor) {
        Instant now = Instant.now();
        return BackupRecord.started(BackupRecord.newId(now, trigger), now, trigger, actor);
    }

    private BackupRecord run(BackupRecord record) {
        runLock.lock();
        try {
            if (!isAvailable()) {
                BackupRecord failed = record.finished(Instant.now(), List.of(
                        BackupRecord.Part.failed(BackupRecord.MONGO, null, "backup directory not mounted"),
                        BackupRecord.Part.failed(BackupRecord.REDIS, null, "backup directory not mounted")));
                lastResult = failed;
                return failed;
            }

            running.set(record);
            Path dir = managedDir().resolve(record.id());
            Files.createDirectories(dir);
            writeManifest(dir, record);
            Logger.info("Backup {} started ({} by {})", record.id(), record.trigger(), record.requestedBy());

            FleetSnapshot fleet = fleetService.refresh();
            List<BackupRecord.Part> parts = new ArrayList<>();
            parts.add(dumpMongo(dependencyContainer(fleet, SharedDependency.MONGO), dir, record.id()));
            parts.add(dumpRedis(dependencyContainer(fleet, SharedDependency.REDIS), dir));

            BackupRecord finished = record.finished(Instant.now(), parts);
            writeManifest(dir, finished);
            lastResult = finished;
            Logger.info("Backup {} finished: {} ({} bytes)", finished.id(), finished.status(), finished.totalBytes());
            prune();
            return finished;
        } catch (IOException e) {
            throw new IllegalStateException("Could not write backup: " + e.getMessage(), e);
        } finally {
            running.set(null);
            runLock.unlock();
        }
    }

    static String dependencyContainer(FleetSnapshot fleet, SharedDependency dependency) {
        for (DependencyStatus status : fleet.dependencies()) {
            if (status.dependency() == dependency && status.present() && status.state().isNominal()) {
                return status.containerName();
            }
        }
        return null;
    }

    private BackupRecord.Part dumpMongo(String container, Path dir, String id) {
        String kind = BackupRecord.MONGO;
        if (container == null) {
            return BackupRecord.Part.failed(kind, null, "Mongo container is not running");
        }
        String remote = "/tmp/control-backup-" + id + ".archive.gz";
        try {
            DockerApi.ExecResult dump = docker.exec(container,
                    List.of("mongodump", "--archive=" + remote, "--gzip", "--quiet"), MONGO_BUDGET);
            if (!dump.ok()) {
                return BackupRecord.Part.failed(kind, container,
                        "mongodump exited " + dump.exitCode() + ": " + tail(dump.output()));
            }
            Path local = dir.resolve("mongo.archive.gz");
            docker.copyFileOut(container, remote, local, MONGO_BUDGET);
            return finishedPart(kind, container, local);
        } catch (DockerException | IOException e) {
            return BackupRecord.Part.failed(kind, container, e.getMessage());
        } finally {
            removeQuietly(container, remote);
        }
    }

    private BackupRecord.Part dumpRedis(String container, Path dir) {
        String kind = BackupRecord.REDIS;
        if (container == null) {
            return BackupRecord.Part.failed(kind, null, "Redis container is not running");
        }
        try {
            RedisPersistence persistence = redisPersistence(container);
            long before = lastSave(container);
            if (before >= Instant.now().getEpochSecond() - 1) {
                // LASTSAVE has one-second resolution; a save in this same second would be
                // indistinguishable from the one about to be requested.
                Thread.sleep(1100);
            }
            DockerApi.ExecResult bgsave = docker.exec(container, List.of("redis-cli", "BGSAVE"), SHORT_EXEC);
            if (!bgsave.ok() || bgsave.output().startsWith("ERR") && !bgsave.output().contains("in progress")) {
                return BackupRecord.Part.failed(kind, container, "BGSAVE refused: " + tail(bgsave.output()));
            }

            Instant deadline = Instant.now().plus(REDIS_BUDGET);
            while (lastSave(container) <= before) {
                if (Instant.now().isAfter(deadline)) {
                    return BackupRecord.Part.failed(kind, container, "BGSAVE did not finish within "
                            + REDIS_BUDGET.toMinutes() + " minutes");
                }
                Thread.sleep(1000);
            }
            Map<String, String> info = RedisPersistence.parseInfo(
                    docker.exec(container, List.of("redis-cli", "INFO", "persistence"), SHORT_EXEC).output());
            if (!"ok".equals(info.get("rdb_last_bgsave_status"))) {
                return BackupRecord.Part.failed(kind, container,
                        "BGSAVE reported " + info.getOrDefault("rdb_last_bgsave_status", "unknown status"));
            }

            Path local = dir.resolve("redis.rdb");
            docker.copyFileOut(container, persistence.rdbPath(), local, REDIS_BUDGET);
            return finishedPart(kind, container, local);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return BackupRecord.Part.failed(kind, container, "interrupted");
        } catch (DockerException | IOException | NumberFormatException e) {
            return BackupRecord.Part.failed(kind, container, e.getMessage());
        }
    }

    RedisPersistence redisPersistence(String container) {
        DockerApi.ExecResult config = docker.exec(container, List.of("redis-cli", "CONFIG", "GET", "*"), SHORT_EXEC);
        if (!config.ok()) {
            throw new DockerException("redis-cli CONFIG GET failed: " + tail(config.output()));
        }
        return RedisPersistence.fromConfig(RedisPersistence.parseConfigReply(config.output()));
    }

    private long lastSave(String container) {
        DockerApi.ExecResult result = docker.exec(container, List.of("redis-cli", "LASTSAVE"), SHORT_EXEC);
        if (!result.ok()) {
            throw new DockerException("redis-cli LASTSAVE failed: " + tail(result.output()));
        }
        return Long.parseLong(result.output().replace("(integer)", "").trim());
    }

    private BackupRecord.Part finishedPart(String kind, String container, Path local) throws IOException {
        long size = Files.size(local);
        if (size == 0) {
            return BackupRecord.Part.failed(kind, container, "copied file is empty");
        }
        return new BackupRecord.Part(kind, container, local.getFileName().toString(), size,
                sha256(local), true, "ok");
    }

    void removeQuietly(String container, String path) {
        try {
            docker.exec(container, List.of("rm", "-f", path), SHORT_EXEC);
        } catch (DockerException e) {
            Logger.warn("Could not remove {} from {}: {}", path, container, e.getMessage());
        }
    }

    public List<BackupRecord> list() {
        List<BackupRecord> records = new ArrayList<>();
        if (!Files.isDirectory(managedDir())) {
            return records;
        }
        try (Stream<Path> dirs = Files.list(managedDir())) {
            for (Path dir : dirs.filter(Files::isDirectory).toList()) {
                BackupRecord record = readManifest(dir);
                if (record != null) {
                    records.add(record);
                }
            }
        } catch (IOException e) {
            Logger.warn(e, "Could not list backups");
        }
        BackupRecord current = running.get();
        // A RUNNING manifest that is not the backup in progress was cut short by an agent
        // restart; it is reported (and pruned) as failed rather than shown running forever.
        records.replaceAll(r -> r.status() == BackupRecord.Status.RUNNING
                && (current == null || !current.id().equals(r.id()))
                ? new BackupRecord(r.id(), r.createdAt(), r.finishedAt(), r.trigger(), r.requestedBy(),
                        BackupRecord.Status.FAILED, r.parts())
                : r);
        records.sort(Comparator.comparing(BackupRecord::createdAt).reversed());
        return records;
    }

    public BackupRecord find(String id) {
        if (!BackupRecord.isValidId(id)) {
            return null;
        }
        Path dir = managedDir().resolve(id);
        return Files.isDirectory(dir) ? readManifest(dir) : null;
    }

    public Path partFile(BackupRecord record, String kind) {
        BackupRecord.Part part = record.part(kind);
        if (part == null || !part.ok() || part.file() == null) {
            return null;
        }
        return managedDir().resolve(record.id()).resolve(part.file());
    }

    /** Dumps in BACKUP_DIR that this agent did not make: listed for context, never touched. */
    public List<Map<String, Object>> unmanaged() {
        List<Map<String, Object>> entries = new ArrayList<>();
        if (!Files.isDirectory(ROOT)) {
            return entries;
        }
        try (Stream<Path> stream = Files.list(ROOT)) {
            for (Path path : stream.filter(p -> !p.getFileName().toString().equals(MANAGED)).toList()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("name", path.getFileName().toString());
                entry.put("directory", Files.isDirectory(path));
                entry.put("bytes", sizeOf(path));
                entry.put("modifiedAt", Files.getLastModifiedTime(path).toInstant());
                entries.add(entry);
            }
        } catch (IOException e) {
            Logger.warn(e, "Could not list unmanaged backups");
        }
        entries.sort(Comparator.comparing((Map<String, Object> e) -> (Instant) e.get("modifiedAt")).reversed());
        return entries;
    }

    private static long sizeOf(Path path) {
        if (!Files.isDirectory(path)) {
            try {
                return Files.size(path);
            } catch (IOException e) {
                return -1;
            }
        }
        try (Stream<Path> walk = Files.walk(path, 4)) {
            return walk.filter(Files::isRegularFile).mapToLong(p -> {
                try {
                    return Files.size(p);
                } catch (IOException e) {
                    return 0;
                }
            }).sum();
        } catch (IOException e) {
            return -1;
        }
    }

    private void prune() {
        for (BackupRecord doomed : BackupRetention.toDelete(list(), retain)) {
            Path dir = managedDir().resolve(doomed.id());
            try (Stream<Path> walk = Files.walk(dir)) {
                for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
                Logger.info("Pruned backup {}", doomed.id());
            } catch (IOException e) {
                Logger.warn(e, "Could not prune backup {}", doomed.id());
            }
        }
    }

    public BackupSchedule schedule() {
        return schedule;
    }

    public int retain() {
        return retain;
    }

    public synchronized void updateSettings(BackupSchedule newSchedule, int newRetain) {
        if (newRetain < 1 || newRetain > 365) {
            throw new IllegalArgumentException("Retention must be between 1 and 365 backups");
        }
        schedule = newSchedule;
        retain = newRetain;
        nextRunAt = schedule.next(Instant.now(), zone);
        saveSettings();
        prune();
    }

    public Map<String, Object> status() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("directory", ROOT.toString());
        payload.put("available", isAvailable());
        payload.put("schedule", schedule.spec());
        payload.put("timezone", zone.getId());
        payload.put("retain", retain);
        payload.put("nextRunAt", nextRunAt);
        payload.put("running", running.get());
        payload.put("lastResult", lastResult);
        payload.put("backups", list());
        payload.put("unmanaged", unmanaged());
        return payload;
    }

    private void loadSettings() {
        Path file = managedDir().resolve(SETTINGS);
        if (!Files.isRegularFile(file)) {
            return;
        }
        try {
            JsonObject saved = Json.read(Files.readString(file), JsonObject.class);
            if (saved.has("schedule")) {
                schedule = BackupSchedule.parse(saved.get("schedule").getAsString());
            }
            if (saved.has("retain")) {
                retain = Math.max(1, saved.get("retain").getAsInt());
            }
        } catch (Exception e) {
            Logger.warn(e, "Ignoring unreadable backup settings at {}", file);
        }
    }

    private void saveSettings() {
        Map<String, Object> saved = new LinkedHashMap<>();
        saved.put("schedule", schedule.spec());
        saved.put("retain", retain);
        try {
            Files.createDirectories(managedDir());
            writeAtomically(managedDir().resolve(SETTINGS), Json.write(saved));
        } catch (IOException e) {
            throw new IllegalStateException("Could not save backup settings: " + e.getMessage(), e);
        }
    }

    private static void writeManifest(Path dir, BackupRecord record) throws IOException {
        writeAtomically(dir.resolve(MANIFEST), Json.write(record));
    }

    private static BackupRecord readManifest(Path dir) {
        Path file = dir.resolve(MANIFEST);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            return Json.read(Files.readString(file), BackupRecord.class);
        } catch (Exception e) {
            Logger.warn("Unreadable backup manifest {}: {}", file, e.getMessage());
            return null;
        }
    }

    private static void writeAtomically(Path target, String content) throws IOException {
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.writeString(temp, content, StandardCharsets.UTF_8);
        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public static String sha256(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[65536];
            int read;
            while ((read = in.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    static String tail(String output) {
        String trimmed = output == null ? "" : output.strip();
        return trimmed.length() > 400 ? "..." + trimmed.substring(trimmed.length() - 400) : trimmed;
    }

    private static ZoneId resolveZone() {
        String configured = System.getenv("BACKUP_TIMEZONE");
        if (configured != null && !configured.isBlank()) {
            try {
                return ZoneId.of(configured.trim());
            } catch (Exception e) {
                Logger.warn("Ignoring invalid BACKUP_TIMEZONE '{}'", configured);
            }
        }
        return ZoneId.systemDefault();
    }

    private static int parseRetain(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Math.max(1, Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
