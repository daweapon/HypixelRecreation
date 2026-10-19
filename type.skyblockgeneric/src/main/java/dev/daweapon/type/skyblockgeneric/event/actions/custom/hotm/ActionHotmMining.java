package dev.daweapon.type.skyblockgeneric.event.actions.custom.hotm;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.glacite.GlaciteOre;
import dev.daweapon.type.skyblockgeneric.hotm.FrontLoaded;
import dev.daweapon.type.skyblockgeneric.hotm.HotmAbilities;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.hotm.HotmSkyMallBuff;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.MiningPetBonuses;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.miningevent.MithrilGourmandEvent;
import dev.daweapon.type.skyblockgeneric.miningevent.RandomMiningEvents;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeLoadout;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.region.mining.MineableBlock;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Iterator;

import dev.daweapon.type.skyblockgeneric.carnival.CarnivalPerk;
import dev.daweapon.type.skyblockgeneric.carnival.Carnivals;
import dev.daweapon.type.skyblockgeneric.fiesta.MiningFiesta;

public class ActionHotmMining implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (Boolean.TRUE.equals(event.getPlayerPlaced())) return;
        SkyBlockPlayer player = event.getPlayer();
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(event.getPoint());
        RegionType regionType = region == null ? null : region.getType();
        GlaciteOre ore = GlaciteOre.of(event.getMaterial(), regionType);
        MineableBlock block = ore != null ? ore.getMineable() : event.getMaterial().block() == null ? null : MineableBlock.get(event.getMaterial().block(), regionType);
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
        boolean titanium = block == MineableBlock.TITANIUM;
        RandomMiningEvents.onOreMined(player, event.getPoint(), !titanium);
        if (titanium) {
            long reforgePowder = ReforgeLoadout.titaniumMithrilPowder(player);
            if (reforgePowder > 0) HotmService.gainPowder(player, HotmPowder.MITHRIL, reforgePowder);
            RandomMiningEvents.applyFallenStar(player, event.getPoint(), event.getDrops(), false, 0);
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
        long gained = HotmService.gainPowder(player, HotmPowder.MITHRIL, base);
        RandomMiningEvents.applyFallenStar(player, event.getPoint(), event.getDrops(), true, gained);
        MithrilGourmandEvent gourmand = MithrilGourmandEvent.active();
        if (gourmand != null) gourmand.rollDrop(player, event.getPoint(), event.getDrops());
        if (HotmService.hasPerk(player, "daily_powder")) awardDailyPowder(player);

        double chance = HotmService.perkValue(player, "titanium_insanium");
        if (MiningFiesta.appliesTo(player)) chance += Carnivals.perkValue(player, CarnivalPerk.TITANIUM_TENACITY);
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
        long base = 1 + HotmService.baseBonus(player, HotmPowder.GEMSTONE);
        if (FrontLoaded.active(player)) {
            base = Math.round(base * FrontLoaded.POWDER_MULTIPLIER);
            FrontLoaded.consume(player);
        }
        HotmService.gainPowder(player, HotmPowder.GEMSTONE, base);
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
