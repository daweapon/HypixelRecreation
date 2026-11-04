package dev.daweapon.service.control.backup;

import dev.daweapon.service.control.docker.DockerApi;
import dev.daweapon.service.control.docker.DockerException;
import dev.daweapon.service.control.docker.TarWriter;
import dev.daweapon.service.control.fleet.ContainerInfo;
import dev.daweapon.service.control.fleet.ContainerState;
import dev.daweapon.service.control.fleet.FleetGroup;
import dev.daweapon.service.control.fleet.FleetService;
import dev.daweapon.service.control.fleet.FleetSnapshot;
import dev.daweapon.service.control.fleet.LifecycleController;
import dev.daweapon.service.control.fleet.OperationJob;
import dev.daweapon.service.control.fleet.SharedDependency;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Restores a managed backup.
 *
 * Two modes:
 * - SCRATCH (the default) is non-destructive. Mongo is restored into {@code restore_<db>}
 *   databases beside the live ones so a document can be compared or copied by hand; Redis is
 *   only verified with redis-check-rdb, since a second Redis would need its own container.
 * - LIVE replaces production data. A fresh PRE_RESTORE backup is taken first and the restore is
 *   refused if that backup fails, so a live restore can always itself be undone. Redis is
 *   stopped, its data files replaced and started again.
 *
 * Runs as a lifecycle job so it cannot interleave with a deploy or staggered restart, and so
 * the watchdog stays quiet while Redis is deliberately down.
 */
public final class RestoreService {
    public enum Mode {
        SCRATCH,
        LIVE;

        public static Mode parse(String value) {
            if (value == null || value.isBlank()) {
                return SCRATCH;
            }
            try {
                return valueOf(value.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown restore mode '" + value + "' - expected scratch or live");
            }
        }
    }

    public static final String SCRATCH_PREFIX = "restore_";
    private static final Duration REDIS_STOP_GRACE = Duration.ofSeconds(20);
    private static final Duration REDIS_READY_TIMEOUT = Duration.ofSeconds(180);
    private static final Duration SHORT_EXEC = Duration.ofSeconds(30);

    private final DockerApi docker;
    private final FleetService fleetService;
    private final LifecycleController lifecycle;
    private final BackupService backups;

    public RestoreService(DockerApi docker, FleetService fleetService, LifecycleController lifecycle,
                          BackupService backups) {
        this.docker = docker;
        this.fleetService = fleetService;
        this.lifecycle = lifecycle;
        this.backups = backups;
    }

    /** Arguments for mongorestore; admin, config and local are never touched. */
    public static List<String> mongorestoreArgs(String archive, Mode mode) {
        List<String> args = new ArrayList<>(List.of("mongorestore", "--archive=" + archive, "--gzip",
                "--drop", "--nsExclude=admin.*", "--nsExclude=config.*", "--nsExclude=local.*"));
        if (mode == Mode.SCRATCH) {
            args.add("--nsFrom=$db$.$coll$");
            args.add("--nsTo=" + SCRATCH_PREFIX + "$db$.$coll$");
        }
        return args;
    }

    /**
     * Checks a request and describes exactly what it would do, without doing any of it. The
     * owner confirms against this text, so it names every destructive step.
     */
    public Map<String, Object> preview(String id, List<String> parts, Mode mode) {
        BackupRecord record = requireRestorable(id, parts);
        List<String> steps = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        steps.add("verify the sha256 of " + String.join(" and ", parts) + " from backup " + id);
        if (mode == Mode.LIVE) {
            steps.add("take a fresh pre-restore backup first, and stop if it does not complete");
        }
        if (parts.contains(BackupRecord.MONGO)) {
            steps.add(mode == Mode.LIVE
                    ? "mongorestore --drop into the LIVE databases: every collection in the backup is "
                    + "replaced with the backed-up copy"
                    : "mongorestore into scratch databases named " + SCRATCH_PREFIX
                    + "<db>; live data is not touched");
        }
        if (parts.contains(BackupRecord.REDIS)) {
            steps.add(mode == Mode.LIVE
                    ? "stop Redis, replace its RDB/AOF files with the backup, start it again"
                    : "verify the Redis RDB with redis-check-rdb; live Redis is not touched");
        }

        if (mode == Mode.LIVE) {
            List<String> runningServers = runningGameServers(fleetService.refresh());
            if (!runningServers.isEmpty()) {
                warnings.add(runningServers.size() + " game server(s) are running and will keep writing "
                        + "their in-memory state over restored data: " + String.join(", ", runningServers)
                        + ". Stop them first unless that is intended.");
            }
            if (parts.contains(BackupRecord.REDIS)) {
                warnings.add("Redis will be down for the restore; services reconnect on their own.");
            }
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", id);
        payload.put("createdAt", record.createdAt());
        payload.put("parts", parts);
        payload.put("mode", mode.name());
        payload.put("steps", steps);
        payload.put("warnings", warnings);
        return payload;
    }

    public OperationJob restore(String id, List<String> parts, Mode mode, String actor) {
        BackupRecord record = requireRestorable(id, parts);
        return lifecycle.submitExternal("restore:" + id + ":" + mode.name().toLowerCase(Locale.ROOT), actor, job -> {
            for (String kind : parts) {
                verifyHash(job, record, kind);
            }

            if (mode == Mode.LIVE) {
                job.setCurrentStep("taking a pre-restore backup");
                BackupRecord safety = backups.runBlocking(BackupRecord.Trigger.PRE_RESTORE, actor);
                boolean covered = parts.stream().allMatch(kind ->
                        safety.part(kind) != null && safety.part(kind).ok());
                if (!covered) {
                    job.record("backup", "pre-restore", false, "pre-restore backup " + safety.id()
                            + " was " + safety.status() + " - live restore aborted, nothing changed");
                    return;
                }
                job.record("backup", "pre-restore", true, "saved as " + safety.id());
            }

            FleetSnapshot fleet = fleetService.refresh();
            if (parts.contains(BackupRecord.MONGO)) {
                restoreMongo(job, record, mode, BackupService.dependencyContainer(fleet, SharedDependency.MONGO));
            }
            if (parts.contains(BackupRecord.REDIS)) {
                String redis = BackupService.dependencyContainer(fleet, SharedDependency.REDIS);
                if (mode == Mode.LIVE) {
                    restoreRedisLive(job, record, redis);
                } else {
                    verifyRedis(job, record, redis);
                }
            }
        });
    }

    private BackupRecord requireRestorable(String id, List<String> parts) {
        BackupRecord record = backups.find(id);
        if (record == null) {
            throw new IllegalArgumentException("No managed backup with id " + id);
        }
        if (parts == null || parts.isEmpty()) {
            throw new IllegalArgumentException("Choose what to restore: mongo, redis or both");
        }
        for (String kind : parts) {
            if (!kind.equals(BackupRecord.MONGO) && !kind.equals(BackupRecord.REDIS)) {
                throw new IllegalArgumentException("Unknown backup part '" + kind + "'");
            }
            Path file = backups.partFile(record, kind);
            if (file == null || !Files.isRegularFile(file)) {
                throw new IllegalArgumentException("Backup " + id + " has no usable " + kind + " dump");
            }
        }
        return record;
    }

    private void verifyHash(OperationJob job, BackupRecord record, String kind) throws IOException {
        Path file = backups.partFile(record, kind);
        String expected = record.part(kind).sha256();
        String actual = BackupService.sha256(file);
        if (!actual.equals(expected)) {
            throw new IllegalStateException(kind + " dump of " + record.id()
                    + " does not match its recorded sha256 - refusing to restore a corrupted file");
        }
        job.record(kind, "verify", true, "sha256 " + actual.substring(0, 16) + "... matches");
    }

    private void restoreMongo(OperationJob job, BackupRecord record, Mode mode, String container) {
        if (container == null) {
            job.record(BackupRecord.MONGO, "restore", false, "Mongo container is not running");
            return;
        }
        job.setCurrentStep("restoring Mongo (" + mode.name().toLowerCase(Locale.ROOT) + ")");
        String name = "control-restore-" + record.id() + ".archive.gz";
        String remote = "/tmp/" + name;
        try {
            docker.copyFilesIn(container, "/tmp",
                    List.of(TarWriter.Entry.file(name, backups.partFile(record, BackupRecord.MONGO))),
                    BackupService.MONGO_BUDGET);
            DockerApi.ExecResult result = docker.exec(container, mongorestoreArgs(remote, mode),
                    BackupService.MONGO_BUDGET);
            job.record(container, "restore", result.ok(), result.ok()
                    ? (mode == Mode.LIVE ? "restored into the live databases"
                    : "restored into " + SCRATCH_PREFIX + "* databases")
                    : "mongorestore exited " + result.exitCode() + ": " + BackupService.tail(result.output()));
        } catch (DockerException e) {
            job.record(container, "restore", false, e.getMessage());
        } finally {
            backups.removeQuietly(container, remote);
        }
    }

    private void verifyRedis(OperationJob job, BackupRecord record, String container) {
        if (container == null) {
            job.record(BackupRecord.REDIS, "verify-rdb", false, "Redis container is not running");
            return;
        }
        job.setCurrentStep("verifying the Redis RDB");
        String name = "control-restore-" + record.id() + ".rdb";
        try {
            docker.copyFilesIn(container, "/tmp",
                    List.of(TarWriter.Entry.file(name, backups.partFile(record, BackupRecord.REDIS))),
                    BackupService.REDIS_BUDGET);
            DockerApi.ExecResult result = docker.exec(container, List.of("redis-check-rdb", "/tmp/" + name),
                    BackupService.REDIS_BUDGET);
            job.record(container, "verify-rdb", result.ok(), result.ok()
                    ? "RDB is readable: " + BackupService.tail(lastLine(result.output()))
                    : "redis-check-rdb exited " + result.exitCode() + ": " + BackupService.tail(result.output()));
        } catch (DockerException e) {
            job.record(container, "verify-rdb", false, e.getMessage());
        } finally {
            backups.removeQuietly(container, "/tmp/" + name);
        }
    }

    private void restoreRedisLive(OperationJob job, BackupRecord record, String container) {
        if (container == null) {
            job.record(BackupRecord.REDIS, "restore", false, "Redis container is not running");
            return;
        }
        RedisPersistence persistence;
        try {
            persistence = backups.redisPersistence(container);
        } catch (DockerException e) {
            job.record(container, "restore", false, "could not read Redis persistence config: " + e.getMessage());
            return;
        }
        job.record(container, "config", true, "data in " + persistence.dir() + ", AOF "
                + (persistence.appendOnly() ? "on" : "off"));

        job.setCurrentStep("stopping " + container);
        try {
            docker.stop(container, (int) REDIS_STOP_GRACE.toSeconds());
            job.record(container, "stop", true, "stopped");

            job.setCurrentStep("replacing Redis data files");
            docker.copyFilesIn(container, persistence.dir(),
                    persistence.restoreEntries(backups.partFile(record, BackupRecord.REDIS),
                            Instant.now().getEpochSecond()),
                    BackupService.REDIS_BUDGET);
            job.record(container, "replace", true, persistence.appendOnly()
                    ? "wrote " + persistence.dbFilename() + " and a new AOF manifest based on the backup"
                    : "wrote " + persistence.dbFilename());
        } catch (DockerException e) {
            job.record(container, "replace", false, e.getMessage());
        } finally {
            // Whatever happened above, Redis must not be left stopped.
            job.setCurrentStep("starting " + container);
            try {
                docker.start(container);
                job.record(container, "start", true, "start issued");
            } catch (DockerException e) {
                job.record(container, "start", false, e.getMessage());
            }
        }

        awaitRedis(job, container);
    }

    private void awaitRedis(OperationJob job, String container) {
        Instant deadline = Instant.now().plus(REDIS_READY_TIMEOUT);
        while (Instant.now().isBefore(deadline)) {
            try {
                ContainerInfo info = docker.inspect(container);
                if (info != null && info.state() == ContainerState.UP) {
                    DockerApi.ExecResult size = docker.exec(container, List.of("redis-cli", "DBSIZE"), SHORT_EXEC);
                    job.record(container, "health", size.ok(), size.ok()
                            ? "back up with " + size.output().replace("(integer)", "").trim() + " keys in db0"
                            : "up, but DBSIZE failed: " + BackupService.tail(size.output()));
                    return;
                }
                if (info != null && info.state() == ContainerState.EXIT) {
                    job.record(container, "health", false, "exited with code " + info.exitCode()
                            + " - check its logs; the pre-restore backup can put the old data back");
                    return;
                }
            } catch (DockerException e) {
                Logger.debug("Waiting for {}: {}", container, e.getMessage());
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        job.record(container, "health", false, "not healthy within " + REDIS_READY_TIMEOUT.toSeconds() + "s");
    }

    static List<String> runningGameServers(FleetSnapshot fleet) {
        return fleet.containers().stream()
                .filter(c -> c.group() == FleetGroup.GAME_SERVER)
                .filter(c -> c.state() == ContainerState.UP || c.state() == ContainerState.WRN)
                .map(ContainerInfo::name)
                .toList();
    }

    private static String lastLine(String output) {
        String[] lines = output.strip().split("\\R");
        return lines.length == 0 ? "" : lines[lines.length - 1];
    }
}
