package dev.daweapon.type.skyblockgeneric.item;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemTypeRegistryTest {
    @Test
    void everyConstantIsRegisteredOnceInOrdinalOrder() {
        ItemType[] values = ItemType.values();
        Set<String> names = new HashSet<>();
        for (int i = 0; i < values.length; i++) {
            assertEquals(i, values[i].ordinal());
            assertTrue(names.add(values[i].name()), values[i].name());
            assertSame(values[i], ItemType.valueOf(values[i].name()));
        }
        assertTrue(values.length > 3_000);
    }

    @Test
    void constantsFromEveryChunkResolveToTheSameInstances() {
        ItemType[] values = ItemType.values();
        assertSame(ItemType.AATROX_BATPHONE, ItemType.valueOf("AATROX_BATPHONE"));
        assertSame(ItemType.ZOMBIE_TALISMAN, ItemType.valueOf("ZOMBIE_TALISMAN"));
        assertSame(ItemType.PARTY_THE_FISH, ItemType.get("minecraft:party_the_fish"));
        assertTrue(ItemType.AATROX_BATPHONE.compareTo(ItemType.ZOMBIE_TALISMAN) < 0);
    }

    @Test
    void lookupsBehaveLikeTheOldEnum() {
        assertThrows(IllegalArgumentException.class, () -> ItemType.valueOf("NOT_AN_ITEM"));
        assertThrows(NullPointerException.class, () -> ItemType.valueOf(null));
        assertNull(ItemType.get("not_an_item"));
        assertEquals("PARTY_CLOAK", ItemType.PARTY_CLOAK.toString());
        assertEquals(Material.PAPER, ItemType.PARTY_CLOAK.material);
        assertEquals(Rarity.SPECIAL, ItemType.PARTY_THE_FISH.rarity);
    }
}
