package dev.daweapon.type.skyblockgeneric.darkauction;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

public final class MidasPricePaid {
    public static final long MIDAS_SWORD_CAP = 50_000_000L;

    private MidasPricePaid() {}

    public static long of(SkyBlockItem item) {
        return item.getAttributeHandler().getDarkAuctionPrice();
    }

    public static void set(SkyBlockItem item, long price) {
        item.getAttributeHandler().setDarkAuctionPrice(price);
        if (item.getItemType() != ItemType.MIDAS_SWORD) return;
        double greed = DarkAuctionHandler.calculateGreedBonus(price);
        item.getAttributeHandler().setExtraDynamicStatistics(ItemStatistics.builder()
                .withBase(ItemStatistic.DAMAGE, greed)
                .withBase(ItemStatistic.STRENGTH, greed)
                .build());
    }
}
