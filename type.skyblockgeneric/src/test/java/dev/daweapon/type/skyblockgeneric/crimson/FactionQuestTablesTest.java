package dev.daweapon.type.skyblockgeneric.crimson;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.ReputationTier;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuest;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestBoard;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestCatalogue;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestGenerator;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestRewards;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestTier;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestType;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingRegistry;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FactionQuestTablesTest {

    @Test
    void everyReputationTitleGetsFiveTiersMatchingTheWikiTable() {
        for (ReputationTier title : ReputationTier.values()) assertEquals(5, FactionQuestTier.mixFor(title).size(), title.name());
        assertEquals(List.of(FactionQuestTier.C, FactionQuestTier.C, FactionQuestTier.B, FactionQuestTier.A, FactionQuestTier.S),
                FactionQuestTier.mixFor(ReputationTier.HERO));
    }

    @Test
    void tierRewardsMatchTheWiki() {
        assertEquals(List.of(2_000, 5_000, 10_000, 15_000, 20_000),
                List.of(FactionQuestTier.values()).stream().map(FactionQuestTier::getCombatXP).toList());
        assertEquals(List.of(30, 50, 75, 100, 125),
                List.of(FactionQuestTier.values()).stream().map(FactionQuestTier::getReputation).toList());
        assertEquals(List.of(7, 11, 4, 10, 6),
                List.of(FactionQuestTier.values()).stream().map(tier -> tier.getItemPool().size()).toList());
        assertEquals(List.of(3, 6, 3, 4, 2),
                List.of(FactionQuestTier.values()).stream().map(tier -> tier.getFishingPool().size()).toList());
    }

    @Test
    void everyRewardAndFetchItemExists() {
        for (FactionQuestTier tier : FactionQuestTier.values()) {
            tier.getItemPool().forEach(reward -> assertNotNull(reward.type(), reward.itemId()));
            tier.getFishingPool().forEach(reward -> assertNotNull(reward.type(), reward.itemId()));
        }
        FactionQuestCatalogue.all().stream().filter(quest -> quest.type() == FactionQuestType.FETCH)
                .forEach(quest -> assertNotNull(ItemType.get(quest.target()), quest.id()));
    }

    @Test
    void poolHasEveryNamedQuest() {
        Map<FactionQuestType, Long> distinctTargets = FactionQuestCatalogue.all().stream()
                .collect(Collectors.groupingBy(FactionQuest::type,
                        Collectors.collectingAndThen(Collectors.mapping(FactionQuest::target, Collectors.toSet()), set -> (long) set.size())));
        assertEquals(13, distinctTargets.get(FactionQuestType.FISHING));
        assertEquals(14, distinctTargets.get(FactionQuestType.FETCH));
        assertEquals(5, distinctTargets.get(FactionQuestType.MINIBOSS));
        assertEquals(7, distinctTargets.get(FactionQuestType.DOJO));
        assertEquals(5, distinctTargets.get(FactionQuestType.KUUDRA));
        assertEquals(2, distinctTargets.get(FactionQuestType.RESCUE));
    }

    @Test
    void fishingQuestsNameRealTrophyFish() {
        FishingRegistry.loadAll(new File("../configuration/skyblock/fishing"));
        FactionQuestCatalogue.all().stream().filter(quest -> quest.type() == FactionQuestType.FISHING)
                .forEach(quest -> assertNotNull(FishingRegistry.getTrophyFish(quest.target()), quest.target()));
    }

    @Test
    void boardsRollFiveQuestsOfDifferentCategoriesWithoutKuudra() {
        for (CrimsonFaction faction : CrimsonFaction.values()) {
            for (ReputationTier title : ReputationTier.values()) {
                for (int seed = 0; seed < 50; seed++) {
                    List<FactionQuest> quests = FactionQuestGenerator.roll(faction, title,
                            quest -> quest.type() != FactionQuestType.KUUDRA, new Random(seed));
                    assertEquals(5, quests.size(), faction + " " + title);
                    assertEquals(5, quests.stream().map(FactionQuest::type).distinct().count(), faction + " " + title);
                    assertTrue(quests.stream().allMatch(quest -> quest.faction() == null || quest.faction() == faction));
                    assertEquals(FactionQuestTier.mixFor(title).stream().sorted().toList(),
                            quests.stream().map(FactionQuest::tier).sorted().toList());
                }
            }
        }
    }

    @Test
    void boardSurvivesSerialization() {
        FactionQuestBoard.Slot fetch = new FactionQuestBoard.Slot("FETCH_MAGMAG_C", 3, false, false);
        fetch.setReward(new FactionQuestTier.RewardItem("MAGMAG", 10));
        FactionQuestBoard board = new FactionQuestBoard(20_000, List.of(fetch,
                new FactionQuestBoard.Slot("RESCUE_MAGE_B", 2, true, true)));
        FactionQuestBoard copy = FactionQuestBoard.fromJson(board.toJson());
        assertEquals(20_000, copy.getDay());
        assertEquals(2, copy.getSlots().size());
        assertEquals(3, copy.getSlots().getFirst().getProgress());
        assertEquals(new FactionQuestTier.RewardItem("MAGMAG", 10), copy.getSlots().getFirst().getReward());
        assertNull(copy.getSlots().get(1).getReward());
        assertTrue(copy.getSlots().get(1).isClaimed());
    }

    @Test
    void rolledRewardComesFromTheQuestPool() {
        Random random = new Random(7);
        for (FactionQuest quest : FactionQuestCatalogue.all()) {
            FactionQuestTier.RewardItem reward = FactionQuestRewards.roll(null, quest, random);
            assertTrue(quest.tier().pool(quest.type()).contains(reward), quest.id());
        }
    }
}
