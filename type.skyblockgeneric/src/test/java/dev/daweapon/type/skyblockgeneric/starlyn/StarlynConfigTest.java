package dev.daweapon.type.skyblockgeneric.starlyn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.event.custom.FishCaughtEvent;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StarlynConfigTest {
    private static final Path DIRECTORY = Path.of("../configuration/skyblock/starlyn");
    private final StarlynConfig agatha = StarlynConfig.load(StarlynSister.AGATHA, DIRECTORY);
    private final StarlynConfig miria = StarlynConfig.load(StarlynSister.MIRIA, DIRECTORY);

    private static StarlynConfig.Fortune fortune(StarlynConfig config, String id) {
        return (StarlynConfig.Fortune) config.entry(id);
    }

    @Test
    void agathaBracketThresholdsMatchTheWiki() {
        assertEquals(List.of(25, 250, 1000, 2000, 3000, 5000, 8000, 12000),
                agatha.brackets().stream().map(StarlynConfig.BracketReward::points).toList());
        assertNull(agatha.bracketFor(24));
        assertEquals(StarlynBracket.COMMON, agatha.bracketFor(25).bracket());
        assertEquals(StarlynBracket.EPIC, agatha.bracketFor(2999.9).bracket());
        assertEquals(StarlynBracket.LEGENDARY, agatha.bracketFor(3000).bracket());
        assertEquals(StarlynBracket.SPECIAL, agatha.bracketFor(1_000_000).bracket());
    }

    @Test
    void miriaBracketThresholdsMatchTheWiki() {
        assertEquals(List.of(50, 250, 1000, 2500, 5000, 10000, 15000, 20000),
                miria.brackets().stream().map(StarlynConfig.BracketReward::points).toList());
        assertNull(miria.bracketFor(49));
        assertEquals(StarlynBracket.DIVINE, miria.bracketFor(15_000).bracket());
    }

    @Test
    void fiveMainAndThreeHiddenBrackets() {
        assertEquals(5, List.of(StarlynBracket.values()).stream().filter(bracket -> !bracket.isHidden()).count());
        assertEquals(3, List.of(StarlynBracket.values()).stream().filter(StarlynBracket::isHidden).count());
        assertEquals(1, StarlynBracket.COMMON.triggerValue());
        assertEquals(5, StarlynBracket.LEGENDARY.triggerValue());
        assertTrue(StarlynBracket.MYTHIC.atLeast(StarlynBracket.LEGENDARY));
    }

    @Test
    void agathaRewardTable() {
        StarlynConfig.BracketReward common = agatha.reward(StarlynBracket.COMMON);
        assertEquals(10, common.coupons());
        assertEquals(2500, common.whispers());
        assertEquals(10, common.essence());
        assertEquals(10_000, common.foragingXp());
        assertEquals(100, common.hotfXp());
        assertEquals(15, common.bits());
        assertEquals(0, common.prizeChance());
        StarlynConfig.BracketReward epic = agatha.reward(StarlynBracket.EPIC);
        assertEquals(10, epic.prizeChance());
        assertEquals(10_500, epic.whispers());
        StarlynConfig.BracketReward special = agatha.reward(StarlynBracket.SPECIAL);
        assertEquals(45, special.coupons());
        assertEquals(20_000, special.whispers());
        assertEquals(80, special.essence());
        assertEquals(120, special.bits());
        assertEquals(20, special.prizeChance());
        for (StarlynConfig.BracketReward reward : agatha.brackets()) assertNull(reward.safariTicket());
    }

    @Test
    void miriaRewardTableAndSafariTickets() {
        StarlynConfig.BracketReward common = miria.reward(StarlynBracket.COMMON);
        assertEquals(3000, common.whispers());
        assertEquals(20, common.essence());
        assertEquals(15_000, common.foragingXp());
        assertEquals(150, common.hotfXp());
        assertNull(common.safariTicket());
        assertEquals("BASIC_SAFARI_TICKET", miria.reward(StarlynBracket.UNCOMMON).safariTicket());
        assertEquals("ECONOMY_SAFARI_TICKET", miria.reward(StarlynBracket.RARE).safariTicket());
        assertEquals("PREMIUM_SAFARI_TICKET", miria.reward(StarlynBracket.EPIC).safariTicket());
        assertEquals("FIRST_CLASS_SAFARI_TICKET", miria.reward(StarlynBracket.SPECIAL).safariTicket());
        assertEquals(10, miria.reward(StarlynBracket.EPIC).prizeChance());
        assertEquals(36_000, miria.reward(StarlynBracket.SPECIAL).whispers());
        assertEquals(75_000, miria.reward(StarlynBracket.LEGENDARY).foragingXp());
    }

    @Test
    void milestoneThresholdsAndWhisperBuffs() {
        for (StarlynConfig config : List.of(agatha, miria)) {
            assertEquals(List.of(0, 100, 500, 1500, 3000), config.milestones().stream().map(StarlynConfig.Milestone::threshold).toList());
            assertEquals(List.of(0, 50, 100, 150, 200), config.milestones().stream().map(StarlynConfig.Milestone::whisperBuffPercent).toList());
            assertEquals(List.of(0, 100, 250, 500, 1000).size(), config.milestones().size());
            assertEquals(1, config.milestoneFor(0));
            assertEquals(1, config.milestoneFor(99));
            assertEquals(2, config.milestoneFor(100));
            assertEquals(3, config.milestoneFor(500));
            assertEquals(4, config.milestoneFor(1500));
            assertEquals(5, config.milestoneFor(3000));
            assertEquals(5, config.milestoneFor(99_999));
        }
        assertEquals(List.of(0L, 2_500L, 10_000L, 20_000L, 30_000L), agatha.milestones().stream().map(StarlynConfig.Milestone::foragingXp).toList());
        assertEquals(List.of(0, 100, 250, 500, 1000), agatha.milestones().stream().map(StarlynConfig.Milestone::essence).toList());
        assertEquals(List.of(0L, 25_000L, 50_000L, 75_000L, 100_000L), miria.milestones().stream().map(StarlynConfig.Milestone::foragingXp).toList());
        assertEquals(List.of(0, 100, 250, 500, 750), miria.milestones().stream().map(StarlynConfig.Milestone::essence).toList());
        assertEquals(1.5, StarlynRewards.whisperBuff(StarlynSister.AGATHA, 2));
        assertEquals(3.0, StarlynRewards.whisperBuff(StarlynSister.MIRIA, 5));
        assertEquals(1.0, StarlynRewards.whisperBuff(StarlynSister.AGATHA, 1));
    }

    @Test
    void maxingFigFortuneCostsTheWikiTotals() {
        StarlynConfig.Fortune fig = fortune(agatha, "fig_fortune");
        StarlynCost max = fig.totalCost(50);
        assertEquals(4750, max.coupons());
        assertEquals(10, max.prizes());
        assertEquals(2148, max.amountOf(ItemType.ENCHANTED_FIG_LOG));
        assertEquals(286, max.amountOf(ItemType.FIGSTONE));
        assertEquals(200, fig.fortune(50));
        StarlynCost atHundredSixty = fig.totalCost(40);
        assertEquals(3500, atHundredSixty.coupons());
        assertEquals(186, atHundredSixty.amountOf(ItemType.FIGSTONE));
        assertEquals(0, atHundredSixty.prizes());
        assertEquals(1500, fig.totalCost(24).coupons());
        assertEquals(26, fig.totalCost(24).amountOf(ItemType.FIGSTONE));
        StarlynCost atSeventySix = fig.totalCost(19);
        assertEquals(950, atSeventySix.coupons());
        assertEquals(2148, atSeventySix.amountOf(ItemType.ENCHANTED_FIG_LOG));
        assertEquals(0, atSeventySix.amountOf(ItemType.FIGSTONE));
    }

    @Test
    void maxingMangroveAndHelixFortuneCostTheWikiTotals() {
        StarlynCost mangrove = fortune(agatha, "mangrove_fortune").totalCost(50);
        assertEquals(4750, mangrove.coupons());
        assertEquals(10, mangrove.prizes());
        assertEquals(2148, mangrove.amountOf(ItemType.ENCHANTED_MANGROVE_LOG));
        assertEquals(286, mangrove.amountOf(ItemType.MANGCORE));
        StarlynCost helix = fortune(miria, "helix_fortune").totalCost(50);
        assertEquals(4750, helix.coupons());
        assertEquals(10, helix.prizes());
        assertEquals(2148, helix.amountOf(ItemType.ENCHANTED_HELIX_LOG));
        assertEquals(286, helix.amountOf(ItemType.HELIXIS));
        assertEquals(dev.daweapon.commons.skyblock.statistics.ItemStatistic.HELIX_FORTUNE, fortune(miria, "helix_fortune").statistic());
    }

    @Test
    void bothFortunesTogetherMatchTheTrivia() {
        StarlynCost both = fortune(agatha, "fig_fortune").totalCost(50).plus(fortune(agatha, "mangrove_fortune").totalCost(50));
        assertEquals(9500, both.coupons());
        assertEquals(20, both.prizes());
    }

    @Test
    void fortuneLevelsFollowTheWikiRows() {
        StarlynConfig.Fortune fig = fortune(agatha, "fig_fortune");
        assertEquals(new StarlynCost(5, 0, java.util.Map.of(ItemType.ENCHANTED_FIG_LOG, 2)), fig.cost(1));
        assertEquals(new StarlynCost(100, 0, java.util.Map.of(ItemType.FIGSTONE, 3)), fig.cost(20));
        assertEquals(new StarlynCost(120, 0, java.util.Map.of(ItemType.FIGSTONE, 8)), fig.cost(24));
        assertEquals(new StarlynCost(125, 0, java.util.Map.of(ItemType.FIGSTONE, 10)), fig.cost(25));
        assertEquals(new StarlynCost(125, 0, java.util.Map.of(ItemType.FIGSTONE, 10)), fig.cost(40));
        assertEquals(new StarlynCost(125, 1, java.util.Map.of(ItemType.FIGSTONE, 10)), fig.cost(41));
        assertEquals(4, fig.perLevel());
        assertEquals(50, fig.maxLevel());
    }

    @Test
    void agathasPowerTiers() {
        StarlynConfig.Power power = (StarlynConfig.Power) agatha.entry("power");
        assertEquals(List.of(20, 30, 40, 50, 60), power.tiers().stream().map(StarlynConfig.PowerTier::coupons).toList());
        assertEquals(List.of(5.0, 10.0, 15.0, 20.0, 25.0), power.tiers().stream().map(StarlynConfig.PowerTier::sweep).toList());
        assertEquals(List.of(1, 2, 3, 4, 5), power.tiers().stream().map(StarlynConfig.PowerTier::milestone).toList());
        assertEquals(200, power.tiers().stream().mapToInt(StarlynConfig.PowerTier::coupons).sum());
    }

    @Test
    void personalBestCostsAndMilestones() {
        StarlynConfig.PersonalBest fig = (StarlynConfig.PersonalBest) agatha.entry("fig_personal_best");
        StarlynConfig.PersonalBest mangrove = (StarlynConfig.PersonalBest) agatha.entry("mangrove_personal_best");
        StarlynConfig.PersonalBest helix = (StarlynConfig.PersonalBest) miria.entry("helix_personal_best");
        assertEquals(12, fig.coupons());
        assertEquals(1, fig.milestone());
        assertEquals(24, mangrove.coupons());
        assertEquals(3, mangrove.milestone());
        assertEquals(36, helix.coupons());
        assertEquals(0.0, StarlynPersonalBests.sweepPercent(99));
        assertEquals(10.0, StarlynPersonalBests.sweepPercent(100_000));
        assertEquals(10.0, StarlynPersonalBests.sweepPercent(500_000));
        assertEquals(1.23, StarlynPersonalBests.sweepPercent(12_345), 1e-9);
    }

    @Test
    void levelCapsAndPrizeTotals() {
        StarlynConfig.LevelCap agathaCaps = (StarlynConfig.LevelCap) agatha.entry("level_cap");
        assertEquals(List.of(250, 500), agathaCaps.tiers().stream().map(tier -> tier.cost().coupons()).toList());
        assertEquals(List.of(2, 5), agathaCaps.tiers().stream().map(tier -> tier.cost().prizes()).toList());
        assertEquals(List.of(4, 4), agathaCaps.tiers().stream().map(StarlynConfig.CapTier::milestone).toList());
        assertEquals(StarlynSister.AGATHA.getLevelCapAwards(), agathaCaps.tiers().stream().map(StarlynConfig.CapTier::award).toList());
        StarlynConfig.LevelCap miriaCaps = (StarlynConfig.LevelCap) miria.entry("level_cap");
        assertEquals(List.of(4, 5), miriaCaps.tiers().stream().map(StarlynConfig.CapTier::milestone).toList());
        assertEquals(StarlynSister.MIRIA.getLevelCapAwards(), miriaCaps.tiers().stream().map(StarlynConfig.CapTier::award).toList());
        assertEquals(47, prizesInShop(agatha));
        assertEquals(37 - 10, prizesInShop(miria));
    }

    private static int prizesInShop(StarlynConfig config) {
        int total = 0;
        for (StarlynConfig.ShopEntry entry : config.shop()) {
            switch (entry) {
                case StarlynConfig.LevelCap cap -> total += cap.tiers().stream().mapToInt(tier -> tier.cost().prizes()).sum();
                case StarlynConfig.Fortune fortune -> total += fortune.totalCost(fortune.maxLevel()).prizes();
                case StarlynConfig.Item item -> total += item.cost().prizes();
                default -> {
                }
            }
        }
        return total;
    }

    @Test
    void itemPricesAndMilestones() {
        assertItem(agatha, "frog_pet", 1, 20, 0);
        assertItem(agatha, "moonglade_jewel", 1, 50, 0);
        assertItem(agatha, "moonglade_talisman", 1, 30, 0);
        assertItem(agatha, "crow_shard", 2, 15, 0);
        assertItem(agatha, "small_frog_treat", 2, 30, 0);
        assertItem(agatha, "medium_frog_treat", 3, 40, 0);
        assertItem(agatha, "forest_pledge_book", 3, 30, 0);
        assertItem(agatha, "moonglade_ring", 3, 30, 0);
        assertItem(agatha, "heron_shard", 4, 15, 0);
        assertItem(agatha, "large_frog_treat", 4, 50, 0);
        assertItem(agatha, "fighting_booster", 4, 100, 0);
        assertItem(agatha, "giant_frog_treat", 5, 60, 0);
        assertItem(agatha, "moonglade_artifact", 5, 30, 0);
        assertItem(agatha, "shiniest", 5, 0, 20);
        assertEquals(1, ((StarlynConfig.Item) agatha.entry("moonglade_ring")).cost().amountOf(ItemType.MOONGLADE_TALISMAN));
        assertEquals(1, ((StarlynConfig.Item) agatha.entry("moonglade_artifact")).cost().amountOf(ItemType.MOONGLADE_RING));
        assertItem(miria, "sloth_pet", 1, 20, 0);
        assertItem(miria, "torrhus_talisman", 1, 30, 0);
        assertItem(miria, "extremely_mild_adhesive", 1, 100, 0);
        assertItem(miria, "woodpecker_shard", 2, 15, 0);
        assertItem(miria, "small_sloth_claw", 2, 30, 0);
        assertItem(miria, "medium_sloth_claw", 3, 40, 0);
        assertItem(miria, "petalfall_book", 3, 20, 0);
        assertItem(miria, "torrhus_ring", 3, 30, 0);
        assertItem(miria, "rubber_snorkel", 3, 125, 0);
        assertItem(miria, "vulture_shard", 4, 25, 0);
        assertItem(miria, "large_sloth_claw", 4, 50, 0);
        assertItem(miria, "winding_ivy", 4, 250, 0);
        assertItem(miria, "torrhus_artifact", 5, 30, 0);
        assertItem(miria, "giant_sloth_claw", 5, 60, 0);
        assertItem(miria, "vial_of_spring_water", 5, 0, 10);
    }

    private static void assertItem(StarlynConfig config, String id, int milestone, int coupons, int prizes) {
        StarlynConfig.Item item = (StarlynConfig.Item) config.entry(id);
        assertNotNull(item, id);
        assertEquals(milestone, item.milestone(), id);
        assertEquals(coupons, item.cost().coupons(), id);
        assertEquals(prizes, item.cost().prizes(), id);
    }

    @Test
    void everyShopEntryHasAUniqueIdAndFitsTheMenu() {
        for (StarlynConfig config : List.of(agatha, miria)) {
            Set<String> ids = new HashSet<>();
            config.shop().forEach(entry -> assertTrue(ids.add(entry.id()), entry.id()));
            assertTrue(config.shop().size() <= 21);
        }
        assertEquals(StarlynSister.AGATHA, agatha.sister());
        assertEquals("SKYBLOCK_GALATEA", agatha.serverType());
    }

    @Test
    void agathaScoringTable() {
        StarlynScoring scoring = new StarlynScoring(agatha.points());
        assertEquals(10, scoring.treeGift());
        assertEquals(3, scoring.logsPerHundred(StarlynTree.FIG));
        assertEquals(6, scoring.logsPerHundred(StarlynTree.MANGROVE));
        assertEquals(0, scoring.logsPerHundred(StarlynTree.HELIX));
        assertEquals(3, scoring.hunting("Verdant", "COMMON"));
        assertEquals(3, scoring.hunting("Azure", "COMMON"));
        assertEquals(6, scoring.hunting("Mossybit", "COMMON"));
        assertEquals(6, scoring.hunting("Cod", "COMMON"));
        assertEquals(15, scoring.hunting("coralot", "RARE"));
        assertEquals(15, scoring.hunting("Spike", "RARE"));
        assertEquals(45, scoring.hunting("Shellwise", "RARE"));
        assertEquals(45, scoring.hunting("Mudworm", "COMMON"));
        assertEquals(30, scoring.hunting("Bullfrog", "UNCOMMON"));
        assertEquals(14, scoring.fishing(FishCaughtEvent.Kind.SEA_CREATURE, null));
        assertEquals(29, scoring.fishing(FishCaughtEvent.Kind.TREASURE, "GOOD"));
        assertEquals(29, scoring.fishing(FishCaughtEvent.Kind.TREASURE, "OUTSTANDING"));
        assertEquals(4, scoring.fishing(FishCaughtEvent.Kind.ITEM, null));
        assertEquals(8, scoring.combat());
        assertEquals(9, scoring.gathering(StarlynResource.LUSHLILAC));
        assertEquals(2, scoring.gathering(StarlynResource.SEA_LUMIES));
        assertEquals(0, scoring.gathering(StarlynResource.HONEYCOMB));
    }

    @Test
    void miriaScoringTable() {
        StarlynScoring scoring = new StarlynScoring(miria.points());
        assertEquals(10, scoring.treeGift());
        assertEquals(9, scoring.logsPerHundred(StarlynTree.HELIX));
        assertEquals(0, scoring.logsPerHundred(StarlynTree.FIG));
        assertEquals(9, scoring.hunting("Anything", "COMMON"));
        assertEquals(25, scoring.hunting("Anything", "UNCOMMON"));
        assertEquals(50, scoring.hunting("Anything", "RARE"));
        assertEquals(100, scoring.hunting("Anything", "EPIC"));
        assertEquals(200, scoring.hunting("Anything", "LEGENDARY"));
        assertEquals(20, scoring.fishing(FishCaughtEvent.Kind.SEA_CREATURE, null));
        assertEquals(10, scoring.fishing(FishCaughtEvent.Kind.ITEM, null));
        assertEquals(60, scoring.fishing(FishCaughtEvent.Kind.TREASURE, "GOOD"));
        assertEquals(260, scoring.fishing(FishCaughtEvent.Kind.TREASURE, "GREAT"));
        assertEquals(760, scoring.fishing(FishCaughtEvent.Kind.TREASURE, "OUTSTANDING"));
        assertEquals(4, scoring.combat());
        assertEquals(1, scoring.gathering(StarlynResource.HONEYCOMB));
        assertEquals(8, scoring.gathering(StarlynResource.RUBY_VEILSHROOM));
    }

    @Test
    void everyGiveResolvesToARealItem() {
        for (StarlynConfig config : List.of(agatha, miria)) {
            for (StarlynConfig.ShopEntry entry : config.shop()) {
                if (!(entry instanceof StarlynConfig.Item item)) continue;
                switch (item.give().type()) {
                    case "ITEM", "PET" -> assertNotNull(item.give().item(), item.id());
                    case "SHARD" -> assertNotNull(dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry.get(item.give().shard()), item.id());
                    case "BOOK" -> assertNotNull(dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType.valueOf(item.give().enchant()), item.id());
                    default -> assertFalse(true, item.id());
                }
            }
        }
    }

    @Test
    void triggerNamesAreLowercaseDotted() {
        assertEquals("agatha.participated", StarlynSister.AGATHA.trigger("participated"));
        assertEquals("miria.bracket", StarlynSister.MIRIA.trigger("bracket"));
        assertEquals("agatha.milestone", StarlynSister.AGATHA.trigger("milestone"));
    }

    @Test
    void profileRoundTripsThroughJson() {
        StarlynProfile profile = new StarlynProfile();
        StarlynProfile.SisterData data = profile.sister(StarlynSister.AGATHA);
        data.setMet(true);
        data.setParticipated(true);
        data.setCouponsEarned(321);
        data.setMilestoneGranted(2);
        data.setPurchased("fig_fortune", 7);
        data.getPersonalBests().put(StarlynTree.FIG, 4_321L);
        data.getReached().add(StarlynBracket.MYTHIC);
        StarlynProfile.Entry entry = new StarlynProfile.Entry(1234);
        entry.setPoints(300.5);
        entry.getLogs().put(StarlynTree.MANGROVE, 250L);
        entry.setBracket(StarlynBracket.UNCOMMON);
        entry.setFinalised(true);
        data.getEntries().add(entry);

        StarlynProfile copy = StarlynProfile.deserialize(profile.serialize());
        StarlynProfile.SisterData restored = copy.sister(StarlynSister.AGATHA);
        assertTrue(restored.isMet());
        assertTrue(restored.isParticipated());
        assertEquals(321, restored.getCouponsEarned());
        assertEquals(2, restored.getMilestoneGranted());
        assertEquals(7, restored.purchased("fig_fortune"));
        assertEquals(4_321L, restored.personalBest(StarlynTree.FIG));
        assertTrue(restored.reachedBracket(StarlynBracket.DIVINE) == false && restored.reachedBracket(StarlynBracket.MYTHIC));
        assertEquals(1, restored.claimable().size());
        StarlynProfile.Entry restoredEntry = restored.entryFor(1234).orElseThrow();
        assertEquals(300, restoredEntry.score());
        assertEquals(250L, restoredEntry.logs(StarlynTree.MANGROVE));
        assertEquals(StarlynBracket.UNCOMMON, restoredEntry.getBracket());
        assertEquals(1, profile.copy().sister(StarlynSister.AGATHA).getEntries().size());
        assertFalse(StarlynProfile.deserialize(null).sister(StarlynSister.MIRIA).isMet());
    }

    @Test
    void dailyBonusIsThreeContestsOf1200() {
        assertEquals(3, StarlynRewards.DAILY_BONUS_CONTESTS);
        assertEquals(1_200, StarlynRewards.DAILY_BONUS_AMOUNT);
        assertEquals(8, StarlynRewards.DAILY_BONUS_WHISPER_TIER);
    }
}
