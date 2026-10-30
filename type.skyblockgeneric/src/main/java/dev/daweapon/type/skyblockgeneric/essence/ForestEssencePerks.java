package dev.daweapon.type.skyblockgeneric.essence;

import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AxeComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class ForestEssencePerks {
    public static final String TRAPPED = "trapped";
    public static final String AXED = "axed";
    public static final String TASTY = "tasty";

    private ForestEssencePerks() {
    }

    public static double value(SkyBlockPlayer player, String perkId) {
        return EssenceShops.shop(EssenceType.FOREST)
                .flatMap(shop -> shop.perks().stream().filter(perk -> perk.id().equals(perkId)).findFirst())
                .map(perk -> EssencePerks.value(player, perk))
                .orElse(0D);
    }

    public static double trapSpeedDiscount(SkyBlockPlayer player) {
        return value(player, TRAPPED) / 100D;
    }

    public static double berryMultiplier(SkyBlockPlayer player) {
        return 1 + value(player, TASTY) / 100D;
    }

    public static ItemStatistics axeBoost(SkyBlockItem item, SkyBlockPlayer player, ItemStatistics current) {
        if (item.isAir() || item.isNA() || !item.hasComponent(AxeComponent.class)) return ItemStatistics.empty();
        double percent = value(player, AXED);
        if (percent <= 0) return ItemStatistics.empty();
        ItemStatistics total = ItemStatistics.add(item.getAttributeHandler().getStatistics(), current);
        return ItemStatistics.multiply(total, percent / 100D);
    }
}
