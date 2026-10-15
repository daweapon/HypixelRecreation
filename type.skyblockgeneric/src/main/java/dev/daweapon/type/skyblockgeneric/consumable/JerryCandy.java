package dev.daweapon.type.skyblockgeneric.consumable;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.potion.GodPotionService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;

public final class JerryCandy {
    public static final String ID = "JERRY_CANDY";
    public static final long DURATION_MILLIS = 20L * 60 * 1000;
    private static final String BUFF_NAME = "Jerry";

    private JerryCandy() {
    }

    public static ItemStatistics statistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, 100D)
                .withBase(ItemStatistic.STRENGTH, 20D)
                .withBase(ItemStatistic.FEROCITY, 2D)
                .withBase(ItemStatistic.INTELLIGENCE, 100D)
                .withBase(ItemStatistic.MAGIC_FIND, 3D)
                .build();
    }

    public static void registerInteractables() {
        InteractableRegistry.register(ID, InteractableItemConfig.builder().rightClickHandler((player, item) -> {
            if (GodPotionService.isActive(player)) {
                player.sendMessage("<c>You can't consume a Jerry Candy while a God Potion is active!");
                return;
            }
            if (!TimedConsumables.start(player, ID, DURATION_MILLIS)) {
                player.sendMessage("<c>You already have the Jerry effect active!");
                return;
            }
            TimedConsumables.consumeHeld(player, item);
            apply(player, DURATION_MILLIS);
            player.sendMessage("<a>You gained the <a>Jerry <a>effect for <b>20 minutes<a>!");
        }).build());
    }

    public static void restore(SkyBlockPlayer player) {
        long remaining = TimedConsumables.expiration(player, ID) - System.currentTimeMillis();
        if (remaining > 0) apply(player, remaining);
    }

    public static void apply(SkyBlockPlayer player, long millis) {
        player.getStatistics().removeTemporaryStatistics(statistic -> BUFF_NAME.equals(statistic.getDisplayName()));
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(statistics())
                .withExpirationInMs(millis)
                .withDisplayName(BUFF_NAME)
                .withDisplayColor("<a>")
                .build());
    }
}
