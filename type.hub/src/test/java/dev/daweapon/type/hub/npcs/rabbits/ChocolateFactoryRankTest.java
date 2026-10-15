package dev.daweapon.type.hub.npcs.rabbits;

import net.kyori.adventure.text.format.NamedTextColor;
import dev.daweapon.commons.text.Text;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChocolateFactoryRankTest {
    @Test
    void fromLevelPicksHighestRankAtOrBelowLevel() {
        assertEquals(ChocolateFactoryRank.UNEMPLOYED, ChocolateFactoryRank.fromLevel(0));
        assertEquals(ChocolateFactoryRank.INTERN, ChocolateFactoryRank.fromLevel(1));
        assertEquals(ChocolateFactoryRank.INTERN, ChocolateFactoryRank.fromLevel(19));
        assertEquals(ChocolateFactoryRank.EMPLOYEE, ChocolateFactoryRank.fromLevel(20));
        assertEquals(ChocolateFactoryRank.EMPLOYEE, ChocolateFactoryRank.fromLevel(119));
        assertEquals(ChocolateFactoryRank.ASSISTANT, ChocolateFactoryRank.fromLevel(120));
        assertEquals(ChocolateFactoryRank.EXECUTIVE, ChocolateFactoryRank.fromLevel(219));
        assertEquals(ChocolateFactoryRank.BOARD_MEMBER, ChocolateFactoryRank.fromLevel(220));
        assertEquals(ChocolateFactoryRank.BOARD_MEMBER, ChocolateFactoryRank.fromLevel(5000));
    }

    @Test
    void negativeLevelsStayUnemployed() {
        assertEquals(ChocolateFactoryRank.UNEMPLOYED, ChocolateFactoryRank.fromLevel(-10));
    }

    @Test
    void rankThresholdsStrictlyIncrease() {
        ChocolateFactoryRank[] ranks = ChocolateFactoryRank.values();
        for (int i = 1; i < ranks.length; i++) {
            assertTrue(ranks[i].getLevel() > ranks[i - 1].getLevel(), ranks[i].name());
        }
    }

    @Test
    void everyRankResolvesFromItsOwnThreshold() {
        for (ChocolateFactoryRank rank : ChocolateFactoryRank.values()) {
            assertEquals(rank, ChocolateFactoryRank.fromLevel(rank.getLevel()));
        }
    }

    @Test
    void hologramShowsLevelOnlyForEmployedRanks() {
        assertEquals(Text.of("<color:{}>{}", NamedTextColor.RED, "Unemployed"),
                ChocolateFactoryRank.UNEMPLOYED.getHologramLine());
        assertEquals(Text.of("<7>[{}] <color:{}>{}", 150, NamedTextColor.DARK_PURPLE, "Manager"),
                ChocolateFactoryRank.MANAGER.getHologramLine(150));
    }
}
