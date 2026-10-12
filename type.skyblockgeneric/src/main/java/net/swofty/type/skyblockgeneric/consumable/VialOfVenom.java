package net.swofty.type.skyblockgeneric.consumable;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class VialOfVenom {
    public static final String ITEM_ID = "VIAL_OF_VENOM";
    public static final int LIMIT = 5;
    public static final double CRIT_DAMAGE_EACH = 1;
    public static final double XP_EACH = 2;
    public static final String MAXED_MESSAGE = "<c>You've already consumed the maximum amount of this item!";
    private static final String CONSUMED = "vial_of_venom:consumed";
    private static final List<String> MESSAGES = List.of(
            "<c><l>YIPPEE! <7>The venom stings as it trickles down your throat. Immunity here you come!",
            "<c><l>GULP! <7>The venom is viscous and hard to swallow. Was this a good idea?",
            "<c><l>BLEGH! <7>The taste just keeps getting worse and worse. Whose idea was this, again?",
            "<c><l>CHUG! <7>You finish the vial in a single swig. You can feel your insides disintegrating.",
            "<c><l>NOPE! <7>No...more...organs...ouchie.");

    private VialOfVenom() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register(ITEM_ID, InteractableItemConfig.builder()
                .rightClickHandler(VialOfVenom::drink).build());
    }

    public static double criticalDamage(int consumed) {
        return Math.min(Math.max(consumed, 0), LIMIT) * CRIT_DAMAGE_EACH;
    }

    public static double experience(int consumed) {
        return Math.min(Math.max(consumed, 0), LIMIT) * XP_EACH;
    }

    public static String messageFor(int consumedBefore) {
        return consumedBefore >= LIMIT ? MAXED_MESSAGE : MESSAGES.get(Math.max(consumedBefore, 0));
    }

    public static ItemStatistics statistics(int consumed) {
        if (consumed <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.CRITICAL_DAMAGE, criticalDamage(consumed)).build();
    }

    public static ItemStatistics permanentStatistics(SkyBlockPlayer player) {
        return statistics(consumed(player));
    }

    public static int consumed(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return 0;
        return (int) DatapointCrimsonIsle.of(player).getNumber(CONSUMED);
    }

    public static void setConsumed(SkyBlockPlayer player, int consumed) {
        int clamped = Math.min(Math.max(consumed, 0), LIMIT);
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(CONSUMED, (long) clamped));
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.VIAL_OF_VENOM, experience(clamped));
    }

    private static void drink(SkyBlockPlayer player, SkyBlockItem item) {
        int consumed = consumed(player);
        if (consumed >= LIMIT) {
            player.sendMessage(MAXED_MESSAGE);
            return;
        }
        setConsumed(player, consumed + 1);
        TimedConsumables.consumeHeld(player, item);
        player.sendMessage(messageFor(consumed));
    }
}
