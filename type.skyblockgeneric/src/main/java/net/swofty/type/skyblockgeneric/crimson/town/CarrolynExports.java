package net.swofty.type.skyblockgeneric.crimson.town;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Carrolyn's exportations: 3,000 of a crop, delivered in any number of trips, for +5 Health,
 * +12 of the crop's Fortune and +5 SkyBlock XP each.
 */
public final class CarrolynExports {
    public static final int REQUIRED = 3_000;
    public static final double HEALTH = 5;
    public static final double FORTUNE = 12;
    public static final int SKYBLOCK_XP = 5;

    private CarrolynExports() {
    }

    public record Delivery(int given, int total, boolean completed) {
    }

    private static String key(CarrolynExport export) {
        return "carrolyn:" + export.name();
    }

    public static int progress(SkyBlockPlayer player, CarrolynExport export) {
        return (int) Math.min(REQUIRED, CrimsonSideQuests.get(player, key(export)));
    }

    public static boolean isComplete(SkyBlockPlayer player, CarrolynExport export) {
        return progress(player, export) >= REQUIRED;
    }

    /** Hands over as many of the crop as the player carries, up to what is still needed. */
    public static Delivery deliver(SkyBlockPlayer player, CarrolynExport export) {
        ItemType type = export.itemType();
        int before = progress(player, export);
        if (type == null || before >= REQUIRED) return new Delivery(0, before, false);
        int given = Math.min(REQUIRED - before, player.getAmountInInventory(type));
        if (given <= 0) return new Delivery(0, before, false);
        player.removeItemFromPlayer(type, given);
        int total = before + given;
        CrimsonSideQuests.set(player, key(export), total);
        boolean completed = total >= REQUIRED;
        if (completed) syncSkyBlockXP(player);
        return new Delivery(given, total, completed);
    }

    public static int completed(SkyBlockPlayer player) {
        int count = 0;
        for (CarrolynExport export : CarrolynExport.values()) {
            if (isComplete(player, export)) count++;
        }
        return count;
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        double health = 0;
        for (CarrolynExport export : CarrolynExport.values()) {
            if (!isComplete(player, export)) continue;
            health += HEALTH;
            builder.withBase(export.fortune(), FORTUNE);
        }
        if (health > 0) builder.withBase(ItemStatistic.HEALTH, health);
        return builder.build();
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.CARROLYNS_EXPORTABLE_CROPS,
                completed(player) * SKYBLOCK_XP);
    }

    public static void maxOut(SkyBlockPlayer player) {
        for (CarrolynExport export : CarrolynExport.values()) {
            CrimsonSideQuests.set(player, key(export), REQUIRED);
        }
        syncSkyBlockXP(player);
    }
}
