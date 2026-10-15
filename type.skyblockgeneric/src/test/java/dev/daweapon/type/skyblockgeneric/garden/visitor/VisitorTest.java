package dev.daweapon.type.skyblockgeneric.garden.visitor;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisitorTest {
    @BeforeAll
    static void load() {
        VisitorCatalogue.load(Path.of("../configuration/skyblock/garden"));
    }

    @Test
    void allHundredAndFortyVisitorsLoadWithUniqueIds() {
        List<VisitorCatalogue.Visitor> visitors = VisitorCatalogue.get().visitors();
        assertEquals(140, visitors.size());
        assertEquals(140, visitors.stream().map(VisitorCatalogue.Visitor::id).distinct().count());
        visitors.forEach(visitor -> assertTrue(visitor.gardenLevel() >= 1 && visitor.gardenLevel() <= 15, visitor.id()));
    }

    @Test
    void everyOfferIsPositiveAndUsesRealItems() {
        VisitorCatalogue catalogue = VisitorCatalogue.get();
        Random random = new Random(7);
        for (VisitorCatalogue.Visitor visitor : catalogue.visitors()) {
            for (int level : new int[]{1, 8, 15}) {
                VisitorOffer offer = VisitorGenerator.generate(catalogue, visitor, level, random);
                assertFalse(offer.required().isEmpty(), visitor.id());
                for (VisitorOffer.Required required : offer.required()) {
                    assertTrue(required.amount() > 0, visitor.id());
                    assertNotNull(ItemType.get(required.item()), visitor.id() + " " + required.item());
                }
                assertTrue(offer.copper() >= VisitorGenerator.MINIMUM_COPPER, visitor.id());
                assertTrue(offer.gardenXp() > 0, visitor.id());
            }
        }
    }

    @Test
    void compactionUsesTheHighestTierThatFits() {
        VisitorCatalogue.Family wheat = VisitorCatalogue.get().family("WHEAT");
        VisitorOffer.Required small = VisitorGenerator.compact(wheat, 800);
        assertEquals(wheat.items().get(wheat.items().size() - 1).rawUnits() <= 800 ? wheat.top().item() : small.item(), small.item());
        assertTrue(small.amount() >= 1);
        VisitorOffer.Required large = VisitorGenerator.compact(wheat, 20_000_000);
        assertEquals(wheat.top().item(), large.item());
    }

    @Test
    void freshGardenSpawnsAnEligibleVisitorAndRespectsTheCap() {
        GardenProfile profile = new GardenProfile();
        long now = 1_000_000_000L;
        VisitorService.advance(profile, null, now);
        assertTrue(profile.visitors().getNextVisitorAt() > now);
        VisitorService.advance(profile, null, now + 100L * 60_000);
        assertEquals(VisitorData.MAX_PRESENT, profile.visitors().getPresent().size());
        assertEquals(VisitorData.MAX_VISIBLE, profile.visitors().visible().size());
        Set<String> ids = new HashSet<>();
        profile.visitors().getPresent().forEach(present -> assertTrue(ids.add(present.getId())));
        for (VisitorData.Present present : profile.visitors().getPresent()) {
            VisitorCatalogue.Visitor visitor = VisitorCatalogue.get().visitor(present.getId());
            assertTrue(VisitorService.eligible(profile, visitor), present.getId());
        }
    }

    @Test
    void visitorDataRoundTrips() {
        GardenProfile profile = new GardenProfile();
        VisitorService.advance(profile, null, 5_000L);
        VisitorService.advance(profile, null, 5_000L + 40L * 60_000);
        profile.visitors().markServed("ARTHUR");
        profile.visitors().getTalkedTo().add("jacob");
        GardenProfile copy = GardenProfile.deserialize(profile.serialize());
        assertEquals(profile.visitors().getPresent().size(), copy.visitors().getPresent().size());
        assertEquals(1, copy.visitors().uniqueServed());
        assertTrue(copy.visitors().getTalkedTo().contains("jacob"));
        assertEquals(profile.visitors().getNextVisitorAt(), copy.visitors().getNextVisitorAt());
    }

    @Test
    void cooldownShortensWithUniqueVisitorsServed() {
        VisitorCatalogue catalogue = VisitorCatalogue.get();
        assertEquals(15, catalogue.cooldownMinutes(0));
        assertEquals(12, catalogue.cooldownMinutes(50));
        assertEquals(10, catalogue.cooldownMinutes(120));
    }
}
