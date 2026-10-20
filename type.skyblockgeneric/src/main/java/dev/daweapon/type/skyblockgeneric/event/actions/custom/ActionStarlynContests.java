package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.FishCaughtEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.TreeGiftReceivedEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynContests;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynTree;

public class ActionStarlynContests implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onTreeGift(TreeGiftReceivedEvent event) {
        StarlynContests.recordTreeGift(event.getPlayer());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onLogs(CustomBlockBreakEvent event) {
        for (SkyBlockItem drop : event.getDrops()) {
            StarlynTree tree = StarlynTree.ofLog(drop.getAttributeHandler().getPotentialType());
            if (tree != null) StarlynContests.recordLogs(event.getPlayer(), tree, drop.getAmount());
        }
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onShard(ShardCaughtEvent event) {
        if (event.getSource() != ShardCaughtEvent.Source.CHARM && event.getSource() != ShardCaughtEvent.Source.HUNTRAP) return;
        StarlynContests.recordHunt(event.getPlayer(), event.getShard());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onFish(FishCaughtEvent event) {
        StarlynContests.recordFishing(event.getPlayer(), event.getKind(), event.getQuality());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onKill(PlayerKilledSkyBlockMobEvent event) {
        StarlynContests.recordKill(event.getPlayer());
    }
}
