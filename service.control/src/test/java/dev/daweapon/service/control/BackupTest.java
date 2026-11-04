package dev.daweapon.service.control;

import dev.daweapon.service.control.backup.BackupRecord;
import dev.daweapon.service.control.backup.BackupRetention;
import dev.daweapon.service.control.backup.BackupSchedule;
import dev.daweapon.service.control.backup.RedisPersistence;
import dev.daweapon.service.control.backup.RestoreService;
import dev.daweapon.service.control.docker.TarReader;
import dev.daweapon.service.control.docker.TarWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupTest {

    @Test
    void defaultScheduleIsDailyAtFour() {
        assertEquals(BackupSchedule.Kind.DAILY, BackupSchedule.parse(null).kind());
        assertEquals(LocalTime.of(4, 0), BackupSchedule.parse("daily").at());
        assertEquals("daily@04:00", BackupSchedule.DEFAULT.spec());
    }

    @Test
    void specsRoundTrip() {
        for (String spec : List.of("off", "daily@02:30", "every 6h", "every 90m")) {
            assertEquals(spec, BackupSchedule.parse(spec).spec());
        }
        assertEquals(Duration.ofHours(12), BackupSchedule.parse("12h").every());
    }

    @Test
    void rejectsNonsenseAndTooFrequentSchedules() {
        assertThrows(IllegalArgumentException.class, () -> BackupSchedule.parse("hourly-ish"));
        assertThrows(IllegalArgumentException.class, () -> BackupSchedule.parse("daily@25:00"));
        assertThrows(IllegalArgumentException.class, () -> BackupSchedule.parse("every 5m"));
    }

    @Test
    void dailyRunsLaterTodayOrTomorrow() {
        BackupSchedule schedule = BackupSchedule.daily(LocalTime.of(4, 0));
        ZoneId utc = ZoneOffset.UTC;
        assertEquals(Instant.parse("2026-11-03T04:00:00Z"),
                schedule.next(Instant.parse("2026-11-03T01:00:00Z"), utc));
        assertEquals(Instant.parse("2026-11-04T04:00:00Z"),
                schedule.next(Instant.parse("2026-11-03T04:00:00Z"), utc));
        assertEquals(Instant.parse("2026-11-04T04:00:00Z"),
                schedule.next(Instant.parse("2026-11-03T13:00:00Z"), utc));
    }

    @Test
    void dailyFollowsTheConfiguredZone() {
        BackupSchedule schedule = BackupSchedule.daily(LocalTime.of(4, 0));
        assertEquals(Instant.parse("2026-11-03T10:00:00Z"),
                schedule.next(Instant.parse("2026-11-03T06:00:00Z"), ZoneId.of("America/Chicago")));
    }

    @Test
    void intervalAndOff() {
        Instant now = Instant.parse("2026-11-03T10:00:00Z");
        assertEquals(now.plus(Duration.ofHours(6)), BackupSchedule.parse("every 6h").next(now, ZoneOffset.UTC));
        assertNull(BackupSchedule.off().next(now, ZoneOffset.UTC));
    }

    private static BackupRecord record(int day, BackupRecord.Trigger trigger, BackupRecord.Status status) {
        Instant at = Instant.parse("2026-10-01T04:00:00Z").plus(Duration.ofDays(day));
        return new BackupRecord(BackupRecord.newId(at, trigger), at, at, trigger, "test", status, List.of());
    }

    @Test
    void retentionKeepsTheNewestCompleteBackups() {
        List<BackupRecord> backups = new ArrayList<>();
        for (int day = 0; day < 20; day++) {
            backups.add(record(day, BackupRecord.Trigger.SCHEDULED, BackupRecord.Status.COMPLETE));
        }
        List<BackupRecord> doomed = BackupRetention.toDelete(backups, 14);
        assertEquals(6, doomed.size());
        assertTrue(doomed.stream().allMatch(b -> b.createdAt().isBefore(backups.get(6).createdAt())));
    }

    @Test
    void retentionNeverDropsBelowOne() {
        List<BackupRecord> backups = List.of(
                record(0, BackupRecord.Trigger.MANUAL, BackupRecord.Status.COMPLETE),
                record(1, BackupRecord.Trigger.MANUAL, BackupRecord.Status.COMPLETE));
        assertEquals(List.of(backups.get(0)), BackupRetention.toDelete(backups, 0));
    }

    @Test
    void failedBackupsAreKeptOnlyUntilSuperseded() {
        BackupRecord oldFailure = record(0, BackupRecord.Trigger.SCHEDULED, BackupRecord.Status.FAILED);
        BackupRecord good = record(1, BackupRecord.Trigger.SCHEDULED, BackupRecord.Status.COMPLETE);
        BackupRecord newFailure = record(2, BackupRecord.Trigger.SCHEDULED, BackupRecord.Status.PARTIAL);
        BackupRecord inProgress = record(3, BackupRecord.Trigger.MANUAL, BackupRecord.Status.RUNNING);

        List<BackupRecord> doomed = BackupRetention.toDelete(List.of(oldFailure, good, newFailure, inProgress), 14);
        assertEquals(List.of(oldFailure), doomed);
    }

    @Test
    void preRestoreSnapshotsRotateSeparately() {
        List<BackupRecord> backups = new ArrayList<>();
        for (int day = 0; day < 5; day++) {
            backups.add(record(day, BackupRecord.Trigger.PRE_RESTORE, BackupRecord.Status.COMPLETE));
        }
        backups.add(record(10, BackupRecord.Trigger.SCHEDULED, BackupRecord.Status.COMPLETE));

        List<BackupRecord> doomed = BackupRetention.toDelete(backups, 1);
        assertEquals(2, doomed.size());
        assertTrue(doomed.stream().allMatch(b -> b.trigger() == BackupRecord.Trigger.PRE_RESTORE));
    }

    @Test
    void statusReflectsWhichPartsSucceeded() {
        BackupRecord.Part ok = new BackupRecord.Part("mongo", "m", "f", 1, "h", true, "ok");
        BackupRecord.Part bad = BackupRecord.Part.failed("redis", "r", "down");
        assertEquals(BackupRecord.Status.COMPLETE, BackupRecord.statusOf(List.of(ok)));
        assertEquals(BackupRecord.Status.PARTIAL, BackupRecord.statusOf(List.of(ok, bad)));
        assertEquals(BackupRecord.Status.FAILED, BackupRecord.statusOf(List.of(bad)));
    }

    @Test
    void idsCannotEscapeTheBackupDirectory() {
        String id = BackupRecord.newId(Instant.parse("2026-11-03T04:00:00Z"), BackupRecord.Trigger.PRE_RESTORE);
        assertEquals("20261103-040000-pre-restore", id);
        assertTrue(BackupRecord.isValidId(id));
        assertFalse(BackupRecord.isValidId("../../etc"));
        assertFalse(BackupRecord.isValidId("20261103-040000-x/.."));
    }

    @Test
    void tarRoundTripsThroughTheReader() throws Exception {
        byte[] content = "redis rdb bytes".repeat(100).getBytes(StandardCharsets.UTF_8);
        byte[] tar = TarWriter.singleFile("dump.rdb", content);
        assertEquals(TarWriter.length(List.of(TarWriter.Entry.bytes("dump.rdb", content))), tar.length);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long size = TarReader.extractFirstFile(new ByteArrayInputStream(tar), out);
        assertEquals(content.length, size);
        assertArrayEquals(content, out.toByteArray());
    }

    @Test
    void parsesRedisConfigAndInfo() {
        Map<String, String> config = RedisPersistence.parseConfigReply(
                "dir\n/data\ndbfilename\ndump.rdb\nsave\n\nappendonly\nyes\nappenddirname\nappendonlydir\n");
        RedisPersistence persistence = RedisPersistence.fromConfig(config);
        assertTrue(persistence.appendOnly());
        assertEquals("/data/dump.rdb", persistence.rdbPath());
        assertEquals("appendonly.aof", persistence.appendFilename());

        Map<String, String> info = RedisPersistence.parseInfo(
                "# Persistence\r\nrdb_bgsave_in_progress:0\r\nrdb_last_bgsave_status:ok\r\n");
        assertEquals("ok", info.get("rdb_last_bgsave_status"));
    }

    @Test
    void aofRestoreWritesANewManifestBasedOnTheRdb() {
        RedisPersistence aof = new RedisPersistence("/data", "dump.rdb", true, "appendonlydir", "appendonly.aof");
        List<TarWriter.Entry> entries = aof.restoreEntries(Path.of("redis.rdb"), 1730000000L);
        List<String> names = entries.stream().map(TarWriter.Entry::name).toList();
        assertEquals(List.of("dump.rdb",
                "appendonlydir/appendonly.aof.1730000000.base.rdb",
                "appendonlydir/appendonly.aof.1730000000.incr.aof",
                "appendonlydir/appendonly.aof.manifest"), names);
        assertEquals("file appendonly.aof.1730000000.base.rdb seq 1730000000 type b\n"
                        + "file appendonly.aof.1730000000.incr.aof seq 1730000000 type i\n",
                new String(entries.get(3).content(), StandardCharsets.UTF_8));

        RedisPersistence rdbOnly = RedisPersistence.DEFAULTS;
        assertEquals(1, rdbOnly.restoreEntries(Path.of("redis.rdb"), 1).size());
    }

    @Test
    void scratchRestoreNeverTouchesLiveDatabases() {
        List<String> scratch = RestoreService.mongorestoreArgs("/tmp/a.gz", RestoreService.Mode.SCRATCH);
        assertTrue(scratch.contains("--nsTo=restore_$db$.$coll$"));
        assertTrue(scratch.contains("--nsExclude=admin.*"));

        List<String> live = RestoreService.mongorestoreArgs("/tmp/a.gz", RestoreService.Mode.LIVE);
        assertTrue(live.contains("--drop"));
        assertFalse(live.stream().anyMatch(arg -> arg.startsWith("--nsTo")));

        assertEquals(RestoreService.Mode.SCRATCH, RestoreService.Mode.parse(null));
        assertThrows(IllegalArgumentException.class, () -> RestoreService.Mode.parse("yolo"));
    }
}
