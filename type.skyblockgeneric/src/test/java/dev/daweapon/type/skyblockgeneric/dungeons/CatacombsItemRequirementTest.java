package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.type.skyblockgeneric.item.components.DungeonItemComponent;
import dev.daweapon.type.skyblockgeneric.item.components.ItemRequirementsComponent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatacombsItemRequirementTest {
    private static ItemRequirementsComponent.Requirement requirement(ItemRequirementsComponent.Type type, int level) {
        return new ItemRequirementsComponent.Requirement(type, "CATACOMBS", level, "");
    }

    @Test
    void dungeonSkillNeedsTheCatacombsLevel() {
        ItemRequirementsComponent.Requirement skill = requirement(ItemRequirementsComponent.Type.DUNGEON_SKILL, 18);
        assertFalse(skill.isDungeonSkillMet(17));
        assertTrue(skill.isDungeonSkillMet(18));
        assertTrue(skill.isDungeonSkillMet(50));
        assertEquals("Catacombs Skill 18", skill.display());
    }

    @Test
    void dungeonTierNeedsThatFloorCompleted() {
        ItemRequirementsComponent.Requirement hyperion = requirement(ItemRequirementsComponent.Type.DUNGEON_TIER, 7);
        CatacombsCompletions sixOnly = CatacombsCompletions.parse("").with(CatacombsFloor.FLOOR_SIX, CatacombsMode.NORMAL);
        CatacombsCompletions sevenMaster = CatacombsCompletions.parse("").with(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.MASTER);

        assertFalse(hyperion.isDungeonTierMet(sixOnly::hasCompleted));
        assertTrue(hyperion.isDungeonTierMet(sevenMaster::hasCompleted));
        assertTrue(hyperion.isDungeonTierMet(CatacombsCompletions.parse("").withEveryFloorCompleted()::hasCompleted));
        assertEquals("The Catacombs Floor VII Completion", hyperion.display());
        assertEquals("The Catacombs Entrance Completion",
                requirement(ItemRequirementsComponent.Type.DUNGEON_TIER, 0).display());
    }

    @Test
    void aMaxedProfileMeetsEveryDungeonRequirement() {
        CatacombsCompletions maxed = CatacombsCompletions.parse("").withEveryFloorCompleted();
        for (int tier = 0; tier < CatacombsFloor.values().length; tier++) {
            assertTrue(requirement(ItemRequirementsComponent.Type.DUNGEON_TIER, tier).isDungeonTierMet(maxed::hasCompleted));
        }
        assertTrue(requirement(ItemRequirementsComponent.Type.DUNGEON_SKILL, 50).isDungeonSkillMet(CatacombsProgress.level(Long.MAX_VALUE)));
    }

    @Test
    void dungeonRarityLineReadsDungeonPlusTheItemType() {
        assertEquals("DUNGEON SWORD", DungeonItemComponent.rarityDisplay("SWORD"));
        assertEquals("DUNGEON ITEM", DungeonItemComponent.rarityDisplay(null));
    }

    @Test
    void rewardLinesFollowTheWikiTable() {
        List<String> levelOne = CatacombsSkillDisplay.rewardLines(1);
        assertTrue(levelOne.stream().anyMatch(line -> line.contains("+<a>2 <c>❤ Health")));
        assertTrue(levelOne.stream().anyMatch(line -> line.contains("<c>5% ")));
        assertTrue(levelOne.stream().anyMatch(line -> line.contains("Access to <c>Floor I")));
        assertTrue(levelOne.stream().anyMatch(line -> line.contains("20 SkyBlock XP")));
        List<String> levelTwentyFour = CatacombsSkillDisplay.rewardLines(24);
        assertTrue(levelTwentyFour.stream().anyMatch(line -> line.contains("Access to <c>Floor VII")));
        assertTrue(levelTwentyFour.stream().anyMatch(line -> line.contains("Access to <c>Master Mode Floor I")));
        assertTrue(CatacombsSkillDisplay.rewardLines(45).stream().anyMatch(line -> line.contains("40 SkyBlock XP")));
        assertEquals(List.of("<6><l>MAX LEVEL REACHED"), CatacombsSkillDisplay.progressLines(Long.MAX_VALUE, ""));
        assertTrue(CatacombsSkillDisplay.progressLines(25, "").getFirst().contains("50%"));
    }
}
