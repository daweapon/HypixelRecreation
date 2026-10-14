package net.swofty.pvp.feature.knockback;

import net.minestom.server.ServerFlag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KnockbackSettingsTest {
    private static final double TPS = ServerFlag.SERVER_TICKS_PER_SECOND;

    @Test
    void defaultsAreScaledFromPerTickToPerSecond() {
        KnockbackSettings settings = KnockbackSettings.DEFAULT;
        assertEquals(0.4 * TPS, settings.horizontal(), 1e-9);
        assertEquals(0.4 * TPS, settings.vertical(), 1e-9);
        assertEquals(0.4 * TPS, settings.verticalLimit(), 1e-9);
        assertEquals(0.5 * TPS, settings.extraHorizontal(), 1e-9);
        assertEquals(0.1 * TPS, settings.extraVertical(), 1e-9);
    }

    @Test
    void builderOverridesIndividualValues() {
        KnockbackSettings settings = KnockbackSettings.builder()
                .horizontal(0.6).vertical(0.35).verticalLimit(0.5)
                .extraHorizontal(0.7).extraVertical(0.2).build();
        assertEquals(0.6 * TPS, settings.horizontal(), 1e-9);
        assertEquals(0.35 * TPS, settings.vertical(), 1e-9);
        assertEquals(0.5 * TPS, settings.verticalLimit(), 1e-9);
        assertEquals(0.7 * TPS, settings.extraHorizontal(), 1e-9);
        assertEquals(0.2 * TPS, settings.extraVertical(), 1e-9);
    }

    @Test
    void builderKeepsDefaultsForUntouchedFields() {
        KnockbackSettings settings = KnockbackSettings.builder().horizontal(1.0).build();
        assertEquals(1.0 * TPS, settings.horizontal(), 1e-9);
        assertEquals(KnockbackSettings.DEFAULT.vertical(), settings.vertical(), 1e-9);
    }
}
