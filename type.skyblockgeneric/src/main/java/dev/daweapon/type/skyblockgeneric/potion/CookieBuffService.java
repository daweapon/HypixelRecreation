package dev.daweapon.type.skyblockgeneric.potion;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;

public final class CookieBuffService {
    private static final long DURATION_MS = 4L * 24 * 60 * 60 * 1000;
    private static final String DISPLAY_NAME = "Cookie Buff";

    private CookieBuffService() {
    }

    public static void consume(SkyBlockPlayer player) {
        long expiration = Math.max(System.currentTimeMillis(), player.getBoosterCookieExpirationDate()) + DURATION_MS;
        player.setBoosterCookieExpirationDate(expiration);
        apply(player, expiration);
    }

    public static void restore(SkyBlockPlayer player) {
        long expiration = player.getBoosterCookieExpirationDate();
        if (expiration > System.currentTimeMillis()) apply(player, expiration);
    }

    public static boolean isCookieBuff(TemporaryStatistic statistic) {
        return DISPLAY_NAME.equals(statistic.getDisplayName());
    }

    public static String formatRemaining(SkyBlockPlayer player) {
        long remaining = Math.max(0, player.getBoosterCookieExpirationDate() - System.currentTimeMillis());
        long days = remaining / (24L * 60 * 60 * 1000);
        long hours = (remaining / 3_600_000) % 24;
        long minutes = (remaining / 60_000) % 60;
        return days > 0 ? days + "d " + hours + "h" : hours + "h " + minutes + "m";
    }

    private static void apply(SkyBlockPlayer player, long expiration) {
        player.getStatistics().removeTemporaryStatistics(statistic -> DISPLAY_NAME.equals(statistic.getDisplayName()));
        long remaining = expiration - System.currentTimeMillis();
        if (remaining <= 0) return;
        ItemStatistics.Builder statistics = ItemStatistics.builder().withBase(ItemStatistic.MAGIC_FIND, 15D);
        for (SkillCategories category : SkillCategories.values()) {
            statistics.withBase(category.getWisdomStatistic(), 25D);
        }
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(statistics.build())
                .withExpirationInMs(remaining)
                .withDisplayName(DISPLAY_NAME)
                .withDisplayColor("<d>")
                .build());
    }
}
