package dev.daweapon.type.skyblockgeneric.chapters;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChapterRegistryTest {
    private final ChapterRegistry registry = ChapterRegistry.load(Path.of("../configuration/skyblock/chapters/moonglade_marsh.yml"));

    @Test
    void moonGladeMarshHasSevenChaptersAndSixtyNineTasks() {
        assertEquals(7, registry.chapters().size());
        assertEquals(69, registry.taskCount());
        assertEquals(List.of(3, 5, 7, 9, 12, 15, 18), registry.chapters().stream().map(chapter -> chapter.tasks().size()).toList());
    }

    @Test
    void skyBlockXpPerChapterMatchesTheWiki() {
        assertEquals(List.of(5, 10, 15, 20, 25, 30, 35), registry.chapters().stream().map(chapter -> chapter.completion().skyBlockXp()).toList());
        int total = registry.chapters().stream().mapToInt(chapter -> chapter.completion().skyBlockXp()).sum();
        assertEquals(140, total);
        assertEquals(560, SkyBlockXPSource.CHAPTERS.getMaxXP());
        assertTrue(SkyBlockXPSource.CHAPTERS.isObtainable());
        assertTrue(total * 4 <= SkyBlockXPSource.CHAPTERS.getMaxXP());
    }

    @Test
    void taskIdsAreUniqueAndEveryTaskCanBeTracked() {
        Set<String> ids = new HashSet<>();
        for (Chapter chapter : registry.chapters()) {
            for (ChapterTask task : chapter.tasks()) {
                assertTrue(ids.add(task.id()), task.id());
                assertTrue(task.trigger() != null || task.probe() != null, task.id());
                assertTrue(task.amount() > 0, task.id());
                assertFalse(task.reward().lines().isEmpty(), task.id());
                assertEquals(task.available(), task.unavailable() == null, task.id());
            }
        }
    }

    @Test
    void everyChapterKeepsAtLeastOneAvailableTask() {
        for (Chapter chapter : registry.chapters()) assertFalse(chapter.availableTasks().isEmpty(), "chapter " + chapter.number());
    }

    @Test
    void unavailableTasksAreExactlyTheUnbuiltContent() {
        Set<String> unavailable = new HashSet<>();
        registry.chapters().forEach(chapter -> chapter.tasks().stream().filter(task -> !task.available()).forEach(task -> unavailable.add(task.id())));
        assertEquals(Set.of("chillshots", "ground", "wetwing", "kiara", "tune_beacon", "flexbone", "booster",
                "starborn_temple", "drowned", "boosters_3", "upgrade_beacon", "brian", "beaconmite", "shiniest", "swamp_the_fish",
                "mob_the_fish", "coral"), unavailable);
    }

    @Test
    void completionRewardsMatchTheWiki() {
        ChapterReward one = registry.chapter(1).completion();
        assertEquals(200, one.hotf());
        assertEquals(List.of("Phanpyre", "Cod", "Lapis Zombie"), List.copyOf(one.shards().keySet()));
        assertEquals(Integer.valueOf(1), registry.chapter(2).completion().items().get(ItemType.PRIME_LUSHLILAC_BONBON));
        ChapterReward six = registry.chapter(6).completion();
        assertEquals(10_000, six.hotf());
        assertEquals(50, six.forestEssence());
        assertEquals(5_000, six.whispers());
        assertEquals(Integer.valueOf(3), six.shards().get("Drowned"));
        ChapterReward seven = registry.chapter(7).completion();
        assertEquals(20_000, seven.hotf());
        assertEquals(100, seven.forestEssence());
        assertEquals(6, seven.shards().size());
        assertNotNull(registry.chapter(7).tasks().stream().filter(task -> task.id().equals("coral")).findFirst().orElseThrow());
    }

    @Test
    void wrappingKeepsColoursAndLineLength() {
        List<String> lines = ChapterText.wrap("<c>Chillshots<7> and <c>Chillblades<7> can be found in a hidden cavern under the <b>Village<7>.");
        assertTrue(lines.size() > 1);
        lines.forEach(line -> assertTrue(ChapterText.strip(line).length() <= 36, line));
    }
}
