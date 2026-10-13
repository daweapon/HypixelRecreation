package net.swofty.type.skyblockgeneric.fiesta;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.carnival.CarnivalPerk;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.hunting.AttributeId;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

public final class MiningFiesta {
    public static final double BREAKING_POWER_PER_XP = 5_000;
    private static final String REFINED_LUCK = "R81";
    private static final String GLOSSY_FORTUNE = "E60";

    private MiningFiesta() {
    }

    public static boolean isActive() {
        return ElectionPerks.isActive(SkyBlockMayor.Perk.MINING_FIESTA);
    }

    public static boolean appliesTo(SkyBlockPlayer player) {
        return isActive() && !HypixelConst.isIslandServer();
    }

    public static @Nullable ItemType rewardFor(MineableBlock block) {
        ItemStatistic specific = block.getBlockType().specificBlockFortune();
        if (specific == ItemStatistic.ORE_FORTUNE || specific == ItemStatistic.DWARVEN_METAL_FORTUNE) return ItemType.REFINED_MINERAL;
        if (specific == ItemStatistic.GEMSTONE_FORTUNE) return ItemType.GLOSSY_GEMSTONE;
        return null;
    }

    public static double chancePercent(SkyBlockPlayer player, MineableBlock block, ItemType reward) {
        String shard = reward == ItemType.REFINED_MINERAL ? REFINED_LUCK : GLOSSY_FORTUNE;
        double multipliers = AttributeEffectService.value(player.getHuntingData(), AttributeId.parse(shard)) / 100D
                + Carnivals.perkValue(player, CarnivalPerk.REFINED_MIND) / 100D;
        return block.getMiningPowerRequirement() / 100D * (1 + multipliers);
    }

    public static boolean rollReward(SkyBlockPlayer player, MineableBlock block, ItemType reward) {
        return LootRoll.roll(player.getUuid(), reward, chancePercent(player, block, reward));
    }

    public static void recordOre(SkyBlockPlayer player, MineableBlock block) {
        player.getSkyBlockExperience().addSourceXP(SkyBlockXPSource.MINING_FIESTA,
                block.getMiningPowerRequirement() / BREAKING_POWER_PER_XP);
    }
}
