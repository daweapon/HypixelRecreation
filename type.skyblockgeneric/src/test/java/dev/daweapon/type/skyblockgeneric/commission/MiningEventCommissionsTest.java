package dev.daweapon.type.skyblockgeneric.commission;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiningEventCommissionsTest {
    @Test
    void dwarvenMobsMapToTheirCommissionTargets() {
        assertEquals(Objective.BlockTarget.GOBLIN, CommissionProgress.killTarget("GOBLIN_WEAKLING_MELEE_2"));
        assertEquals(Objective.BlockTarget.GOBLIN, CommissionProgress.killTarget("GOBLIN_CREEPERTAMER_90"));
        assertEquals(Objective.BlockTarget.GOLDEN_GOBLIN, CommissionProgress.killTarget("GOBLIN"));
        assertEquals(Objective.BlockTarget.GOLDEN_GOBLIN, CommissionProgress.killTarget("GOBLIN_2"));
        assertEquals(Objective.BlockTarget.GLACITE_WALKER, CommissionProgress.killTarget("ICE_WALKER"));
        assertEquals(Objective.BlockTarget.TREASURE_HOARDER, CommissionProgress.killTarget("TREASURE_HOARDER"));
        assertNull(CommissionProgress.killTarget("ZOMBIE"));
    }

    @Test
    void eventDependentCommissionsAreTheWikiEventGroup() {
        long events = Commissions.SITUATIONAL.stream().filter(Commissions::isEventDependent).count();
        assertEquals(6, events);
        assertNotNull(Commissions.getCommissionByName("Elusive Goblin Slayer"));
        assertTrue(Commissions.isEventDependent(Commissions.getCommissionByName("Elusive Goblin Slayer")));
        assertFalse(Commissions.isEventDependent(Commissions.getCommissionByName("Star Sentry Puncher")));
    }
}
