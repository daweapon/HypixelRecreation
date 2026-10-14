package net.swofty.type.murdermysterygame.role;

import net.swofty.commons.murdermystery.MurderMysteryGameType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameRoleTest {
    @Test
    void everyRoleHasDisplayNameAndMessages() {
        for (GameRole role : GameRole.values()) {
            assertFalse(role.getDisplayName().isBlank());
            assertNotNull(role.getAnnouncement());
            assertNotNull(role.getDescription());
        }
    }

    @Test
    void gameTypeRoleCountsFitInsideMaxPlayers() {
        for (MurderMysteryGameType type : MurderMysteryGameType.values()) {
            assertTrue(type.getMurdererCount() + type.getDetectiveCount() < type.getMaxPlayers());
            assertTrue(type.getMinPlayers() >= 4);
            assertTrue(type.getMinPlayers() <= type.getMaxPlayers());
        }
    }

    @Test
    void gameTypeLookupIgnoresCase() {
        assertEquals(MurderMysteryGameType.DOUBLE_UP, MurderMysteryGameType.from("double_up"));
        assertNull(MurderMysteryGameType.from("nope"));
    }
}
