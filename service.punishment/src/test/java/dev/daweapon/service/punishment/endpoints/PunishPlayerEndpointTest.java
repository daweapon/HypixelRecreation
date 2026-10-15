package dev.daweapon.service.punishment.endpoints;

import dev.daweapon.commons.protocol.objects.punishment.PunishPlayerServiceProtocol;
import dev.daweapon.commons.punishment.PunishmentReason;
import dev.daweapon.commons.punishment.PunishmentTag;
import dev.daweapon.commons.punishment.template.BanType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PunishPlayerEndpointTest {
    private final PunishPlayerEndpoint endpoint = new PunishPlayerEndpoint();

    private PunishPlayerServiceProtocol.PunishPlayerMessage message(String type, long expiresAt, List<PunishmentTag> tags) {
        return new PunishPlayerServiceProtocol.PunishPlayerMessage(
                UUID.randomUUID(), type, new PunishmentReason(BanType.CROSS_TEAMING), UUID.randomUUID(), tags, expiresAt);
    }

    @Test
    void rejectsUnknownPunishmentType() {
        var response = endpoint.handle(message("KICK", -1, null), null);

        assertFalse(response.success());
        assertEquals(PunishPlayerServiceProtocol.ErrorCode.INVALID_TYPE, response.errorCode());
        assertNull(response.punishmentId());
    }

    @Test
    void rejectsLowercaseTypeBecauseMatchingIsCaseSensitive() {
        var response = endpoint.handle(message("ban", -1, null), null);

        assertEquals(PunishPlayerServiceProtocol.ErrorCode.INVALID_TYPE, response.errorCode());
    }

    @Test
    void rejectsExpiryInThePast() {
        var response = endpoint.handle(message("BAN", System.currentTimeMillis() - 60_000, null), null);

        assertFalse(response.success());
        assertEquals(PunishPlayerServiceProtocol.ErrorCode.INVALID_EXPIRY, response.errorCode());
    }

    @Test
    void typeIsValidatedBeforeExpiry() {
        var response = endpoint.handle(message("NOPE", 1, null), null);

        assertEquals(PunishPlayerServiceProtocol.ErrorCode.INVALID_TYPE, response.errorCode());
    }

    @Test
    void validRequestWithoutRedisReportsDatabaseError() {
        var response = endpoint.handle(message("MUTE", System.currentTimeMillis() + 3_600_000, null), null);

        assertFalse(response.success());
        assertEquals(PunishPlayerServiceProtocol.ErrorCode.DATABASE_ERROR, response.errorCode());
        assertNull(response.punishmentId());
    }

    @Test
    void permanentPunishmentPassesExpiryValidation() {
        var response = endpoint.handle(message("WARNING", -1, List.of(PunishmentTag.OVERWRITE)), null);

        assertEquals(PunishPlayerServiceProtocol.ErrorCode.DATABASE_ERROR, response.errorCode());
    }

    @Test
    void usesPunishPlayerServiceProtocol() {
        assertInstanceOf(PunishPlayerServiceProtocol.class, endpoint.protocol());
    }
}
