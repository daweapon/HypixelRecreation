package dev.daweapon.type.skyblockgeneric.leaderboard;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkyBlockBoardsTest {

    @Test
    void boardIdsAreUniqueAndKeysUseTheBoardPrefix() {
        Set<String> ids = new HashSet<>();
        for (SkyBlockBoard board : SkyBlockBoards.fixed()) {
            assertTrue(ids.add(board.id()), board.id());
            assertEquals("skyblock:board:" + board.id(), board.key());
        }
    }

    @Test
    void everySlayerAndSkillHasABoardAndCosmeticSkillsDoNot() {
        for (SlayerType type : SlayerType.values()) {
            assertTrue(SkyBlockBoards.byId(SkyBlockBoards.slayerId(type)).isPresent(), type.name());
        }
        assertTrue(SkyBlockBoards.byId("skill:FARMING").isPresent());
        assertTrue(SkyBlockBoards.byId("skill:SOCIAL").isEmpty());
        assertTrue(SkyBlockBoards.byId(SkyBlockBoards.LEVEL).isPresent());
    }

    @Test
    void collectionBoardIdsRoundTrip() {
        SkyBlockBoard board = SkyBlockBoards.collection(ItemType.WHEAT);
        assertEquals("collection:WHEAT", board.id());
        assertEquals(board.id(), SkyBlockBoards.byId(board.id()).orElseThrow().id());
        assertTrue(SkyBlockBoards.byId("collection:NOT_AN_ITEM").isEmpty());
    }

    @Test
    void categoriesCoverEveryFixedBoard() {
        for (SkyBlockBoard board : SkyBlockBoards.fixed()) {
            assertTrue(board.category() != BoardCategory.COLLECTIONS, board.id());
        }
    }
}
