package dev.daweapon.type.skyblockgeneric.accessory;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.accessory.effect.AccessoryEffect;
import dev.daweapon.type.skyblockgeneric.accessory.effect.AccessoryEffectKind;
import dev.daweapon.type.skyblockgeneric.item.ItemConfigParser;
import dev.daweapon.type.skyblockgeneric.item.Recombobulation;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AccessoryComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessoriesBatch2Test {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");

    private static final List<String> BATCH = List.of(
            "ACCRETION_ARTIFACT", "ACCRETION_RING", "ACCRETION_TALISMAN", "APPLICANT_STATEMENT", "ATMOSPHERIC_FILTER",
            "BEASTSLAYERS_CODEX", "BESTIARY_TALISMAN", "BINGO_COMBAT_TALISMAN", "BLUERTOOTH_RING", "BLUETOOTH_RING",
            "BOOK_OF_PROGRESSION", "CENTURY_ARTIFACT", "COMBO_MANIA_TALISMAN", "DEVOUR_RING", "ETERNAL_HOOF",
            "EXPERIENCE_ARTIFACT", "FISH_AFFINITY_TALISMAN", "GENERAL_MEDALLION", "GRATITUDE_ARTIFACT", "GRATITUDE_RING",
            "GRATITUDE_TALISMAN", "HANDY_BLOOD_CHALICE", "HOCUS_POCUS_CIPHER", "HOTSPOT_ARTIFACT", "HOTSPOT_RING",
            "HOTSPOT_TALISMAN", "JAKE_PLUSHIE", "KING_TALISMAN", "LUCKY_HOOF", "MAGNETIC_TALISMAN", "MASTER_THESIS",
            "MINIATURIZED_TUBULATOR", "NIGHT_VISION_CHARM", "ORGAN_DONOR_ARTIFACT", "ORGAN_DONOR_RING",
            "ORGAN_DONOR_TALISMAN", "PHD_GRIMOIRE", "PIGS_FOOT", "POTATO_RING", "REAPER_ORB", "RUNEBOOK", "SHENS_REGALIA",
            "STUDENT_STUDIES", "SURVIVOR_CUBE", "TARANTULA_RING", "TARANTULA_TALISMAN", "TRAPPER_CREST",
            "WEDDING_RING_0", "WEDDING_RING_1", "WEDDING_RING_2", "WEDDING_RING_3", "WEDDING_RING_4", "WEDDING_RING_5",
            "WEDDING_RING_6", "WEDDING_RING_7", "WEDDING_RING_8", "WEDDING_RING_9", "WITCH_ARTIFACT", "WITCH_RING",
            "WITCH_TALISMAN", "WOLF_PAW"
    );

    private static final Set<String> STANDALONE = Set.of(
            "BINGO_COMBAT_TALISMAN", "BOOK_OF_PROGRESSION", "COMBO_MANIA_TALISMAN", "DEVOUR_RING", "EXPERIENCE_ARTIFACT",
            "FISH_AFFINITY_TALISMAN", "GENERAL_MEDALLION", "HANDY_BLOOD_CHALICE", "HOCUS_POCUS_CIPHER", "JAKE_PLUSHIE",
            "KING_TALISMAN", "MAGNETIC_TALISMAN", "MINIATURIZED_TUBULATOR", "NIGHT_VISION_CHARM", "PIGS_FOOT",
            "REAPER_ORB", "RUNEBOOK", "SHENS_REGALIA", "SURVIVOR_CUBE", "TRAPPER_CREST", "WOLF_PAW", "ATMOSPHERIC_FILTER"
    );

    private static final Set<String> NO_RECOMBOBULATION = Set.of("BEASTSLAYERS_CODEX", "BESTIARY_TALISMAN",
            "BOOK_OF_PROGRESSION", "RUNEBOOK");

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws IOException {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
                if (!(items instanceof List<?> list)) continue;
                for (Object entry : list) ItemConfigParser.parseItem((Map<String, Object>) entry);
            }
        }
    }

    private static SkyBlockItem item(String id) {
        ItemType type = ItemType.get(id);
        assertNotNull(type, id + " is missing from ItemType; regenerate it");
        return new SkyBlockItem(type);
    }

    @Test
    void batchHasSixtyOneDistinctIds() {
        assertEquals(61, BATCH.size());
        assertEquals(61, Set.copyOf(BATCH).size());
    }

    @Test
    void everyBatchIdIsAnAccessoryWithParsedEffects() {
        List<String> failures = new ArrayList<>();
        for (String id : BATCH) {
            SkyBlockItem item = item(id);
            if (!item.hasComponent(AccessoryComponent.class)) {
                failures.add(id + ": no ACCESSORY component");
                continue;
            }
            for (AccessoryEffect effect : item.getComponent(AccessoryComponent.class).getEffects()) {
                if (effect.kind() == AccessoryEffectKind.HANDLER && !AccessoryHandlers.isKnown(effect.handler()))
                    failures.add(id + ": unknown handler " + effect.handler());
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    @Test
    void everyBatchIdResolvesToItsFamilyOrStandsAlone() {
        AccessoryFamilies families = AccessoryFamilies.get();
        List<String> failures = new ArrayList<>();
        for (String id : BATCH) {
            AccessoryFamily family = families.familyOf(id);
            if (STANDALONE.contains(id) && family != null) failures.add(id + ": expected standalone, in " + family.id());
            if (!STANDALONE.contains(id) && family == null) failures.add(id + ": not in any family");
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
        assertEquals("WEDDING_RING_0", families.familyOf("WEDDING_RING_9").id());
        assertEquals("TARANTULA_TALISMAN", families.familyOf("TARANTULA_RING").id());
        assertEquals("BEASTSLAYERS_CODEX", families.familyOf("BESTIARY_TALISMAN").id());
    }

    @Test
    void ringOfLoveOnlyCountsItsBestTier() {
        List<OwnedAccessory> active = AccessoryOwnership.dedupe(List.of(
                OwnedAccessory.of(item("WEDDING_RING_2"), true),
                OwnedAccessory.of(item("WEDDING_RING_9"), true),
                OwnedAccessory.of(item("WEDDING_RING_5"), true)));
        assertEquals(1, active.size());
        assertEquals(ItemType.WEDDING_RING_9, active.getFirst().type());
    }

    @Test
    void statAccessoriesCountOncePerLine() {
        List<OwnedAccessory> active = AccessoryOwnership.dedupe(List.of(
                OwnedAccessory.of(item("WITCH_TALISMAN"), true),
                OwnedAccessory.of(item("WITCH_ARTIFACT"), true),
                OwnedAccessory.of(item("APPLICANT_STATEMENT"), true),
                OwnedAccessory.of(item("PHD_GRIMOIRE"), true),
                OwnedAccessory.of(item("FISH_AFFINITY_TALISMAN"), true)));
        ItemStatistics stats = AccessoryStats.of(null, active, ignored -> ItemStatistics.empty());
        assertEquals(1.5, stats.getOverall(ItemStatistic.ALCHEMY_WISDOM), 0.0001);
        assertEquals(4, stats.getOverall(ItemStatistic.TROPHY_FISH_CHANCE), 0.0001);
        assertEquals(10, stats.getOverall(ItemStatistic.FISHING_SPEED), 0.0001);
    }

    @Test
    void recombobulationFlagsMatchHypixel() {
        for (String id : BATCH) {
            if (id.equals("BINGO_COMBAT_TALISMAN") || id.equals("COMBO_MANIA_TALISMAN")) continue;
            boolean allowed = Recombobulation.canRecombobulate(item(id));
            if (NO_RECOMBOBULATION.contains(id)) assertFalse(allowed, id + " can't be recombobulated");
            else assertTrue(allowed, id + " should be recombobulatable");
        }
        assertTrue(item("GENERAL_MEDALLION").getComponent(AccessoryComponent.class).isDungeon());
    }
}
