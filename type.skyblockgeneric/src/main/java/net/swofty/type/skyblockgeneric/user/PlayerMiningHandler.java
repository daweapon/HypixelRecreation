package net.swofty.type.skyblockgeneric.user;

import net.minestom.server.instance.block.Block;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.glacite.ColdService;
import net.swofty.type.skyblockgeneric.hotm.HotmAbilities;
import net.swofty.type.skyblockgeneric.hotm.HotmStatistics;
import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.ValueUpdateEvent;
import net.swofty.type.skyblockgeneric.event.value.events.MiningValueUpdateEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering.GatheringPetBonuses;
import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import net.swofty.type.skyblockgeneric.region.mining.handler.SkyBlockMiningHandler;

class PlayerMiningHandler {
    private final SkyBlockPlayer player;

    PlayerMiningHandler(SkyBlockPlayer player) {
        this.player = player;
    }

    double getTimeToMine(SkyBlockItem item, Block b) {
        if (player.getRegion() == null) return -1;
        MineableBlock block = MineableBlock.get(b, player.getRegion().getType());
        if (block == null) return -1;

        SkyBlockMiningHandler handler = block.getMiningHandler();

        // Check if block breaks instantly
        if (handler.breaksInstantly()) {
            return 0;
        }

        // Check if tool can break this block
        if (!handler.canToolBreak(item)) {
            return -1;
        }

        // Check breaking power requirement
        if (handler.getMiningPowerRequirement() > item.getAttributeHandler().getBreakingPower() + HotmAbilities.breakingPowerBonus(player)) {
            return -1;
        }

        if (net.swofty.type.skyblockgeneric.drill.DrillFuel.isEmpty(item)) {
            net.swofty.type.skyblockgeneric.drill.DrillFuel.warnEmpty(player);
            return -1;
        }

        // Handle vanilla-like fixed break times (e.g., logs with axes)
        if (handler.usesVanillaBreakTime()) {
            return handler.getBreakTimeForTool(item);
        }

        if (handler.getStrength() > 0) {
            // Use handler's speed statistic
            double speed = player.getStatistics().allStatistics().getOverall(handler.getSpeedStatistic());
            if (handler.getSpeedStatistic() == ItemStatistic.MINING_SPEED) {
                speed += GatheringPetBonuses.miningSpeed(player, block);
                speed += HotmStatistics.miningSpeedFor(player, block);
                speed += net.swofty.type.skyblockgeneric.item.tool.ToolPassives.miningSpeedBonus(item,
                        block.getBlockType().specificBlockFortune() == ItemStatistic.GEMSTONE_FORTUNE);
            }
            if (handler.getSpeedStatistic() == ItemStatistic.MINING_SPEED) {
                speed *= ColdService.miningSpeedMultiplier(player);
            }
            double time = Math.round(handler.getStrength() * 30) / (Math.max(speed, 1));
            ValueUpdateEvent event = new MiningValueUpdateEvent(player, time, item);
            SkyBlockValueEvent.callValueUpdateEvent(event);
            time = (double) event.getValue();

            return time;
        }

        return -1;
    }
}
