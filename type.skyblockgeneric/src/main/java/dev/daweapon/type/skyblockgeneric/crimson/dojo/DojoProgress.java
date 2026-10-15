package dev.daweapon.type.skyblockgeneric.crimson.dojo;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle.CrimsonIsleData;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Per-profile Dojo state, kept in {@link DatapointCrimsonIsle}'s numbers: each test's best score and
 * that run's time, whether Master Tao has met you, and the highest belt reward claimed.
 */
public final class DojoProgress {
    private static final String BEST = "dojo:best:";
    private static final String TIME = "dojo:time:";
    private static final String MET = "dojo:met";
    private static final String BELT = "dojo:belt";

    private DojoProgress() {
    }

    public static boolean hasAttempted(SkyBlockPlayer player, DojoTest test) {
        return DatapointCrimsonIsle.of(player).getNumbers().containsKey(BEST + test.name());
    }

    public static int best(SkyBlockPlayer player, DojoTest test) {
        return (int) DatapointCrimsonIsle.of(player).getNumber(BEST + test.name());
    }

    public static long bestTimeMillis(SkyBlockPlayer player, DojoTest test) {
        return DatapointCrimsonIsle.of(player).getNumber(TIME + test.name());
    }

    public static DojoGrade bestGrade(SkyBlockPlayer player, DojoTest test) {
        return DojoGrade.of(best(player, test));
    }

    /** Dojo Points: the sum of every test's personal best, with scores above 1,000 still counting. */
    public static long points(SkyBlockPlayer player) {
        CrimsonIsleData data = DatapointCrimsonIsle.of(player);
        long total = 0;
        for (DojoTest test : DojoTest.values()) total += data.getNumber(BEST + test.name());
        return total;
    }

    /** Only the Test of Force starts unlocked; passing a test (D or better) unlocks the next. */
    public static boolean isUnlocked(SkyBlockPlayer player, DojoTest test) {
        DojoTest previous = test.previous();
        return previous == null || bestGrade(player, previous).isPassing();
    }

    /** Stores a finished run if it beats the previous best. Returns whether it did. */
    public static boolean record(SkyBlockPlayer player, DojoTest test, int score, long timeMillis) {
        int clamped = Math.max(0, score);
        boolean newBest = !hasAttempted(player, test) || clamped > best(player, test);
        if (!newBest) return false;
        DatapointCrimsonIsle.edit(player, data -> {
            data.getNumbers().put(BEST + test.name(), (long) clamped);
            data.getNumbers().put(TIME + test.name(), timeMillis);
        });
        return true;
    }

    public static boolean hasMetTao(SkyBlockPlayer player) {
        return DatapointCrimsonIsle.of(player).getNumber(MET) > 0;
    }

    public static void setMetTao(SkyBlockPlayer player) {
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(MET, 1L));
    }

    /** The highest belt Master Tao has handed over (White from the first talk, the rest as claimed rewards). */
    public static @Nullable DojoBelt highestBelt(SkyBlockPlayer player) {
        long stored = DatapointCrimsonIsle.of(player).getNumber(BELT);
        return stored <= 0 ? null : DojoBelt.values()[(int) Math.min(stored, DojoBelt.values().length) - 1];
    }

    public static boolean hasClaimed(SkyBlockPlayer player, DojoBelt belt) {
        DojoBelt highest = highestBelt(player);
        return highest != null && highest.ordinal() >= belt.ordinal();
    }

    /** The next belt reward in line, or null once Black is claimed. */
    public static @Nullable DojoBelt nextBelt(SkyBlockPlayer player) {
        DojoBelt highest = highestBelt(player);
        int next = highest == null ? 0 : highest.ordinal() + 1;
        return next < DojoBelt.values().length ? DojoBelt.values()[next] : null;
    }

    public static void setHighestBelt(SkyBlockPlayer player, DojoBelt belt) {
        DatapointCrimsonIsle.edit(player, data -> {
            if (data.getNumber(BELT) < belt.ordinal() + 1) data.getNumbers().put(BELT, (long) belt.ordinal() + 1);
        });
        syncSkyBlockXP(player);
    }

    /** SkyBlock XP follows the highest belt obtained, so skipped tiers still pay out. */
    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.THE_DOJO,
                DojoBelt.cumulativeSkyBlockXP(highestBelt(player)));
    }

    /** /maxprofile: S on every test and every belt reward claimed, without handing out belt items. */
    public static void maxOut(SkyBlockPlayer player) {
        DatapointCrimsonIsle.edit(player, data -> {
            data.getNumbers().put(MET, 1L);
            for (DojoTest test : DojoTest.values()) {
                if (data.getNumber(BEST + test.name()) < DojoGrade.S.getRequiredScore()) {
                    data.getNumbers().put(BEST + test.name(), (long) DojoGrade.S.getRequiredScore());
                    long limit = test.isTimed() ? test.getTimeLimitSeconds() * 1000L : 0L;
                    data.getNumbers().put(TIME + test.name(), limit);
                }
            }
            data.getNumbers().put(BELT, (long) DojoBelt.values().length);
        });
        syncSkyBlockXP(player);
    }
}
