package dev.daweapon.service.api;

import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.ControlTokens;
import dev.daweapon.service.api.control.DeviceRecord;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControlAccessTest {

    private static DeviceRecord device(ControlRole role) {
        return new DeviceRecord("hash", UUID.randomUUID().toString(), "test device",
                "daweapon", UUID.randomUUID(), role, Instant.now(), Instant.now(), false);
    }

    @Test
    void readOnlyCannotOperateOrAdminister() {
        assertTrue(ControlRole.READ_ONLY.canRead());
        assertFalse(ControlRole.READ_ONLY.canOperate());
        assertFalse(ControlRole.READ_ONLY.canAdminister());
    }

    @Test
    void operatorCanOperateButNotAdminister() {
        assertTrue(ControlRole.OPERATOR.canOperate());
        assertFalse(ControlRole.OPERATOR.canAdminister());
    }

    @Test
    void ownerSatisfiesEveryRequirement() {
        for (ControlRole required : ControlRole.values()) {
            assertTrue(ControlRole.OWNER.satisfies(required), "owner should satisfy " + required);
        }
    }

    @Test
    void readOnlyDoesNotSatisfyOperatorRequirement() {
        assertFalse(ControlRole.READ_ONLY.satisfies(ControlRole.OPERATOR));
        assertFalse(ControlRole.OPERATOR.satisfies(ControlRole.OWNER));
    }

    @Test
    void unknownRoleFallsBackRatherThanThrowing() {
        assertEquals(ControlRole.READ_ONLY, ControlRole.parse("nonsense", ControlRole.READ_ONLY));
        assertEquals(ControlRole.READ_ONLY, ControlRole.parse(null, ControlRole.READ_ONLY));
        assertEquals(ControlRole.OWNER, ControlRole.parse("owner", ControlRole.READ_ONLY));
    }

    @Test
    void tokensAreUniqueAndHashedStably() {
        String a = ControlTokens.generateToken();
        String b = ControlTokens.generateToken();
        assertNotEquals(a, b);
        assertEquals(ControlTokens.hash(a), ControlTokens.hash(a));
        assertNotEquals(ControlTokens.hash(a), ControlTokens.hash(b));
        // The hash must not be reversible to, or contain, the token itself.
        assertFalse(ControlTokens.hash(a).contains(a));
    }

    @Test
    void accessTokenValidatesThenRevokes() {
        DeviceRecord record = device(ControlRole.OPERATOR);
        String token = ControlTokens.issueAccessToken(record);

        ControlTokens.AccessToken resolved = ControlTokens.validateAccessToken(token);
        assertNotNull(resolved);
        assertEquals(ControlRole.OPERATOR, resolved.role());
        assertEquals(record.deviceId(), resolved.deviceId());

        ControlTokens.revokeDevice(record.deviceId());
        assertNull(ControlTokens.validateAccessToken(token),
                "revoking a device must immediately invalidate its live access tokens");
    }

    @Test
    void rejectsGarbageAndEmptyAccessTokens() {
        assertNull(ControlTokens.validateAccessToken(null));
        assertNull(ControlTokens.validateAccessToken(""));
        assertNull(ControlTokens.validateAccessToken("not-a-real-token"));
    }

    @Test
    void revokedCopyAndRoleChangePreserveIdentity() {
        DeviceRecord record = device(ControlRole.READ_ONLY);
        assertTrue(record.revokedCopy().revoked());
        assertEquals(record.deviceId(), record.revokedCopy().deviceId());
        assertEquals(ControlRole.OWNER, record.withRole(ControlRole.OWNER).role());
        assertEquals(record.deviceId(), record.withRole(ControlRole.OWNER).deviceId());
    }
}
