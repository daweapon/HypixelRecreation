package dev.daweapon.type.skyblockgeneric.experiments;

import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointExperiments;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.LocalDate;
import java.time.ZoneOffset;

public final class ExperimentTable {
    public static final int CHARGES = 3;
    public static final long CHARGE_REGEN_MILLIS = 24L * 60 * 60 * 1000;
    public static final int[] RENEWAL_LEVELS = {50, 100, 200};
    public static final int[] RENEWAL_BITS = {150, 300, 500};
    public static final int SERUM_LIMIT = 3;
    public static final int SERUM_BONUS_CLICKS = 2;
    public static final int CHRONOMATRON_REQUIRED_LEVEL = 20;
    public static final int ULTRASEQUENCER_REQUIRED_LEVEL = 25;
    public static final int TABLE_RECIPE_LEVEL = 10;

    private static final String CHARGE_PREFIX = "chargeUsedAt";
    private static final String EXTRA_GAMES = "extraGames";
    private static final String RENEW_DAY = "renewDay";
    private static final String RENEW_COUNT = "renewCount";
    private static final String SERUMS = "serums";
    private static final String CHRONOMATRON_CLICKS = "chronomatronClicks";
    private static final String ULTRASEQUENCER_CLICKS = "ultrasequencerClicks";
    private static final String CHRONOMATRON_DONE = "chronomatronDone";
    private static final String ULTRASEQUENCER_DONE = "ultrasequencerDone";
    private static final String ATTEMPT_PREFIX = "attempt:";

    private ExperimentTable() {
    }

    public static DatapointExperiments.ExperimentsData data(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.EXPERIMENTS, DatapointExperiments.class).getValue();
    }

    public static void save(SkyBlockPlayer player) {
        var datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.EXPERIMENTS, DatapointExperiments.class);
        datapoint.setValue(datapoint.getValue());
    }

    public static int enchantingLevel(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.ENCHANTING);
    }

    public static long today() {
        return LocalDate.now(ZoneOffset.UTC).toEpochDay();
    }

    public static int chargesAvailable(DatapointExperiments.ExperimentsData data, long now) {
        int available = 0;
        for (int i = 0; i < CHARGES; i++) {
            if (now - data.counter(CHARGE_PREFIX + i) >= CHARGE_REGEN_MILLIS) available++;
        }
        return available + (int) data.counter(EXTRA_GAMES);
    }

    public static long millisUntilCharge(DatapointExperiments.ExperimentsData data, long now) {
        long soonest = Long.MAX_VALUE;
        for (int i = 0; i < CHARGES; i++) {
            soonest = Math.min(soonest, data.counter(CHARGE_PREFIX + i) + CHARGE_REGEN_MILLIS - now);
        }
        return Math.max(0, soonest);
    }

    public static boolean consumeCharge(DatapointExperiments.ExperimentsData data, long now) {
        for (int i = 0; i < CHARGES; i++) {
            if (now - data.counter(CHARGE_PREFIX + i) >= CHARGE_REGEN_MILLIS) {
                data.getCounters().put(CHARGE_PREFIX + i, now);
                return true;
            }
        }
        long extra = data.counter(EXTRA_GAMES);
        if (extra <= 0) return false;
        data.getCounters().put(EXTRA_GAMES, extra - 1);
        return true;
    }

    public static int renewalsToday(DatapointExperiments.ExperimentsData data) {
        return data.counter(RENEW_DAY) == today() ? (int) data.counter(RENEW_COUNT) : 0;
    }

    public static boolean renewalsLeft(DatapointExperiments.ExperimentsData data) {
        return renewalsToday(data) < RENEWAL_LEVELS.length;
    }

    public static boolean renew(SkyBlockPlayer player) {
        DatapointExperiments.ExperimentsData data = data(player);
        int used = renewalsToday(data);
        if (used >= RENEWAL_LEVELS.length) return false;
        if (player.getLevel() < RENEWAL_LEVELS[used]) {
            player.sendMessage("<c>You need {} Experience Levels to buy another game!", RENEWAL_LEVELS[used]);
            return false;
        }
        if (player.getBits() < RENEWAL_BITS[used]) {
            player.sendMessage("<c>You need {} Bits to buy another game!", RENEWAL_BITS[used]);
            return false;
        }
        player.setLevel(player.getLevel() - RENEWAL_LEVELS[used]);
        player.removeBits(RENEWAL_BITS[used]);
        data.getCounters().put(RENEW_DAY, today());
        data.getCounters().put(RENEW_COUNT, (long) used + 1);
        data.getCounters().merge(EXTRA_GAMES, 1L, Long::sum);
        save(player);
        return true;
    }

    public static int serums(DatapointExperiments.ExperimentsData data) {
        return (int) data.counter(SERUMS);
    }

    public static boolean drinkSerum(DatapointExperiments.ExperimentsData data) {
        if (serums(data) >= SERUM_LIMIT) return false;
        data.getCounters().merge(SERUMS, 1L, Long::sum);
        return true;
    }

    public static int serumBonusClicks(DatapointExperiments.ExperimentsData data) {
        return serums(data) >= SERUM_LIMIT ? SERUM_BONUS_CLICKS : 0;
    }

    public static boolean chronomatronRequired(SkyBlockPlayer player) {
        return enchantingLevel(player) >= CHRONOMATRON_REQUIRED_LEVEL;
    }

    public static boolean ultrasequencerRequired(SkyBlockPlayer player) {
        return enchantingLevel(player) >= ULTRASEQUENCER_REQUIRED_LEVEL;
    }

    public static boolean chronomatronDone(DatapointExperiments.ExperimentsData data) {
        return data.counter(CHRONOMATRON_DONE) > 0;
    }

    public static boolean ultrasequencerDone(DatapointExperiments.ExperimentsData data) {
        return data.counter(ULTRASEQUENCER_DONE) > 0;
    }

    public static boolean superpairsUnlocked(SkyBlockPlayer player) {
        DatapointExperiments.ExperimentsData data = data(player);
        return (!chronomatronRequired(player) || chronomatronDone(data))
                && (!ultrasequencerRequired(player) || ultrasequencerDone(data));
    }

    public static void completeChronomatron(DatapointExperiments.ExperimentsData data, int clicks) {
        data.getCounters().put(CHRONOMATRON_DONE, 1L);
        data.getCounters().put(CHRONOMATRON_CLICKS, (long) clicks);
    }

    public static void completeUltrasequencer(DatapointExperiments.ExperimentsData data, int clicks) {
        data.getCounters().put(ULTRASEQUENCER_DONE, 1L);
        data.getCounters().put(ULTRASEQUENCER_CLICKS, (long) clicks);
    }

    public static int addOnClicks(DatapointExperiments.ExperimentsData data, SuperpairsStake stake) {
        return (int) (data.counter(CHRONOMATRON_CLICKS) + data.counter(ULTRASEQUENCER_CLICKS)) * stake.addOnMultiplier()
                + serumBonusClicks(data);
    }

    public static void resetAddOns(DatapointExperiments.ExperimentsData data) {
        data.getCounters().put(CHRONOMATRON_DONE, 0L);
        data.getCounters().put(ULTRASEQUENCER_DONE, 0L);
        data.getCounters().put(CHRONOMATRON_CLICKS, 0L);
        data.getCounters().put(ULTRASEQUENCER_CLICKS, 0L);
    }

    public static int attemptCost(SkyBlockPlayer player, String gameStakeKey, int restartLevels) {
        DatapointExperiments.ExperimentsData data = data(player);
        return data.counter(ATTEMPT_PREFIX + gameStakeKey + ":" + today()) > 0 ? restartLevels : 0;
    }

    public static void recordAttempt(DatapointExperiments.ExperimentsData data, String gameStakeKey) {
        String suffix = ":" + today();
        data.getCounters().keySet().removeIf(key -> key.startsWith(ATTEMPT_PREFIX) && !key.endsWith(suffix));
        data.getCounters().merge(ATTEMPT_PREFIX + gameStakeKey + ":" + today(), 1L, Long::sum);
    }

    public static boolean spendLevels(SkyBlockPlayer player, int levels) {
        if (player.getLevel() < levels) return false;
        player.setLevel(player.getLevel() - levels);
        return true;
    }

    public static void resetCharges(SkyBlockPlayer player) {
        DatapointExperiments.ExperimentsData data = data(player);
        for (int i = 0; i < CHARGES; i++) data.getCounters().put(CHARGE_PREFIX + i, 0L);
        data.getCounters().put(RENEW_COUNT, 0L);
        data.getCounters().put(EXTRA_GAMES, 0L);
        save(player);
    }

    public static void unlockAll(SkyBlockPlayer player) {
        DatapointExperiments.ExperimentsData data = data(player);
        data.getCounters().put(SERUMS, (long) SERUM_LIMIT);
        save(player);
        MetaphysicalSerum.syncXp(player);
    }
}
