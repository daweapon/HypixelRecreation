package net.swofty.type.skyblockgeneric.museum;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointMuseum;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MuseumMaxGrantTest {
    private static final MuseumCatalogue CATALOGUE =
            MuseumCatalogue.fromFile(new File("../configuration/skyblock/museum/catalogue.yml"));

    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
    }

    @Test
    void maxProfileStoresACopyOfEveryDonatableEntry() {
        DatapointMuseum.MuseumData data = new DatapointMuseum.MuseumData();
        int granted = MuseumService.grantCopies(CATALOGUE, data);

        int expected = 0;
        for (MuseumEntry entry : CATALOGUE.all()) {
            if (!entry.isAvailable()) continue;
            expected += entry.requiredItemTypes().size();
            assertEquals(entry.requiredItemTypes().size(), data.itemsFor(entry, true).size(), entry.id());
            assertTrue(data.isCredited(entry.id()), entry.id());
        }
        assertEquals(expected, granted);
        assertEquals(expected, data.getCurrentlyInMuseum().size());
        assertTrue(data.getCurrentlyInMuseum().stream().allMatch(data::isStaffGranted));

        long specials = CATALOGUE.entries(MuseumCategory.SPECIAL).stream()
                .filter(MuseumEntry::isAvailable).count();
        assertTrue(specials > MuseumCategory.SPECIAL_ITEM_LIMIT, "the Special cap is bypassed only if it is exceeded");
        assertEquals(specials, CATALOGUE.entries(MuseumCategory.SPECIAL).stream()
                .filter(entry -> data.isCredited(entry.id())).count());
    }

    @Test
    void secondRunAddsNothing() {
        DatapointMuseum.MuseumData data = new DatapointMuseum.MuseumData();
        MuseumService.grantCopies(CATALOGUE, data);
        Set<String> before = trackedIds(data);

        assertEquals(0, MuseumService.grantCopies(CATALOGUE, data));
        assertEquals(before, trackedIds(data));
    }

    @Test
    void staffCopiesStayUnboundThroughRetrieveAndRedonate() {
        DatapointMuseum.MuseumData data = new DatapointMuseum.MuseumData();
        MuseumService.grantCopies(CATALOGUE, data);
        data.getCurrentlyInMuseum().forEach(item -> assertNull(soulBound(item)));

        MuseumEntry entry = CATALOGUE.entry("ASPECT_OF_THE_END").orElseThrow();
        SkyBlockItem copy = data.itemsFor(entry, true).getFirst();
        data.moveToRetrieved(copy);
        assertEquals(List.of(copy), data.itemsFor(entry, false));

        MuseumService.bindOnDonation(data, entry, copy);
        data.add(entry, copy);
        assertNull(soulBound(copy));
        assertTrue(data.isStaffGranted(copy));
        assertEquals(List.of(copy), data.itemsFor(entry, true));
        assertTrue(data.itemsFor(entry, false).isEmpty());
    }

    @Test
    void aPlayersOwnDonationStillBinds() {
        DatapointMuseum.MuseumData data = new DatapointMuseum.MuseumData();
        MuseumEntry entry = CATALOGUE.entry("ASPECT_OF_THE_END").orElseThrow();
        SkyBlockItem item = tracked(ItemType.ASPECT_OF_THE_END);
        MuseumService.bindOnDonation(data, entry, item);
        assertNotNull(soulBound(item));

        MuseumEntry special = CATALOGUE.entry("SNOW_NECKLACE").filter(found -> found.category().isSpecial()).orElseThrow();
        SkyBlockItem necklace = tracked(ItemType.SNOW_NECKLACE);
        MuseumService.bindOnDonation(data, special, necklace);
        assertNull(soulBound(necklace));
    }

    @Test
    void playersOwnDonationsAndRetrievalsAreLeftAlone() {
        DatapointMuseum.MuseumData data = new DatapointMuseum.MuseumData();
        MuseumEntry donated = CATALOGUE.entry("ASPECT_OF_THE_END").orElseThrow();
        SkyBlockItem own = tracked(ItemType.ASPECT_OF_THE_END);
        data.add(donated, own);

        MuseumEntry retrievedEntry = CATALOGUE.entry("LAPIS_ARMOR").orElseThrow();
        List<SkyBlockItem> lapis = retrievedEntry.requiredItemTypes().stream().map(MuseumMaxGrantTest::tracked).toList();
        lapis.forEach(piece -> data.add(retrievedEntry, piece));
        lapis.forEach(data::moveToRetrieved);

        MuseumService.grantCopies(CATALOGUE, data);

        assertEquals(List.of(own), data.itemsFor(donated, true));
        assertFalse(data.isStaffGranted(own));
        assertTrue(data.itemsFor(retrievedEntry, true).isEmpty());
        assertEquals(lapis, data.itemsFor(retrievedEntry, false));
    }

    @Test
    void snowSuitAndSnowSpecialsKeepTheirOwnItems() {
        MuseumEntry suit = CATALOGUE.entry("SNOW_SUIT").orElseThrow();
        List<MuseumEntry> specials = CATALOGUE.entries(MuseumCategory.SPECIAL).stream()
                .filter(entry -> suit.pieces().contains(entry.id()))
                .toList();
        assertEquals(4, specials.size());
        specials.forEach(special -> assertTrue(
                CATALOGUE.entriesFor(ItemType.get(special.id())).containsAll(List.of(suit, special)), special.id()));

        DatapointMuseum.MuseumData data = new DatapointMuseum.MuseumData();
        suit.requiredItemTypes().forEach(type -> data.add(suit, tracked(type)));
        for (MuseumEntry special : specials) {
            data.add(special, tracked(ItemType.get(special.id())));
        }

        assertEquals(suit.pieces().size(), data.itemsFor(suit, true).size());
        for (MuseumEntry special : specials) {
            assertEquals(1, data.itemsFor(special, true).size(), special.id());
            assertFalse(data.itemsFor(suit, true).contains(data.itemsFor(special, true).getFirst()));
        }

        DatapointMuseum.MuseumData granted = new DatapointMuseum.MuseumData();
        MuseumService.grantCopies(CATALOGUE, granted);
        assertEquals(suit.pieces().size(), granted.itemsFor(suit, true).size());
        specials.forEach(special -> assertEquals(1, granted.itemsFor(special, true).size(), special.id()));
    }

    @Test
    void legacyDonationsAreAdoptedByTheirOriginalEntry() {
        DatapointMuseum.MuseumData data = new DatapointMuseum.MuseumData();
        SkyBlockItem legacyNecklace = tracked(ItemType.SNOW_NECKLACE);
        data.getCurrentlyInMuseum().add(legacyNecklace);
        MuseumEntry special = CATALOGUE.entries(MuseumCategory.SPECIAL).stream()
                .filter(entry -> entry.id().equals("SNOW_NECKLACE")).findFirst().orElseThrow();

        MuseumService.adopt(CATALOGUE, data);

        assertEquals("SNOW_SUIT", data.ownerId(legacyNecklace));
        assertTrue(data.itemsFor(special, true).isEmpty());
    }

    @Test
    void ownersAndStaffFlagsSurviveSaving() {
        DatapointMuseum.MuseumData data = new DatapointMuseum.MuseumData();
        MuseumEntry suit = CATALOGUE.entry("SNOW_SUIT").orElseThrow();
        suit.requiredItemTypes().forEach(type -> data.grant(suit, tracked(type)));

        DatapointMuseum.MuseumData loaded = DatapointMuseum.MuseumData.deserialize(new JSONObject(data.serialize().toString()));

        assertEquals(data.getEntryOf(), loaded.getEntryOf());
        assertEquals(data.getStaffGranted(), loaded.getStaffGranted());
        assertEquals(suit.pieces().size(), loaded.getStaffGranted().size());
    }

    @Test
    void profileSizeAfterMaxProfileStaysSmall() {
        DatapointMuseum.MuseumData data = new DatapointMuseum.MuseumData();
        MuseumService.grantCopies(CATALOGUE, data);
        int bytes = data.serialize().toString().getBytes().length;
        assertTrue(bytes < 2_000_000, "a maxed Museum serialises to " + bytes + " bytes");
        System.out.println("Maxed Museum: " + data.getCurrentlyInMuseum().size() + " items, " + bytes + " bytes");
    }

    private static SkyBlockItem tracked(ItemType type) {
        SkyBlockItem item = new SkyBlockItem(type);
        item.getAttribute("unique-tracked-id").setValue(UUID.randomUUID().toString());
        return item;
    }

    private static Object soulBound(SkyBlockItem item) {
        return item.getAttribute("soul_bound").getValue();
    }

    private static Set<String> trackedIds(DatapointMuseum.MuseumData data) {
        Set<String> ids = new HashSet<>();
        data.getAllItems().forEach(item -> ids.add(item.getAttributeHandler().getUniqueTrackedID()));
        return ids;
    }
}
