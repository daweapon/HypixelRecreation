package dev.daweapon.type.skyblockgeneric.consumable;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.garden.feast.FeastCalendar;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;

public final class FinnsFocaccia {
    public static final String ID = "FINNS_FOCACCIA";
    public static final double OVERBLOOM = 5;
    private static final long MILLIS_PER_TICK = 50;
    private static final String BUFF_NAME = "Finn's Focaccia";

    private FinnsFocaccia() {
    }

    public static long millisUntilEventEnd() {
        return (SkyBlockCalendar.YEAR - SkyBlockCalendar.getElapsed() % SkyBlockCalendar.YEAR) * MILLIS_PER_TICK;
    }

    public static void registerInteractables() {
        InteractableRegistry.register(ID, InteractableItemConfig.builder().rightClickHandler((player, item) -> {
            if (!FeastCalendar.grand()) {
                player.sendMessage("<c>You can only eat this during the Grand Feast!");
                return;
            }
            long duration = millisUntilEventEnd();
            if (!TimedConsumables.start(player, ID, duration)) {
                player.sendMessage("<c>You already have the Finn's Focaccia effect active!");
                return;
            }
            TimedConsumables.consumeHeld(player, item);
            apply(player, duration);
            player.sendMessage("<a>You gained <e>+5 Overbloom <a>until the Grand Feast ends!");
        }).build());
    }

    public static void restore(SkyBlockPlayer player) {
        long remaining = TimedConsumables.expiration(player, ID) - System.currentTimeMillis();
        if (remaining > 0 && FeastCalendar.grand()) apply(player, remaining);
    }

    private static void apply(SkyBlockPlayer player, long millis) {
        player.getStatistics().removeTemporaryStatistics(statistic -> BUFF_NAME.equals(statistic.getDisplayName()));
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(ItemStatistics.builder().withBase(ItemStatistic.OVERBLOOM, OVERBLOOM).build())
                .withExpirationInMs(millis)
                .withDisplayName(BUFF_NAME)
                .withDisplayColor("<e>")
                .build());
    }
}
