package dev.daweapon.type.skyblockgeneric.minion.inferno;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * A permanent consumable from Hypergolic Inferno Minions: each of up to five grants +1 Health,
 * +1 Cold Resistance and 10 SkyBlock XP.
 */
public final class ReaperPepper {
    public static final int LIMIT = 5;
    public static final double XP_EACH = 10;
    private static final String EATEN = "reaper_pepper:eaten";
    private static final List<String> MESSAGES = List.of(
            "<a>You've eaten a reaper pepper, it's delicious!",
            "<a>You've eaten a reaper pepper, it's uh... really hot.",
            "<a>You've eaten a reaper pepper and begun regretting this decision.",
            "<a>You've eaten a reaper pepper, where is the MILK??",
            "<a>You've eaten a reaper pepper, you feel death creeping up in your spine. Your whole life flashes by. Is this what it means to be endgame?");

    private ReaperPepper() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register("REAPER_PEPPER", InteractableItemConfig.builder()
                .rightClickHandler(ReaperPepper::eat).build());
    }

    public static int eaten(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return 0;
        return (int) DatapointCrimsonIsle.of(player).getNumber(EATEN);
    }

    public static void setEaten(SkyBlockPlayer player, int eaten) {
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(EATEN, (long) eaten));
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.REAPER_PEPPERS, eaten * XP_EACH);
    }

    public static ItemStatistics permanentStatistics(SkyBlockPlayer player) {
        int eaten = eaten(player);
        if (eaten == 0) return ItemStatistics.empty();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, (double) eaten)
                .withBase(ItemStatistic.COLD_RESISTANCE, (double) eaten)
                .build();
    }

    private static void eat(SkyBlockPlayer player, SkyBlockItem item) {
        int eaten = eaten(player);
        if (eaten >= LIMIT) {
            player.sendMessage("<c>You already ate the maximum number of reaper peppers!");
            player.sendMessage("<c>You may want to visit a hospital ASAP.");
            return;
        }
        setEaten(player, eaten + 1);
        SkyBlockItem remaining = item.clone();
        remaining.setAmount(item.getAmount() - 1);
        player.setItemInHand(remaining.getAmount() > 0 ? remaining : null);
        player.sendMessage(MESSAGES.get(eaten));
    }
}
