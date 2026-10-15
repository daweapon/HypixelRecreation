package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.fiesta.MiningFiesta;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.region.mining.MineableBlock;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
