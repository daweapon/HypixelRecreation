package dev.daweapon.dungeons.partyfinder;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PartyFinderGroupTest {
    enum Cls { MAGE, TANK }

    private static PartyFinderMember<Cls> member(Cls cls, int level) {
        return PartyFinderMember.member(UUID.randomUUID(), "p", cls, level);
    }

    private static PartyFinderGroup<String, Cls> group(int capacity, int minLevel, Set<Cls> allowed) {
        return new PartyFinderGroup<>(UUID.randomUUID(), PartyFinderMember.leader(UUID.randomUUID(), "lead", Cls.MAGE, 30),
                "F7", null, minLevel, allowed, capacity, 2, 0L);
    }

    @Test
    void leaderIsInsertedWithLeaderRole() {
        var g = group(5, 0, Set.of());
        assertEquals(1, g.size());
        assertEquals(PartyFinderRole.LEADER, g.leader().role());
        assertEquals("", g.note());
    }

    @Test
    void checkRejectsLowLevelAndDisallowedClass() {
        var g = group(5, 20, Set.of(Cls.MAGE));
        assertEquals(PartyFinderGroup.JoinCheck.LEVEL_TOO_LOW, g.check(member(Cls.MAGE, 10)));
        assertEquals(PartyFinderGroup.JoinCheck.CLASS_NOT_ALLOWED, g.check(member(Cls.TANK, 25)));
        assertEquals(PartyFinderGroup.JoinCheck.OK, g.check(member(Cls.MAGE, 25)));
    }

    @Test
    void fullGroupRefusesJoin() {
        var g = group(2, 0, Set.of());
        g.join(member(Cls.TANK, 1));
        assertTrue(g.full());
        assertEquals(PartyFinderGroup.JoinCheck.FULL, g.check(member(Cls.TANK, 1)));
        assertThrows(IllegalStateException.class, () -> g.join(member(Cls.TANK, 1)));
    }

    @Test
    void duplicateMemberIsDetected() {
        var g = group(5, 0, Set.of());
        var m = member(Cls.TANK, 1);
        g.join(m);
        assertEquals(PartyFinderGroup.JoinCheck.ALREADY_MEMBER, g.check(m));
    }

    @Test
    void readyNeedsMinimumSizeAndEveryoneReady() {
        var g = group(5, 0, Set.of());
        g.ready(g.leaderId(), true);
        assertFalse(g.ready());
        var m = member(Cls.TANK, 1);
        g.join(m);
        assertFalse(g.ready());
        g.ready(m.playerId(), true);
        assertTrue(g.ready());
    }

    @Test
    void leaderLeavingClosesGroup() {
        var g = group(5, 0, Set.of());
        var m = member(Cls.TANK, 1);
        g.join(m);
        assertTrue(g.leave(m.playerId()));
        assertTrue(g.open());
        assertFalse(g.leave(m.playerId()));
        assertTrue(g.leave(g.leaderId()));
        assertFalse(g.open());
        assertEquals(PartyFinderGroup.JoinCheck.CLOSED, g.check(member(Cls.TANK, 1)));
    }

    @Test
    void updateClassEnforcesAllowedSet() {
        var g = group(5, 0, Set.of(Cls.MAGE));
        assertThrows(IllegalArgumentException.class, () -> g.updateClass(g.leaderId(), Cls.TANK));
        assertThrows(IllegalArgumentException.class, () -> g.member(UUID.randomUUID()));
    }

    @Test
    void zeroCapacityRejected() {
        assertThrows(IllegalArgumentException.class, () -> group(0, 0, Set.of()));
    }
}
