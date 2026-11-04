package dev.daweapon.service.control.backup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Decides which managed backups to prune.
 *
 * - The newest {@code retain} complete scheduled/manual backups are kept.
 * - Pre-restore snapshots rotate separately, so a burst of restores cannot push every routine
 *   backup out, and routine backups cannot silently eat the snapshot a restore would undo to.
 * - A failed or partial backup is kept only while it is newer than every complete one: it is the
 *   evidence of a current problem. Once a complete backup supersedes it, it goes.
 * - Running backups are never touched.
 *
 * Only the agent's own backups ever reach this; hand-made dumps beside them are not managed.
 */
public final class BackupRetention {
    public static final int DEFAULT_RETAIN = 14;
    public static final int PRE_RESTORE_RETAIN = 3;

    private BackupRetention() {
    }

    public static List<BackupRecord> toDelete(List<BackupRecord> backups, int retain) {
        int keep = Math.max(1, retain);
        List<BackupRecord> newestFirst = new ArrayList<>(backups);
        newestFirst.sort(Comparator.comparing(BackupRecord::createdAt).reversed());

        BackupRecord newestComplete = newestFirst.stream()
                .filter(b -> b.status() == BackupRecord.Status.COMPLETE)
                .findFirst().orElse(null);

        List<BackupRecord> delete = new ArrayList<>();
        int routineKept = 0;
        int preRestoreKept = 0;
        for (BackupRecord backup : newestFirst) {
            switch (backup.status()) {
                case RUNNING -> {
                }
                case COMPLETE -> {
                    if (backup.trigger() == BackupRecord.Trigger.PRE_RESTORE) {
                        if (++preRestoreKept > PRE_RESTORE_RETAIN) {
                            delete.add(backup);
                        }
                    } else if (++routineKept > keep) {
                        delete.add(backup);
                    }
                }
                case PARTIAL, FAILED -> {
                    if (newestComplete != null && backup.createdAt().isBefore(newestComplete.createdAt())) {
                        delete.add(backup);
                    }
                }
            }
        }
        return delete;
    }
}
