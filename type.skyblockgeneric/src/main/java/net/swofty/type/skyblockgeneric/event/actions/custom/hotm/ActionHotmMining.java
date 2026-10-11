package net.swofty.type.skyblockgeneric.event.actions.custom.hotm;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.glacite.GlaciteOre;
import net.swofty.type.skyblockgeneric.hotm.HotmAbilities;
import net.swofty.type.skyblockgeneric.hotm.HotmPowder;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.hotm.HotmSkyMallBuff;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering.MiningPetBonuses;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Iterator;

public class ActionHotmMining implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (Boolean.TRUE.equals(event.getPlayerPlaced())) return;
        SkyBlockPlayer player = event.getPlayer();
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(event.getPoint());
        RegionType regionType = region == null ? null : region.getType();
        GlaciteOre ore = GlaciteOre.of(event.getMaterial(), regionType);
        MineableBlock block = ore != null ? ore.getMineable() : MineableBlock.get(event.getMaterial().block());
        if (block == null) return;

        HotmAbilities.onBlockMined(player);
        if (ore != null) {
            mineGlacite(player, ore, event);
            return;
        }
        ItemStatistic specific = block.getBlockType().specificBlockFortune();
        if (specific == ItemStatistic.DWARVEN_METAL_FORTUNE) mineDwarvenMetal(player, block, event);
        if (specific == ItemStatistic.GEMSTONE_FORTUNE) mineGemstone(player, event);
    }

    private void mineDwarvenMetal(SkyBlockPlayer player, MineableBlock block, CustomBlockBreakEvent event) {
        if (block == MineableBlock.TITANIUM) {
            if (HotmService.skyMallBuff(player) == HotmSkyMallBuff.TITANIUM) {
                event.getDrops().forEach(drop -> drop.setAmount(drop.getAmount() * 5));
            }
            return;
        }
        long base = switch (block) {
            case MITHRIL_WEAK, MITHRIL_WEAK2 -> 1;
            case MITHRIL_STRONG -> 5;
            default -> 2;
        } + HotmService.baseBonus(player, HotmPowder.MITHRIL);
        HotmService.gainPowder(player, HotmPowder.MITHRIL, base);
        if (HotmService.hasPerk(player, "daily_powder")) awardDailyPowder(player);

        double chance = HotmService.perkValue(player, "titanium_insanium");
        if (chance > 0 && LootRoll.roll(player.getUuid(), ItemType.TITANIUM, chance)) {
            Iterator<SkyBlockItem> drops = event.getDrops().iterator();
            while (drops.hasNext()) {
                if (drops.next().getAttributeHandler().getPotentialType() == ItemType.MITHRIL) drops.remove();
            }
            event.getDrops().add(new SkyBlockItem(ItemType.TITANIUM, 1));
        }
    }

    private void mineGlacite(SkyBlockPlayer player, GlaciteOre ore, CustomBlockBreakEvent event) {
        if (ore.getMineable().getBlockType().specificBlockFortune() == ItemStatistic.GEMSTONE_FORTUNE) {
            mineGemstone(player, event);
            return;
        }
        HotmService.gainPowder(player, HotmPowder.GLACITE, ore.basePowder() + HotmService.baseBonus(player, HotmPowder.GLACITE));
        if (HotmService.claimDailyGlacite(player)) {
            player.sendMessage("<b><l>DAILY POWDER <r><7>You earned <b>{} Glacite Powder<7> for your first Glacite mined today.",
                    String.format("%,d", HotmService.dailyPowderAmount(player)));
        }
    }

    private void mineGemstone(SkyBlockPlayer player, CustomBlockBreakEvent event) {
        if (MiningPetBonuses.rollingMinerReady(player, System.currentTimeMillis())) {
            event.getDrops().forEach(drop -> drop.setAmount(drop.getAmount() * 2));
        }
        HotmService.gainPowder(player, HotmPowder.GEMSTONE, 1 + HotmService.baseBonus(player, HotmPowder.GEMSTONE));
    }

    private void awardDailyPowder(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        long today = HotmService.dailyBonusDay();
        if (data.getDailyPowderDay() == today) return;
        long bonus = HotmService.dailyPowderAmount(player);
        data.setDailyPowderDay(today);
        data.addMithrilPowder(bonus);
        HotmService.save(player, data);
        player.sendMessage("<2><b>DAILY POWDER <r><7>You earned <2>{} Mithril Powder<7> for your first Mithril of the day.", String.format("%,d", bonus));
    }
}
