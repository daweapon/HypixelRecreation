package dev.daweapon.type.skyblockgeneric.consumable;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public enum DwarvenOs {
    ORE_OATS("DWARVEN_OS_ORE_OATS", ItemStatistic.ORE_FORTUNE),
    BLOCK_BRAN("DWARVEN_OS_BLOCK_BRAN", ItemStatistic.BLOCK_FORTUNE),
    GEMSTONE_GRAHAMS("DWARVEN_OS_GEMSTONE_GRAHAMS", ItemStatistic.GEMSTONE_FORTUNE),
    METALLIC_MINIS("DWARVEN_OS_METALLIC_MINIS", ItemStatistic.DWARVEN_METAL_FORTUNE);

    public static final int LIMIT = 5;
    public static final double FORTUNE_EACH = 2;

    private final String itemId;
    private final ItemStatistic statistic;

    DwarvenOs(String itemId, ItemStatistic statistic) {
        this.itemId = itemId;
        this.statistic = statistic;
    }

    public String itemId() {
        return itemId;
    }

    public ItemStatistic statistic() {
        return statistic;
    }

    public static double fortune(int eaten) {
        return Math.min(Math.max(eaten, 0), LIMIT) * FORTUNE_EACH;
    }

    public static ItemStatistics statistics(int[] eatenPerCereal) {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        DwarvenOs[] cereals = values();
        for (int i = 0; i < cereals.length; i++) {
            if (eatenPerCereal[i] > 0) builder.withBase(cereals[i].statistic, fortune(eatenPerCereal[i]));
        }
        return builder.build();
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        int[] eaten = new int[values().length];
        for (DwarvenOs cereal : values()) eaten[cereal.ordinal()] = cereal.eaten(player);
        return statistics(eaten);
    }

    public int eaten(SkyBlockPlayer player) {
        return (int) DatapointCrimsonIsle.of(player).getNumber(key());
    }

    public void setEaten(SkyBlockPlayer player, int eaten) {
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(key(), (long) eaten));
    }

    public static void maxAll(SkyBlockPlayer player) {
        for (DwarvenOs cereal : values()) cereal.setEaten(player, LIMIT);
    }

    public static void registerInteractables() {
        for (DwarvenOs cereal : values()) {
            InteractableRegistry.register(cereal.itemId, InteractableItemConfig.builder()
                    .rightClickHandler(cereal::eat).build());
        }
    }

    private String key() {
        return "dwarven_os:" + name();
    }

    private void eat(SkyBlockPlayer player, SkyBlockItem item) {
        int eaten = eaten(player);
        if (eaten >= LIMIT) {
            player.sendMessage("<c>You can't eat any more of these, you're stuffed!");
            return;
        }
        setEaten(player, eaten + 1);
        TimedConsumables.consumeHeld(player, item);
        player.sendMessage("<a>You ate the cereal and permanently gained <6>+{} {}<a>!", FORTUNE_EACH, statistic.getDisplayName());
    }
}
