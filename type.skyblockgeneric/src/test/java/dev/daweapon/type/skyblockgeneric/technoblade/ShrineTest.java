package dev.daweapon.type.skyblockgeneric.technoblade;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShrineTest {
    private static final long HOUR = 3_600_000L;

    private static ShrineSnapshot snapshot(int relics, boolean built, long completedAt) {
        Map<Integer, String> found = new java.util.HashMap<>();
        for (int index = 0; index < relics; index++) found.put(index, "Player" + index);
        return new ShrineSnapshot(found, built, completedAt, null);
    }

    @Test
    void configHasSevenDistinctRelicSpots() throws Exception {
        Map<String, Object> root = YamlFileUtils.loadYaml(new File("../configuration/skyblock/technoblade/shrine.yml"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> relics = (List<Map<String, Object>>) root.get("relics");
        assertEquals(7, relics.size());
        Set<String> spots = new HashSet<>();
        relics.forEach(relic -> spots.add(relic.get("x") + "," + relic.get("y") + "," + relic.get("z")));
        assertEquals(7, spots.size());
        assertEquals(20, ((Number) root.get("effect_interval_minutes")).intValue());
        assertEquals(48, ((Number) root.get("effect_window_hours")).intValue());
    }

    @Test
    void tenEffectsFiveOfThemSkillExperience() {
        assertEquals(10, AncientPigEffect.values().length);
        long skillEffects = java.util.Arrays.stream(AncientPigEffect.values()).filter(effect -> effect.skill() != null).count();
        assertEquals(5, skillEffects);
        assertEquals(SkillCategories.FARMING, AncientPigEffect.FARMING_EXPERIENCE.skill());
        assertEquals(25, AncientPigEffect.SKILL_XP_BONUS_PERCENT);
        assertEquals(5, AncientPigEffect.RUNIC_CHANCE_FACTOR);
        assertEquals(50, AncientPigEffect.INFUSION_BONUS_PERCENT);
        assertEquals(60_000, AncientPigEffect.INFUSION_MILLIS);
    }

    @Test
    void effectsWithoutAnActiveShrineChangeNothing() {
        assertEquals(1, AncientPigEffects.skillExperienceMultiplier(SkillCategories.MINING));
        assertEquals(1, AncientPigEffects.runicChanceMultiplier());
        assertEquals(3, AncientPigEffects.dropAmount(3));
    }

    @Test
    void activeEffectRoundTripsThroughItsRedisForm() {
        ShrineSnapshot.ActiveEffect effect = new ShrineSnapshot.ActiveEffect(AncientPigEffect.THUNDER, 1_000, 2_000);
        assertEquals(effect, ShrineSnapshot.ActiveEffect.parse(effect.serialize()));
        assertNull(ShrineSnapshot.ActiveEffect.parse("NOPE|1|2"));
        assertNull(ShrineSnapshot.ActiveEffect.parse(null));
        assertTrue(effect.activeAt(1_999));
        assertFalse(effect.activeAt(2_000));
    }

    @Test
    void shrineStandsWhileThePerkIsActiveOrRelicsRemain() {
        assertTrue(snapshot(0, false, 0).standing(true, 7, 48 * HOUR, 0));
        assertFalse(snapshot(0, false, 0).standing(false, 7, 48 * HOUR, 0));
        assertTrue(snapshot(3, true, 0).standing(false, 7, 48 * HOUR, 99 * HOUR));
    }

    @Test
    void completedShrineStaysTwoDaysAfterCompletion() {
        ShrineSnapshot complete = snapshot(7, true, 10 * HOUR);
        assertTrue(complete.complete(7));
        assertTrue(complete.standing(false, 7, 48 * HOUR, 10 * HOUR + 47 * HOUR));
        assertFalse(complete.standing(false, 7, 48 * HOUR, 10 * HOUR + 48 * HOUR));
    }

    @Test
    void technoshopClosesOnceAllRelicsAreFoundAndTheTermIsOver() {
        assertTrue(snapshot(2, true, 0).technoshopOpen(false, 7));
        assertTrue(snapshot(7, true, 5).technoshopOpen(true, 7));
        assertFalse(snapshot(7, true, 5).technoshopOpen(false, 7));
        assertFalse(snapshot(0, false, 0).technoshopOpen(false, 7));
    }
}
