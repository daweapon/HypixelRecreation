package dev.daweapon.type.skyblockgeneric.fishing.bestiary;

import dev.daweapon.type.skyblockgeneric.bestiary.BestiaryData;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfile;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfiles;
import dev.daweapon.type.skyblockgeneric.fishing.festival.FestivalShark;
import dev.daweapon.type.skyblockgeneric.fishing.winter.WinterSeaCreature;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FishingBestiaryFamilyTest {

    @Test
    void sectionFamilyCountsMatchHypixel() {
        assertEquals(24, FishingBestiaryFamily.in(FishingBestiarySection.FISHING).length);
        assertEquals(19, FishingBestiaryFamily.in(FishingBestiarySection.LAVA).length);
        assertEquals(6, FishingBestiaryFamily.in(FishingBestiarySection.SPOOKY_FESTIVAL).length);
        assertEquals(4, FishingBestiaryFamily.in(FishingBestiarySection.FISHING_FESTIVAL).length);
        assertEquals(6, FishingBestiaryFamily.in(FishingBestiarySection.WINTER).length);
        assertEquals(6, FishingBestiaryFamily.in(FishingBestiarySection.BACKWATER_BAYOU).length);
        assertEquals(6, FishingBestiaryFamily.in(FishingBestiarySection.TORRHUS_CANYON).length);
        assertEquals(71, FishingBestiaryFamily.values().length);
    }

    @Test
    void familiesAreListedAlphabeticallyWithinEachSection() {
        for (FishingBestiarySection section : FishingBestiarySection.values()) {
            List<String> names = Arrays.stream(FishingBestiaryFamily.in(section))
                    .map(FishingBestiaryFamily::getFamilyName).toList();
            List<String> sorted = new ArrayList<>(names);
            sorted.sort(String.CASE_INSENSITIVE_ORDER);
            assertEquals(sorted, names, section.name());
        }
    }

    @Test
    void killsToMaxMatchTheWiki() {
        BestiaryData data = new BestiaryData();
        Map<FishingBestiaryFamily, Integer> wiki = Map.ofEntries(
                Map.entry(FishingBestiaryFamily.SQUID, 1_000),
                Map.entry(FishingBestiaryFamily.MITHRIL_GRUBBER, 300),
                Map.entry(FishingBestiaryFamily.WATER_HYDRA, 250),
                Map.entry(FishingBestiaryFamily.CATFISH, 750),
                Map.entry(FishingBestiaryFamily.GUARDIAN_DEFENDER, 500),
                Map.entry(FishingBestiaryFamily.WIKI_TIKI, 50),
                Map.entry(FishingBestiaryFamily.PLHLEGBLAST, 7),
                Map.entry(FishingBestiaryFamily.LORD_JAWBUS, 50),
                Map.entry(FishingBestiaryFamily.TAURUS, 500),
                Map.entry(FishingBestiaryFamily.NURSE_SHARK, 1_000),
                Map.entry(FishingBestiaryFamily.GREAT_WHITE_SHARK, 250),
                Map.entry(FishingBestiaryFamily.REINDRAKE, 50),
                Map.entry(FishingBestiaryFamily.YETI, 250),
                Map.entry(FishingBestiaryFamily.TITANOBOA, 50),
                Map.entry(FishingBestiaryFamily.BANSHEE, 750));
        wiki.forEach((family, kills) ->
                assertEquals(kills, data.getTotalKillsForMaxTier(family.getBracket(), family.getMaxTier()), family.name()));
        for (FishingBestiaryFamily family : FishingBestiaryFamily.values()) {
            assertTrue(data.getTotalKillsForMaxTier(family.getBracket(), family.getMaxTier()) > 0, family.name());
        }
    }

    @Test
    void existingSharkAndWinterKillKeysArePreserved() {
        for (FestivalShark shark : FestivalShark.values()) {
            FishingBestiaryFamily family = FishingBestiaryFamily.forMobKey(shark.name()).orElseThrow();
            assertEquals(FishingBestiarySection.FISHING_FESTIVAL, family.getSection());
            assertEquals(List.of(shark.name()), family.getMobKeys());
            assertEquals(shark.getBestiary().bracket(), family.getBracket());
            assertEquals(shark.getBestiary().maxTier(), family.getMaxTier());
        }
        for (WinterSeaCreature creature : WinterSeaCreature.values()) {
            FishingBestiaryFamily family = FishingBestiaryFamily.forMobKey(creature.name()).orElseThrow();
            assertEquals(FishingBestiarySection.WINTER, family.getSection());
            assertEquals(List.of(creature.name()), family.getMobKeys());
            assertEquals(creature.getBestiary().bracket(), family.getBracket());
            assertEquals(creature.getBestiary().maxTier(), family.getMaxTier());
        }
        assertEquals(FishingBestiaryFamily.FROSTY, FishingBestiaryFamily.forMobKey("FROSTY_THE_SNOWMAN").orElseThrow());
    }

    @Test
    void spawnableSeaCreaturesMapOntoTheirFamilies() {
        assertEquals(FishingBestiaryFamily.SEA_WALKER, FishingBestiaryFamily.forMobKey("SEA_WALKER").orElseThrow());
        assertEquals(FishingBestiaryFamily.INKLING, FishingBestiaryFamily.forMobKey("NIGHT_SQUID").orElseThrow());
        assertEquals(FishingBestiaryFamily.RIDER_OF_THE_DEEP, FishingBestiaryFamily.forMobKey("MONSTER_OF_THE_DEEP").orElseThrow());
        assertEquals(FishingBestiaryFamily.LORD_JAWBUS, FishingBestiaryFamily.forMobKey("LORD_JAWBUS").orElseThrow());
        assertTrue(FishingBestiaryFamily.forMobKey("THE_SEA_EMPEROR").isEmpty());
        assertTrue(FishingBestiaryFamily.AGARIMOO.getMobKeys().isEmpty());
    }

    @Test
    void everyMobKeyIsUniqueAndSpawnable() {
        Set<String> spawnable = new HashSet<>();
        SeaCreatureProfiles.CANONICAL.stream().map(SeaCreatureProfile::id).forEach(spawnable::add);
        Arrays.stream(FestivalShark.values()).map(Enum::name).forEach(spawnable::add);
        Arrays.stream(WinterSeaCreature.values()).map(Enum::name).forEach(spawnable::add);
        Arrays.stream(dev.daweapon.type.skyblockgeneric.fishing.lava.LavaSeaCreature.values()).map(Enum::name).forEach(spawnable::add);

        List<String> keys = Arrays.stream(FishingBestiaryFamily.values())
                .flatMap(family -> family.getMobKeys().stream()).toList();
        assertEquals(keys.size(), keys.stream().collect(Collectors.toSet()).size());
        assertTrue(spawnable.containsAll(keys), () -> "Unknown keys: " + keys.stream().filter(k -> !spawnable.contains(k)).toList());
    }
}
