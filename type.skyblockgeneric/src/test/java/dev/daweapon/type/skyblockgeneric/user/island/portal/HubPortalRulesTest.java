package dev.daweapon.type.skyblockgeneric.user.island.portal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HubPortalRulesTest {
    @Test
    void onlyThePlacedHubPortalCountsAsTheHubPortal() {
        assertTrue(HubPortalRules.isHubPortal(IslandPortals.LEGACY_HUB_PORTAL));
        assertFalse(HubPortalRules.isHubPortal(new PlacedPortal("DANGER_1_PORTAL", 0, 100, 0, 0)));
    }

    @Test
    void tradeAndGateMatchTheWiki() {
        assertEquals(100, HubPortalRules.REPLACEMENT_COST);
        assertEquals(5, HubPortalRules.REMOVAL_SKILL_LEVEL);
    }

    @Test
    void theMissingPortalErrorQuotesTheWiki() {
        assertTrue(PortalizerCombine.MISSING_PORTAL_LORE.contains("This travel scroll doesn't"));
        assertTrue(PortalizerCombine.MISSING_PORTAL_LORE.contains("to add one immediately!"));
    }
}
