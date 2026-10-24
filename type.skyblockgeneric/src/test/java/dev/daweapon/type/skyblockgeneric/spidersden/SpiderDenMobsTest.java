package dev.daweapon.type.skyblockgeneric.spidersden;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.CataloguedBestiaryAreas;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpiderDenMobsTest {
    private static final Path REGIONS = Path.of("../configuration/skyblock/Minestom.regions.yml");

    @Test
    void twentyFourVariantsInTotal() {
        assertEquals(24, SpiderDenMobs.all().size());
    }

    @Test
    void everyBestiaryProfileMatchesACataloguedFamily() {
        Set<String> families = Arrays.stream(CataloguedBestiaryAreas.SPIDERS_DEN)
                .flatMap(entry -> entry.getMobs().stream()).map(mob -> mob.getMobID()).collect(Collectors.toSet());
        for (CrimsonMobProfile profile : SpiderDenMobs.all()) {
            if (profile.inBestiary()) assertTrue(families.contains(profile.id()), profile.id());
        }
    }

    @Test
    void everyDropIsARealItem() {
        for (CrimsonMobProfile profile : SpiderDenMobs.all()) {
            for (MobDrop drop : profile.drops()) {
                for (String id : drop.choices()) assertTrue(ItemType.get(id) != null, profile.id() + " drops unknown " + id);
            }
        }
    }

    @Test
    void wikiNumbers() {
        assertEquals(170, SpiderDenMobs.DASHER_MOUND.health());
        assertEquals(41, SpiderDenMobs.BROODMOTHER.level());
        assertEquals(3_000, SpiderDenMobs.BROODMOTHER.health());
        assertEquals(307, SpiderDenMobs.BROODMOTHER.combatXp());
        assertEquals(List.of(250.0, 300.0, 360.0), SpiderDenMobs.DASHER_BURROW.stream().map(CrimsonMobProfile::health).toList());
        assertEquals(440, SpiderDenMobs.SPLITTER_BURROW.get(2).health());
    }

    @Test
    void broodmotherRareRowsIgnoreMagicFindAndTheCapIs300() {
        for (MobDrop drop : SpiderDenMobs.BROODMOTHER.drops()) {
            if (drop.chancePercent() < 100) assertEquals(false, drop.magicFind(), drop.choices().toString());
        }
        assertEquals(4, SpiderDenMobs.BROODMOTHER.drops().stream().filter(drop -> drop.chancePercent() < 100).count());
    }

    @Test
    void bestiaryMobsNeverCorrupt() {
        for (CrimsonMobProfile profile : SpiderDenMobs.all()) assertEquals(false, profile.corruptible(), profile.id());
    }

    @Test
    void allEightZonesHaveBoxes() throws Exception {
        String yaml = Files.readString(REGIONS);
        for (RegionType zone : List.of(RegionType.SPIDERS_DEN, RegionType.SPIDER_MOUND, RegionType.ARACHNES_BURROW,
                RegionType.ARACHNES_SANCTUARY, RegionType.GRAVEL_MINES, RegionType.GRANDMAS_HOUSE, RegionType.MICHAELS_CRIB,
                RegionType.ARCHAEOLOGISTS_CAMP)) {
            assertTrue(yaml.contains("type: " + zone.name() + "\n    x1:") && yaml.contains("SKYBLOCK_SPIDERS_DEN"), zone.name());
        }
    }

    @Test
    void broodmotherRespawnsAfterTwoAndAHalfMinutes() {
        assertEquals(150_000, SpiderDenMobSpawner.BROODMOTHER_RESPAWN_MILLIS);
    }
}
