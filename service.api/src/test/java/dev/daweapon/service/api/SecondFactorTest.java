package dev.daweapon.service.api;

import dev.daweapon.service.api.control.SecondFactor;
import dev.daweapon.service.api.control.Totp;
import dev.daweapon.service.api.control.TotpStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecondFactorTest {
    private static final long NOW = 1_800_000_000L;

    private static TotpStore.State active(String secret) {
        return new TotpStore.State(secret, true, "daweapon", Instant.now(), null, null, null);
    }

    @Test
    void nothingChangesUntilASecretIsEnrolled() {
        assertEquals(SecondFactor.Verdict.NOT_ENROLLED, SecondFactor.evaluate(TotpStore.State.NONE, null, NOW));
        assertEquals(SecondFactor.Verdict.NOT_ENROLLED, SecondFactor.evaluate(null, "000000", NOW));
    }

    @Test
    void aPendingEnrolmentIsNotEnforced() {
        TotpStore.State pending = new TotpStore.State(null, false, null, null, Totp.newSecret(), "daweapon", Instant.now());
        assertEquals(SecondFactor.Verdict.NOT_ENROLLED, SecondFactor.evaluate(pending, null, NOW));
    }

    @Test
    void onceEnrolledMissingAndWrongCodesAreRefused() {
        String secret = Totp.newSecret();
        TotpStore.State state = active(secret);
        assertEquals(SecondFactor.Verdict.MISSING, SecondFactor.evaluate(state, null, NOW));
        assertEquals(SecondFactor.Verdict.MISSING, SecondFactor.evaluate(state, " ", NOW));

        String good = Totp.codeAt(secret, NOW);
        String bad = good.equals("000000") ? "111111" : "000000";
        assertEquals(SecondFactor.Verdict.INVALID, SecondFactor.evaluate(state, bad, NOW));
        assertEquals(SecondFactor.Verdict.OK, SecondFactor.evaluate(state, good, NOW));
    }
}
