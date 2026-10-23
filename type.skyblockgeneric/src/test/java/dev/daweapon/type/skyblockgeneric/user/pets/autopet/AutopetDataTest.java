package dev.daweapon.type.skyblockgeneric.user.pets.autopet;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointAutopets;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutopetDataTest {
    @Test
    void everyTwoPackAddsTwoSlotsUpToTwentyEight() {
        DatapointAutopets.AutopetData data = new DatapointAutopets.AutopetData();
        assertEquals(0, data.unlockedSlots());
        for (int i = 0; i < DatapointAutopets.MAX_PACKS; i++) assertTrue(data.addPack());
        assertFalse(data.addPack());
        assertEquals(28, data.unlockedSlots());
    }

    @Test
    void lockedSlotsCannotHoldRules() {
        DatapointAutopets.AutopetData data = new DatapointAutopets.AutopetData();
        data.addPack();
        AutopetRule rule = new AutopetRule(AutopetTrigger.LOGIN, null, "ELEPHANT", "LEGENDARY", List.of());
        data.setRule(2, rule);
        assertNull(data.rule(2));
        data.setRule(1, rule);
        assertEquals(rule, data.rule(1));
    }

    @Test
    void rulesRoundTripThroughTheDatapoint() {
        DatapointAutopets point = new DatapointAutopets("autopets");
        point.getValue().setPacks(3);
        AutopetRule rule = new AutopetRule(AutopetTrigger.GAIN_SKILL_XP, "FARMING", "RABBIT", "EPIC", List.of(
                new AutopetRule.Condition(AutopetException.NOT_IN_ISLAND, "SKYBLOCK_HUB"),
                new AutopetRule.Condition(AutopetException.SLAYER_ACTIVE, null)));
        point.getValue().setRule(4, rule);
        DatapointAutopets copy = new DatapointAutopets("autopets");
        copy.deserializeValue(point.getSerializedValue());
        assertEquals(3, copy.getValue().getPacks());
        assertEquals(rule, copy.getValue().rule(4));
        assertNull(copy.getValue().rule(0));
    }

    @Test
    void unavailableTriggersAreTheUnbuiltOnes() {
        assertFalse(AutopetTrigger.SELECT_DUNGEON_CLASS.isAvailable());
        assertFalse(AutopetTrigger.CATACOMBS_FLOOR.isAvailable());
        assertFalse(AutopetTrigger.AFK.isAvailable());
        assertTrue(AutopetTrigger.FISHING_HOOK.isAvailable());
        assertEquals(16, AutopetTrigger.values().length);
    }
}
