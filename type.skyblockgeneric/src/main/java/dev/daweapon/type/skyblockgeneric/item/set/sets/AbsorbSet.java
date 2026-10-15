package dev.daweapon.type.skyblockgeneric.item.set.sets;

import net.minestom.server.instance.block.Block;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.set.impl.PlayerArmorSet;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsorbSet implements PlayerArmorSet {
    public static double bonusDefense(double wornPiecesDefense, boolean inWater) {
        return inWater ? Math.max(0, wornPiecesDefense) : 0;
    }

    @Override
    public String getName() {
        return "Absorb";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of("<9>Doubles<7> your <a>❈ Defense<7> while in", "<7>water."));
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player) {
        double bonus = bonusDefense(wornDefense(player), inWater(player));
        return bonus == 0 ? ItemStatistics.empty() : ItemStatistics.builder().withBase(ItemStatistic.DEFENSE, bonus).build();
    }

    private static boolean inWater(SkyBlockPlayer player) {
        return player.getInstance() != null && player.getInstance().getBlock(player.getPosition()).compare(Block.WATER);
    }

    private static double wornDefense(SkyBlockPlayer player) {
        double total = 0;
        for (var stack : List.of(player.getHelmet(), player.getChestplate(), player.getLeggings(), player.getBoots())) {
            if (stack.isAir()) continue;
            total += new SkyBlockItem(stack).getAttributeHandler().getStatistics().getOverall(ItemStatistic.DEFENSE);
        }
        return total;
    }
}
