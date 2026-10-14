package net.swofty.service.punishment.endpoints;

import net.swofty.commons.protocol.objects.punishment.GetActivePunishmentProtocol;
import net.swofty.commons.protocol.objects.punishment.UnpunishPlayerProtocol;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ActivePunishmentLookupEndpointsTest {
    @Test
    void getActiveReportsNotFoundWhenNothingStored() {
        var response = new GetActivePunishmentEndpoint().handle(
                new GetActivePunishmentProtocol.GetActivePunishmentMessage(UUID.randomUUID(), "BAN"), null);

        assertFalse(response.found());
        assertTrue(response.success());
        assertNull(response.banId());
        assertNull(response.reason());
        assertTrue(response.tags().isEmpty());
    }

    @Test
    void unpunishExplainsWhichTypeWasMissingInLowercase() {
        var response = new UnpunishPlayerEndpoint().handle(
                new UnpunishPlayerProtocol.UnpunishPlayerMessage(UUID.randomUUID(), UUID.randomUUID(), "MUTE"), null);

        assertFalse(response.success());
        assertEquals("No active mute found for this player.", response.errorMessage());
    }
}
