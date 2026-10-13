package net.swofty.type.skyblockgeneric.event.actions.custom;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.fiesta.MiningFiesta;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionMiningFiestaDrops implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (Boolean.TRUE.equals(event.getPlayerPlaced())) return;
        SkyBlockPlayer player = event.getPlayer();
        if (!MiningFiesta.appliesTo(player)) return;

        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(event.getPoint());
        RegionType regionType = region == null ? null : region.getType();
        MineableBlock block = MineableBlock.get(event.getMaterial().block(), regionType);
        if (block == null) return;

        ItemType reward = MiningFiesta.rewardFor(block);
        if (reward == null) return;

        MiningFiesta.recordOre(player, block);
        if (!MiningFiesta.rollReward(player, block, reward)) return;
        if (player.canInsertItemIntoSacks(reward, 1)) {
            player.getSackItems().increase(reward, 1);
        } else {
            player.addAndUpdateItem(new SkyBlockItem(reward, 1));
        }
    }
}
