package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.century.raffle.CenturyEvent;
import dev.daweapon.type.skyblockgeneric.century.raffle.CenturyRaffle;
import dev.daweapon.type.skyblockgeneric.century.raffle.RaffleAction;
import dev.daweapon.type.skyblockgeneric.century.raffle.RaffleAction.Trigger;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.FishCaughtEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.KuudraClearedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.TreeGiftReceivedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.TrophyFishCaughtEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

public class ActionCenturyRaffleTasks implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onKill(PlayerKilledSkyBlockMobEvent event) {
        if (!CenturyEvent.isActive()) return;
        CenturyRaffle.progress(event.getPlayer(), RaffleAction.kill(event.getKilledMob()));
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onFish(FishCaughtEvent event) {
        if (!CenturyEvent.isActive()) return;
        switch (event.getKind()) {
            case SEA_CREATURE -> CenturyRaffle.progress(event.getPlayer(), RaffleAction.of(Trigger.SEA_CREATURE, event.getId()));
            case TREASURE -> CenturyRaffle.progress(event.getPlayer(), RaffleAction.of(Trigger.TREASURE, event.getQuality()));
            default -> {
            }
        }
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onTrophy(TrophyFishCaughtEvent event) {
        if (!CenturyEvent.isActive()) return;
        CenturyRaffle.progress(event.getPlayer(), RaffleAction.of(Trigger.TROPHY_FISH, event.getFishId(), event.getTier().ordinal() + 1));
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onBreak(CustomBlockBreakEvent event) {
        if (!CenturyEvent.isActive() || Boolean.TRUE.equals(event.getPlayerPlaced())) return;
        CenturyRaffle.progress(event.getPlayer(), RaffleAction.of(Trigger.BLOCK_MINED));
        for (SkyBlockItem drop : event.getDrops()) {
            ItemType type = drop.getAttributeHandler().getPotentialType();
            if (type != null) CenturyRaffle.progress(event.getPlayer(), RaffleAction.of(Trigger.ITEM_MINED, type.name()));
        }
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onKuudra(KuudraClearedEvent event) {
        if (!CenturyEvent.isActive()) return;
        CenturyRaffle.progress(event.getPlayer(), RaffleAction.of(Trigger.KUUDRA, event.tier().name(), event.tier().ordinal()));
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onTreeGift(TreeGiftReceivedEvent event) {
        if (!CenturyEvent.isActive()) return;
        CenturyRaffle.progress(event.getPlayer(), RaffleAction.of(Trigger.TREE_GIFT, event.getTreeType()));
    }
}
