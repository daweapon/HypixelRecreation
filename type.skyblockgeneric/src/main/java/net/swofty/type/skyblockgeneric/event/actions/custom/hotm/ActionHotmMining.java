package net.swofty.type.skyblockgeneric.event.actions.custom.hotm;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.hotm.HotmAbilities;
import net.swofty.type.skyblockgeneric.hotm.HotmPowder;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.hotm.HotmSkyMallBuff;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Iterator;

public class ActionHotmMining implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (Boolean.TRUE.equals(event.getPlayerPlaced())) return;
        SkyBlockPlayer player = event.getPlayer();
        MineableBlock block = MineableBlock.get(event.getMaterial().block());
        if (block == null) return;

        HotmAbilities.onBlockMined(player);
        ItemStatistic specific = block.getBlockType().specificBlockFortune();
        if (specific == ItemStatistic.DWARVEN_METAL_FORTUNE) mineDwarvenMetal(player, block, event);
        if (specific == ItemStatistic.GEMSTONE_FORTUNE) mineGemstone(player);
    }

    private void mineDwarvenMetal(SkyBlockPlayer player, MineableBlock block, CustomBlockBreakEvent event) {
        if (block == MineableBlock.TITANIUM) {
            if (HotmService.skyMallBuff(player) == HotmSkyMallBuff.TITANIUM) {
                event.getDrops().forEach(drop -> drop.setAmount(drop.getAmount() * 5));
            }
            return;
        }
        int core = HotmService.data(player).getCoreLevel();
        long base = switch (block) {
            case MITHRIL_WEAK, MITHRIL_WEAK2 -> 1;
            case MITHRIL_STRONG -> 5;
            default -> 2;
        } + (core >= 4 ? 1 : 0);
        HotmService.gainPowder(player, HotmPowder.MITHRIL, base);
        awardDailyPowder(player);

        double chance = HotmService.perkValue(player, "titanium_insanium");
        if (chance > 0 && LootRoll.roll(player.getUuid(), ItemType.TITANIUM, chance)) {
            Iterator<SkyBlockItem> drops = event.getDrops().iterator();
            while (drops.hasNext()) {
                if (drops.next().getAttributeHandler().getPotentialType() == ItemType.MITHRIL) drops.remove();
            }
            event.getDrops().add(new SkyBlockItem(ItemType.TITANIUM, 1));
        }
    }

    private void mineGemstone(SkyBlockPlayer player) {
        int core = HotmService.data(player).getCoreLevel();
        HotmService.gainPowder(player, HotmPowder.GEMSTONE, 1 + (core >= 6 ? 2 : 0));
    }

    private void awardDailyPowder(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        long today = HotmService.dailyBonusDay();
        if (data.getDailyPowderDay() == today) return;
        long bonus = (long) HotmService.DAILY_POWDER_PER_TIER * data.getTier();
        data.setDailyPowderDay(today);
        data.addMithrilPowder(bonus);
        HotmService.save(player, data);
        player.sendMessage("<2><b>DAILY POWDER <r><7>You earned <2>{} Mithril Powder<7> for your first Mithril of the day.", String.format("%,d", bonus));
    }
}
