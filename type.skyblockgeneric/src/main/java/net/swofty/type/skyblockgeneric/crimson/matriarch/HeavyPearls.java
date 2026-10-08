package net.swofty.type.skyblockgeneric.crimson.matriarch;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * The Matriarch's daily Heavy Pearls for one profile (wiki: The_Matriarch): 3 a day, reset at 8 PM Eastern, plus up
 * to 4 Matriarch's Perfumes a day that each refill her to 3. Dying in her belly locks her for 10 minutes.
 */
public final class HeavyPearls {
    public static final int DAILY_PEARLS = 3;
    public static final int DAILY_PERFUMES = 4;
    public static final long DEATH_COOLDOWN_MS = 10 * 60 * 1000;
    static final String DAY = "matriarch:day";
    static final String PEARLS = "matriarch:pearls";
    static final String PERFUMES = "matriarch:perfumes";
    static final String COOLDOWN = "matriarch:cooldown";
    private static final ZoneId EASTERN = ZoneId.of("America/New_York");
    private static final int RESET_HOUR = 20;

    private HeavyPearls() {
    }

    public static long currentDay() {
        return ZonedDateTime.now(EASTERN).minusHours(RESET_HOUR).toLocalDate().toEpochDay() + 1;
    }

    private static void refresh(SkyBlockPlayer player) {
        long today = currentDay();
        if (DatapointCrimsonIsle.of(player).getNumber(DAY) == today) return;
        DatapointCrimsonIsle.edit(player, data -> {
            data.getNumbers().put(DAY, today);
            data.getNumbers().put(PEARLS, (long) DAILY_PEARLS);
            data.getNumbers().put(PERFUMES, 0L);
        });
    }

    public static int available(SkyBlockPlayer player) {
        refresh(player);
        return (int) DatapointCrimsonIsle.of(player).getNumber(PEARLS);
    }

    public static int perfumesUsed(SkyBlockPlayer player) {
        refresh(player);
        return (int) DatapointCrimsonIsle.of(player).getNumber(PERFUMES);
    }

    public static boolean take(SkyBlockPlayer player) {
        int left = available(player);
        if (left <= 0) return false;
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(PEARLS, (long) left - 1));
        return true;
    }

    public static boolean usePerfume(SkyBlockPlayer player) {
        int used = perfumesUsed(player);
        if (used >= DAILY_PERFUMES) return false;
        DatapointCrimsonIsle.edit(player, data -> {
            data.getNumbers().put(PERFUMES, (long) used + 1);
            data.getNumbers().put(PEARLS, (long) DAILY_PEARLS);
        });
        return true;
    }

    public static boolean onCooldown(SkyBlockPlayer player) {
        return DatapointCrimsonIsle.of(player).getNumber(COOLDOWN) > System.currentTimeMillis();
    }

    public static void startCooldown(SkyBlockPlayer player) {
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(COOLDOWN, System.currentTimeMillis() + DEATH_COOLDOWN_MS));
    }
}
