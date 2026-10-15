package dev.daweapon.type.skyblockgeneric.data;

import dev.daweapon.commons.skyblock.CoopLinks;
import dev.daweapon.commons.skyblock.SkyBlockProfileFields;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.type.generic.data.mongodb.ProfilesDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileWipeCoverageTest {
    @BeforeAll
    static void registerItemAttributes() {
        ItemAttribute.registerItemAttributes();
    }

    @Test
    void storesEveryPrivateDatapointOnTheProfileDocumentTheWipeDeletes() {
        List<SkyBlockDataHandler.Data> profileBacked = Arrays.stream(SkyBlockDataHandler.Data.values())
                .filter(data -> data.coopField() == null)
                .toList();

        assertTrue(profileBacked.contains(SkyBlockDataHandler.Data.COINS));
        assertTrue(profileBacked.contains(SkyBlockDataHandler.Data.INVENTORY));
        assertTrue(profileBacked.contains(SkyBlockDataHandler.Data.AUCTION_DELIVERED_CLAIMS));
        profileBacked.forEach(data -> {
            assertNotNull(data.profileField());
            assertEquals(SkyBlockProfileFields.NAMESPACE, data.profileField().namespace());
        });
    }

    @Test
    void storesEveryCoopSharedDatapointOnTheCoopTheWipeOnlyEndsWhenItEmpties() {
        List<SkyBlockDataHandler.Data> coopBacked = Arrays.stream(SkyBlockDataHandler.Data.values())
                .filter(data -> data.coopField() != null)
                .toList();

        assertFalse(coopBacked.isEmpty());
        assertTrue(coopBacked.contains(SkyBlockDataHandler.Data.BANK_DATA));
        assertTrue(coopBacked.contains(SkyBlockDataHandler.Data.ISLAND_UUID));
        coopBacked.forEach(data -> {
            assertNull(data.profileField());
            assertSame(CoopLinks.COOP, data.coopField().linkType());
        });
    }

    @Test
    void addressesTheIslandTheWipeReadsUnderTheSameKeyTheProfileWritesIt() {
        assertEquals(SkyBlockDataHandler.Data.ISLAND_UUID.coopField().fullKey(),
                SkyBlockProfileFields.ISLAND_UUID.fullKey());
        assertEquals(SkyBlockDataHandler.Data.ISLAND_UUID.getKey(), SkyBlockProfileFields.ISLAND_UUID_KEY);
        assertSame(SkyBlockProfileFields.DOCUMENT, ProfilesDatabase.DOCUMENT);
    }
}
