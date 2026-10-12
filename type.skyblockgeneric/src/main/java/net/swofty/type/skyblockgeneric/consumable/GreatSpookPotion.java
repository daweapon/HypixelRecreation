package net.swofty.type.skyblockgeneric.consumable;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.TemporaryStatistic;

public final class GreatSpookPotion {
    public static final String ID = "GREAT_SPOOK_POTION";
    public static final double FEAR = 3;
    public static final long DURATION_MILLIS = 24L * 60 * 60 * 1000;
    private static final String BUFF_NAME = "Great Spook Potion";

    private GreatSpookPotion() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register(ID, InteractableItemConfig.builder().rightClickHandler((player, item) -> {
            if (!TimedConsumables.start(player, ID, DURATION_MILLIS)) {
                player.sendMessage("<c>You already have an active Great Spook Potion!");
                return;
            }
            TimedConsumables.consumeHeld(player, item);
            apply(player, DURATION_MILLIS);
            player.sendMessage("<a>You gained <5>+3 Fear <a>for <b>24 hours<a>!");
        }).build());
    }

    public static void restore(SkyBlockPlayer player) {
        long remaining = TimedConsumables.expiration(player, ID) - System.currentTimeMillis();
        if (remaining > 0) apply(player, remaining);
    }

    private static void apply(SkyBlockPlayer player, long millis) {
        player.getStatistics().removeTemporaryStatistics(statistic -> BUFF_NAME.equals(statistic.getDisplayName()));
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(ItemStatistics.builder().withBase(ItemStatistic.FEAR, FEAR).build())
                .withExpirationInMs(millis)
                .withDisplayName(BUFF_NAME)
                .withDisplayColor("<5>")
                .build());
    }
}
