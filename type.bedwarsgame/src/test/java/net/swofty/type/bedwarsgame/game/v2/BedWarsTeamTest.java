package net.swofty.type.bedwarsgame.game.v2;

import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.type.bedwarsgame.shop.TeamUpgradeId;
import net.swofty.type.bedwarsgame.shop.TrapId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BedWarsTeamTest {
    @Test
    void newTeamTakesIdentityFromKeyAndHasNoBed() {
        BedWarsTeam team = new BedWarsTeam(TeamKey.RED);
        assertEquals("RED", team.getId());
        assertEquals("Red", team.getName());
        assertEquals("R", team.firstLetter());
        assertFalse(team.canRespawn());
    }

    @Test
    void respawnFollowsBedState() {
        BedWarsTeam team = new BedWarsTeam(TeamKey.BLUE);
        team.setBedAlive(true);
        assertTrue(team.canRespawn());
        team.destroyBed();
        assertFalse(team.canRespawn());
    }

    @Test
    void upgradeLevelDefaultsToZero() {
        BedWarsTeam team = new BedWarsTeam(TeamKey.GREEN);
        assertEquals(0, team.getUpgradeLevel(TeamUpgradeId.FORGE));
        team.setUpgradeLevel(TeamUpgradeId.FORGE, 3);
        assertEquals(3, team.getUpgradeLevel(TeamUpgradeId.FORGE));
    }

    @Test
    void trapsAreTrackedInOrder() {
        BedWarsTeam team = new BedWarsTeam(TeamKey.AQUA);
        team.addTrap(TrapId.BLINDNESS);
        team.addTrap(TrapId.REVEAL);
        assertEquals(2, team.getTrapCount());
        assertTrue(team.hasTrap(TrapId.REVEAL));
        team.removeTrap(TrapId.BLINDNESS);
        assertFalse(team.hasTrap(TrapId.BLINDNESS));
        assertEquals(1, team.getTrapCount());
    }

    @Test
    void swapStateExchangesBedUpgradesAndTrapsButNotPlayers() {
        BedWarsTeam a = new BedWarsTeam(TeamKey.RED);
        BedWarsTeam b = new BedWarsTeam(TeamKey.BLUE);
        UUID playerA = UUID.randomUUID();
        a.addPlayer(playerA);
        a.setBedAlive(true);
        a.setUpgradeLevel(TeamUpgradeId.SHARPNESS, 1);
        a.addTrap(TrapId.MINER_FATIGUE);
        b.setUpgradeLevel(TeamUpgradeId.HEAL_POOL, 1);

        a.swapState(b);

        assertFalse(a.isBedAlive());
        assertTrue(b.isBedAlive());
        assertEquals(0, a.getUpgradeLevel(TeamUpgradeId.SHARPNESS));
        assertEquals(1, a.getUpgradeLevel(TeamUpgradeId.HEAL_POOL));
        assertEquals(1, b.getUpgradeLevel(TeamUpgradeId.SHARPNESS));
        assertTrue(b.hasTrap(TrapId.MINER_FATIGUE));
        assertEquals(0, a.getTrapCount());
        assertTrue(a.getPlayerIds().contains(playerA));
    }
}
