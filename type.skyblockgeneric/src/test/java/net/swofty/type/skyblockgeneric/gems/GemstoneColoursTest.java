package net.swofty.type.skyblockgeneric.gems;

import net.swofty.type.skyblockgeneric.commission.Commission;
import net.swofty.type.skyblockgeneric.commission.CommissionReward;
import net.swofty.type.skyblockgeneric.commission.Commissions;
import net.swofty.type.skyblockgeneric.commission.Objective;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GemstoneColoursTest {
    @Test
    void gemstonesUseTheirHypixelColours() {
        assertEquals('c', GemstoneColours.code(Gemstone.RUBY));
        assertEquals('6', GemstoneColours.code(Gemstone.AMBER));
        assertEquals('b', GemstoneColours.code(Gemstone.SAPPHIRE));
        assertEquals('a', GemstoneColours.code(Gemstone.JADE));
        assertEquals('5', GemstoneColours.code(Gemstone.AMETHYST));
        assertEquals('e', GemstoneColours.code(Gemstone.TOPAZ));
        assertEquals('d', GemstoneColours.code(Gemstone.JASPER));
        assertEquals('f', GemstoneColours.code(Gemstone.OPAL));
        for (Gemstone gemstone : Gemstone.values()) {
            assertTrue(GemstoneColours.tag(gemstone).length() == 3, gemstone.name());
        }
    }

    @Test
    void gemstoneEnumColoursMatchTheTable() {
        assertEquals(net.kyori.adventure.text.format.NamedTextColor.RED, Gemstone.RUBY.color);
        assertEquals(net.kyori.adventure.text.format.NamedTextColor.AQUA, Gemstone.SAPPHIRE.color);
    }

    @Test
    void rubyCommissionNameIsRed() {
        Commission ruby = Commissions.getCommissionByName("Ruby Gemstone Collector");
        assertEquals("<c>", ruby.nameColourTag());
        assertEquals("<b>", Commissions.getCommissionByName("Sapphire Gemstone Collector").nameColourTag());
        assertEquals("<9>", Commissions.getCommissionByName("Yog Slayer").nameColourTag());
        assertNull(GemstoneColours.of(Objective.BlockTarget.HARD_STONE));
    }

    @Test
    void hollowsCommissionsPayGemstonePowderNotMithril() {
        CommissionReward reward = CommissionReward.calculateHollows(5, false, false);
        assertEquals(0, reward.mithrilPowder());
        assertEquals(0, reward.glacitePowder());
        assertEquals(1_000, reward.gemstonePowder());
        assertEquals(2_000, CommissionReward.calculateHollows(10, false, false).gemstonePowder());
        assertTrue(reward.miningXp() >= 10_000 && reward.miningXp() <= 20_000);
        assertTrue(CommissionReward.getHollowsRewardLore(5, false, false).stream().anyMatch(t -> t.plain().contains("Gemstone Powder")));
        assertTrue(CommissionReward.getHollowsRewardLore(5, false, false).stream().noneMatch(t -> t.plain().contains("Mithril")));
    }
}
