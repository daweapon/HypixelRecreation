package net.swofty.type.skyblockgeneric.item;

import net.swofty.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemTypeResolverTest {
    private static final Map<String, String> NAMES = Map.of(
            "FIREDUST_DAGGER", "Firedust Dagger",
            "MAWDUST_DAGGER", "Twilight Dagger",
            "BURSTFIRE_DAGGER", "Kindlebane Dagger",
            "BURSTMAW_DAGGER", "Mawdredge Dagger",
            "HEARTFIRE_DAGGER", "Pyrochaos Dagger",
            "HEARTMAW_DAGGER", "Deathripper Dagger");

    private static ItemType resolve(String input) {
        return ItemTypeResolver.resolve(input, NAMES::get);
    }

    @Test
    void resolvesEveryAttunementDaggerByIdAndByName() {
        assertEquals(ItemType.FIREDUST_DAGGER, resolve("firedust_dagger"));
        assertEquals(ItemType.BURSTFIRE_DAGGER, resolve("BURSTFIRE_DAGGER"));
        assertEquals(ItemType.BURSTFIRE_DAGGER, resolve("KINDLEBANE_DAGGER"));
        assertEquals(ItemType.HEARTFIRE_DAGGER, resolve("pyrochaos_dagger"));
        assertEquals(ItemType.HEARTFIRE_DAGGER, resolve("HEARTFIRE_DAGGER"));
        assertEquals(ItemType.MAWDUST_DAGGER, resolve("twilight_dagger"));
        assertEquals(ItemType.MAWDUST_DAGGER, resolve("MAWDUST_DAGGER"));
        assertEquals(ItemType.BURSTMAW_DAGGER, resolve("mawdredge-dagger"));
        assertEquals(ItemType.HEARTMAW_DAGGER, resolve("DeathripperDagger"));
    }

    @Test
    void unknownInputResolvesToNothing() {
        assertNull(resolve("NOT_A_REAL_ITEM_AT_ALL"));
    }
}
