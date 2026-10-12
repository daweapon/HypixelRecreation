package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.impl.PlayerArmorSet;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class FairyOutfitSet implements PlayerArmorSet {
    public static double health(int fairySouls) {
        return Math.max(0, fairySouls);
    }

    @Override
    public String getName() {
        return "Fairy's Outfit";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of("<7>Gain <c>1❤ <7>per Fairy Soul."));
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, health(player.getFairySouls().getAllFairySouls().size()))
                .build();
    }
}
