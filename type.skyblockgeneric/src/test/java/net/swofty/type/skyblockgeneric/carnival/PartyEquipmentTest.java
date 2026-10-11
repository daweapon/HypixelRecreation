package net.swofty.type.skyblockgeneric.carnival;

import net.swofty.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PartyEquipmentTest {
    @Test
    void carnivalCrazeGivesFourPercentPerPieceUpToTwenty() {
        assertEquals(0, PartyEquipment.bonusPercent(0));
        assertEquals(4, PartyEquipment.bonusPercent(1));
        assertEquals(20, PartyEquipment.bonusPercent(5));
        assertEquals(20, PartyEquipment.bonusPercent(9));
    }

    @Test
    void onlyDistinctPartyPiecesCount() {
        assertEquals(PartyEquipment.TOTAL_PIECES, PartyEquipment.countPieces(List.of(ItemType.PARTY_HAT,
                ItemType.PARTY_NECKLACE, ItemType.PARTY_CLOAK, ItemType.PARTY_BELT, ItemType.PARTY_GLOVES)));
        assertEquals(1, PartyEquipment.countPieces(Arrays.asList(ItemType.PARTY_CLOAK, ItemType.PARTY_CLOAK,
                ItemType.SNOW_CLOAK, null)));
    }
}
