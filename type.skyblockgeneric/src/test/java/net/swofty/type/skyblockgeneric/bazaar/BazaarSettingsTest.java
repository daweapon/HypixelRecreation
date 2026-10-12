package net.swofty.type.skyblockgeneric.bazaar;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointStringList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BazaarSettingsTest {
    @Test
    void ignoredItemsSurviveASerializeRoundTrip() {
        DatapointStringList datapoint = new DatapointStringList("bazaar_ignored_items",
                new java.util.ArrayList<>(BazaarSettings.encode(List.of(ItemType.COAL, ItemType.DIAMOND))));
        DatapointStringList restored = new DatapointStringList("bazaar_ignored_items");
        restored.deserializeValue(datapoint.getSerializedValue());
        assertEquals(Set.of(ItemType.COAL, ItemType.DIAMOND), BazaarSettings.decode(restored.getValue()));
    }

    @Test
    void anEmptyIgnoreListDecodesToNothing() {
        DatapointStringList restored = new DatapointStringList("bazaar_ignored_items");
        restored.deserializeValue(new DatapointStringList("bazaar_ignored_items").getSerializedValue());
        assertTrue(BazaarSettings.decode(restored.getValue()).isEmpty());
    }

    @Test
    void unknownItemNamesAreDropped() {
        assertEquals(Set.of(ItemType.COAL), BazaarSettings.decode(List.of("COAL", "NOT_A_REAL_ITEM")));
    }
}
