package dev.daweapon.type.skyblockgeneric.runecrafting;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.skill.skills.RunecraftingSkill;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunecraftingRulesTest {
    @Test
    @SuppressWarnings("unchecked")
    void skillTableHasTwentyFiveLevelsAndEachUnlocksItsRuneLevel() throws java.io.IOException {
        Map<String, Object> yaml = YamlFileUtils.loadYaml(new File("../configuration/skyblock/skills/runecrafting.yml"));
        List<Map<String, Object>> rewards = (List<Map<String, Object>>) yaml.get("rewards");
        assertEquals(25, rewards.size());
        double total = 0;
        for (int i = 0; i < rewards.size(); i++) {
            Map<String, Object> reward = rewards.get(i);
            assertEquals(i + 1, reward.get("level"));
            total += ((Number) reward.get("requirement")).doubleValue();
            List<Map<String, Object>> unlocks = (List<Map<String, Object>>) reward.get("unlocks");
            Map<String, Object> data = (Map<String, Object>) unlocks.getFirst().get("data");
            assertEquals(i + 1, data.get("runeLevel"));
        }
        assertEquals(94250, total);
    }

    @Test
    void multipliersMatchTheWiki() {
        assertEquals(1D, RunecraftingSkill.rankMultiplier(Rank.DEFAULT));
        assertEquals(1.1D, RunecraftingSkill.rankMultiplier(Rank.VIP));
        assertEquals(1.25D, RunecraftingSkill.rankMultiplier(Rank.VIP_PLUS));
        assertEquals(1.5D, RunecraftingSkill.rankMultiplier(Rank.MVP));
        assertEquals(2D, RunecraftingSkill.rankMultiplier(Rank.MVP_PLUS));
        assertEquals(3D, RunecraftingSkill.rankMultiplier(Rank.MVP_PLUS_PLUS));
    }

    @Test
    void fusionChanceAndXpFollowRarity() {
        assertEquals(25, RuneFusion.chancePercent(Rarity.COMMON));
        assertEquals(35, RuneFusion.chancePercent(Rarity.UNCOMMON));
        assertEquals(40, RuneFusion.chancePercent(Rarity.RARE));
        assertEquals(45, RuneFusion.chancePercent(Rarity.EPIC));
        assertEquals(50, RuneFusion.chancePercent(Rarity.LEGENDARY));
        assertEquals(10, RuneFusion.xp(Rarity.COMMON));
        assertEquals(35, RuneFusion.xp(Rarity.LEGENDARY));
    }

    @Test
    void onlyEqualLevelsBelowTheCapFuse() {
        assertTrue(RuneFusion.canFuse(1, 1));
        assertTrue(RuneFusion.canFuse(2, 2));
        assertFalse(RuneFusion.canFuse(1, 2));
        assertFalse(RuneFusion.canFuse(3, 3));
    }

    @Test
    void runicSpawnRateIsHalfAPercent() {
        Random random = new Random(7);
        int runic = 0;
        int trials = 200_000;
        for (int i = 0; i < trials; i++) if (RunicMobs.rollSpawn(random)) runic++;
        assertEquals(0.005, runic / (double) trials, 0.0008);
    }

    @Test
    void runicDropsAreWeightedTowardCommonRunes() {
        Random random = new Random(11);
        int common = 0;
        int legendary = 0;
        for (int i = 0; i < 20_000; i++) {
            ItemType rune = RunicMobs.pickRune(null, random);
            if (rune.rarity == Rarity.COMMON) common++;
            if (rune.rarity == Rarity.LEGENDARY) legendary++;
        }
        assertTrue(common > legendary * 4, common + " vs " + legendary);
    }

    @Test
    void everyRunicRuneIsInTheCatalogue() {
        assertEquals(13, RunicMobs.runes().size());
    }
}
