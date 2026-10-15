package dev.daweapon.type.skyblockgeneric.accessory.hatcessory;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryFamilies;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryFamily;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Hatcessories worn as a helmet: the hat's own stats plus Party Time, +1 Intelligence for every
 * SkyBlock year the profile has existed. In the Accessory Bag they count like any accessory.
 */
public final class Hatcessories {
    public static final String FAMILY_ID = "HATCESSORIES";
    public static final long SKYBLOCK_YEAR_MILLIS = 124L * 60 * 60 * 1000;

    private Hatcessories() {
    }

    public static boolean isHatcessory(@Nullable ItemType type) {
        if (type == null) return false;
        AccessoryFamily family = AccessoryFamilies.get().familyOf(type.name());
        return family != null && FAMILY_ID.equals(family.id());
    }

    public static ItemStatistics wornStatistics(SkyBlockPlayer player, @Nullable SkyBlockItem helmet) {
        if (helmet == null || helmet.isAir() || !isHatcessory(helmet.getAttributeHandler().getPotentialType()))
            return ItemStatistics.empty();
        ItemStatistics partyTime = ItemStatistics.builder()
                .withBase(ItemStatistic.INTELLIGENCE, (double) profileYears(player)).build();
        return ItemStatistics.add(helmet.getAttributeHandler().getStatistics(), partyTime);
    }

    public static long profileYears(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return 0;
        long created = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CREATED, DatapointLong.class).getValue();
        return yearsSince(created, System.currentTimeMillis());
    }

    public static long yearsSince(long createdMillis, long nowMillis) {
        if (createdMillis <= 0 || nowMillis <= createdMillis) return 0;
        return (nowMillis - createdMillis) / SKYBLOCK_YEAR_MILLIS;
    }
}
