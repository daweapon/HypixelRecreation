package net.swofty.type.skyblockgeneric.gifts;

import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class ReindeerPet {
    public static final double INFUSED_FISHING_SPEED_PER_LEVEL = 0.75;
    public static final double INFUSED_TREASURE_CHANCE = 5;
    public static final double SNOW_POWER_PER_LEVEL = 0.1;
    public static final double ICY_WIND_PER_LEVEL = 0.2;

    private ReindeerPet() {
    }

    public static boolean isReindeer(SkyBlockItem pet) {
        return pet != null && pet.getAttributeHandler().getPotentialType() == ItemType.REINDEER_PET;
    }

    public static double snowPowerPercent(SkyBlockPlayer player) {
        return level(player) * SNOW_POWER_PER_LEVEL;
    }

    public static double icyWindPercent(SkyBlockPlayer player) {
        return level(player) * ICY_WIND_PER_LEVEL;
    }

    public static ItemStatistics infused(double fishingSpeed) {
        if (HypixelConst.getTypeLoader().getType() != ServerType.SKYBLOCK_JERRYS_WORKSHOP) return ItemStatistics.empty();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.FISHING_SPEED, fishingSpeed)
                .withBase(ItemStatistic.TREASURE_CHANCE, INFUSED_TREASURE_CHANCE)
                .build();
    }

    private static int level(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return 0;
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        if (!isReindeer(pet)) return 0;
        Rarity rarity = pet.getAttributeHandler().getRarity();
        return pet.getAttributeHandler().getPetData().getAsLevel(rarity);
    }
}
