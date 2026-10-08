package net.swofty.type.skyblockgeneric.event.actions.custom.spooky;

import net.swofty.type.skyblockgeneric.user.pets.PetAbilities;
import java.util.List;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.carnival.CarnivalPerk;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import net.swofty.type.skyblockgeneric.calendar.CalendarEvent;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.spookyfestival.SpookyFestivalCandyTally;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

// Universal candy drop during the Spooky Festival (Roadmap #19). Real Hypixel: ALL mobs get a
// baseline 10% chance to drop Green Candy per kill, which itself converts to Purple Candy at a
// base 10% rate plus 0.2% per mob level - both rates are boosted by Spooky Armor (+5% drop per piece, +5% "rare Candy"
// i.e. Purple conversion at full 4-piece set) and the Candy Talisman/Ring/Artifact/Relic line
// (+5/10/15/20% drop, highest tier only), both sourced from the Fear Mongerer shop tooltips.
// Bat Person Armor - a separate, unrelated pre-existing accessory line in this codebase - is not
// part of the Spooky Festival's boost per the sourced tooltips, so it's intentionally excluded.
//
// This intentionally hooks the existing global PlayerKilledSkyBlockMobEvent (fired from
// SkyBlockMob.kill() for every mob kill) rather than tagging specific mobs MobType.SPOOKY - real
// Hypixel's candy drop applies to ALL mobs during the festival, not just festival-specific mob
// variants (those are a separate, cosmetic "Spooky Mob" night-spawn layer not built this pass).
public class SpookyFestivalCandyHandler implements HypixelEventClass {

    private static final double CANDY_DROP_CHANCE = 0.10;
    private static final double PURPLE_CONVERSION_CHANCE = 0.10;
    private static final double SPOOKY_ARMOR_PIECE_BONUS = 0.05;
    private static final double SPOOKY_ARMOR_FULL_SET_BONUS = 0.05;
    private static final double PURPLE_CHANCE_PER_MOB_LEVEL = 0.002;

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerKilledSkyBlockMobEvent event) {
        if (!SkyBlockCalendar.getCurrentEvents().contains(CalendarEvent.SPOOKY_FESTIVAL)) return;

        SkyBlockPlayer player = event.getPlayer();
        int spookyPieces = countSpookyArmorPieces(player);
        double dropChance = CANDY_DROP_CHANCE + (spookyPieces * SPOOKY_ARMOR_PIECE_BONUS) + candyAccessoryBonus(player)
                + ElectionPerks.bonus(SkyBlockMayor.Perk.SWEET_BENEVOLENCE) / 100D;
        dropChance *= 1 + PetAbilities.value(player, ItemType.BAT_PET, "Candy Lover", 0) / 100D;
        ItemType forced = LootRoll.claim(player.getUuid(), List.of(ItemType.PURPLE_CANDY, ItemType.GREEN_CANDY));
        int candies = (int) dropChance + (ThreadLocalRandom.current().nextDouble() < dropChance % 1D ? 1 : 0);
        if (forced != null) candies = Math.max(1, candies);
        if (candies == 0) return;
        candies = Carnivals.boostedCount(player, CarnivalPerk.CANDY_SCAVENGER, candies);

        Integer mobLevel = event.getKilledMob().getLevel();
        double purpleChance = PURPLE_CONVERSION_CHANCE
                + (mobLevel == null ? 0 : mobLevel * PURPLE_CHANCE_PER_MOB_LEVEL)
                + (spookyPieces == 4 ? SPOOKY_ARMOR_FULL_SET_BONUS : 0)
                + Carnivals.perkValue(player, CarnivalPerk.PURPLE_VOGUE) / 100D;
        for (int i = 0; i < candies; i++) {
            boolean purple = i == 0 && forced != null ? forced == ItemType.PURPLE_CANDY
                    : ThreadLocalRandom.current().nextDouble() < purpleChance;
            giveCandy(player, purple);
        }
    }

    private static void giveCandy(SkyBlockPlayer player, boolean purple) {
        ItemType candyType = purple ? ItemType.PURPLE_CANDY : ItemType.GREEN_CANDY;

        player.addAndUpdateItem(new SkyBlockItem(candyType));
        player.sendMessage("<7>You received <a>1x {}<7>!", new SkyBlockItem(candyType).getDisplayName());

        SpookyFestivalCandyTally.record(player, purple);
        double points = purple ? SpookyFestivalCandyTally.PURPLE_POINTS : SpookyFestivalCandyTally.GREEN_POINTS;
        String leaderboardKey = "spooky_festival_" + SkyBlockCalendar.getYear();
        Double currentScore = LeaderboardService.getPlayerScore(leaderboardKey, player.getUuid());
        double newScore = (currentScore == null ? 0 : currentScore) + points;
        LeaderboardService.updateScoreAsync(leaderboardKey, player.getUuid(), newScore);
    }

    private static int countSpookyArmorPieces(SkyBlockPlayer player) {
        ItemType[] spookySet = {ItemType.SPOOKY_HELMET, ItemType.SPOOKY_CHESTPLATE, ItemType.SPOOKY_LEGGINGS, ItemType.SPOOKY_BOOTS};
        SkyBlockItem[] worn = player.getArmor();
        int count = 0;
        for (int i = 0; i < worn.length; i++) {
            if (worn[i].getAttributeHandler().getPotentialType() == spookySet[i]) count++;
        }
        return count;
    }

    private static double candyAccessoryBonus(SkyBlockPlayer player) {
        boolean hasRelic = false, hasArtifact = false, hasRing = false, hasTalisman = false;
        for (SkyBlockItem accessory : player.getAccessoryBag().getUniqueAccessories()) {
            ItemType type = accessory.getAttributeHandler().getPotentialType();
            if (type == ItemType.CANDY_RELIC) hasRelic = true;
            else if (type == ItemType.CANDY_ARTIFACT) hasArtifact = true;
            else if (type == ItemType.CANDY_RING) hasRing = true;
            else if (type == ItemType.CANDY_TALISMAN) hasTalisman = true;
        }
        if (hasRelic) return 0.20;
        if (hasArtifact) return 0.15;
        if (hasRing) return 0.10;
        if (hasTalisman) return 0.05;
        return 0;
    }
}
