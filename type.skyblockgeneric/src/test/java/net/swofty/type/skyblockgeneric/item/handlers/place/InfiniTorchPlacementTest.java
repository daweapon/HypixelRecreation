package net.swofty.type.skyblockgeneric.item.handlers.place;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfiniTorchPlacementTest {
    @Test
    void torchesCostTwoCoins() {
        assertFalse(InfiniTorchPlacement.canAfford(1.9));
        assertTrue(InfiniTorchPlacement.canAfford(2));
    }
}
