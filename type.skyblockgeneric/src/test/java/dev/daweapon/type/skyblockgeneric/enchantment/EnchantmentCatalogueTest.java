package dev.daweapon.type.skyblockgeneric.enchantment;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.CatalogueEnchantment;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.CatalogueEntry;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.EnchantmentCatalogue;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.StatEnchantment;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantmentCatalogueTest {
    private static final Set<String> ITEMS_WITHOUT_A_GROUP = Set.of("Throwing Axe", "Aspect of the Jerry", "Wand", "Shiny Rod",
            "Precursor Eye", "Vacuum", "Shears");
    private static final Set<String> NOT_IN_CATALOGUE = Set.of();

    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
    }

    private static String plain(String markup) {
        return Text.parseLenient(markup).serialize().replaceAll("<[^>]*>", "");
    }

    @Test
    void everyCatalogueEntryIsRegisteredAsAnEnchantment() {
        Set<String> registered = new HashSet<>();
        for (EnchantmentType type : EnchantmentType.values()) registered.add(type.name());
        for (String key : EnchantmentCatalogue.all().keySet())
            assertTrue(registered.contains(key), key + " is catalogued but not registered");
    }

    @Test
    void everyRegisteredEnchantmentIsCataloguedExceptTheKnownOnes() {
        for (EnchantmentType type : EnchantmentType.values()) {
            if (NOT_IN_CATALOGUE.contains(type.name())) continue;
            assertNotNull(EnchantmentCatalogue.find(type.name()), type.name() + " has no catalogue entry");
        }
    }

    @Test
    void namesAreUniqueAndEntriesComplete() {
        Set<String> names = new HashSet<>();
        for (EnchantmentType type : EnchantmentType.values()) {
            assertFalse(type.getName().isBlank(), type.name());
            assertTrue(names.add(type.getName()), "Duplicate display name " + type.getName());
        }
        for (CatalogueEntry entry : EnchantmentCatalogue.all().values()) {
            assertFalse(entry.levels().isEmpty(), entry.key());
            assertTrue(entry.maxLevel() >= 1, entry.key());
            for (int level = 1; level <= entry.maxLevel(); level++)
                assertFalse(plain(entry.description(level)).isBlank(), entry.key() + " level " + level);
        }
    }

    @Test
    void catalogueOnlyEnchantmentsExposeEveryLevelAndGroup() {
        for (EnchantmentType type : EnchantmentType.values()) {
            if (!(type.getEnch() instanceof CatalogueEnchantment catalogued)) continue;
            CatalogueEntry entry = catalogued.entry();
            assertEquals(entry.maxLevel(), catalogued.getLevelsToApply(null).maximumLevel(), type.name());
            assertEquals(entry.ultimate(), catalogued.isUltimate(), type.name());
            for (int level = 1; level <= entry.maxLevel(); level++) {
                String description = catalogued.getDescription(level);
                assertEquals(catalogued.isEffectActive(), !description.contains("(Not active yet)"), type.name());
            }
        }
    }

    @Test
    void appliedToLabelsMapToGroupsOrAreSpecialItems() {
        for (CatalogueEntry entry : EnchantmentCatalogue.all().values()) {
            for (String label : entry.appliedTo()) {
                boolean mapped = EnchantItemGroups.fromLabel(label).isPresent();
                assertTrue(mapped || ITEMS_WITHOUT_A_GROUP.contains(label), entry.key() + ": unknown item " + label);
            }
        }
    }

    @Test
    void conflictListsResolveAndAreSymmetric() {
        for (EnchantmentType type : EnchantmentType.values()) {
            CatalogueEntry entry = EnchantmentCatalogue.find(type.name());
            if (entry != null) {
                for (String name : entry.conflicts()) {
                    boolean found = Arrays.stream(EnchantmentType.values()).anyMatch(other -> other.getName().equals(name));
                    assertTrue(found, type.name() + " conflicts with unknown " + name);
                }
            }
            for (EnchantmentType other : EnchantmentConflicts.of(type))
                assertTrue(EnchantmentConflicts.of(other).contains(type), type.name() + " <-> " + other.name());
        }
    }

    @Test
    void ultimatesExcludeEachOtherAndOneForAllExcludesEverything() {
        assertTrue(EnchantmentConflicts.excludes(EnchantmentType.COMBO, EnchantmentType.SWARM));
        assertTrue(EnchantmentConflicts.excludes(EnchantmentType.ONE_FOR_ALL, EnchantmentType.SHARPNESS));
        assertFalse(EnchantmentConflicts.excludes(EnchantmentType.COMBO, EnchantmentType.SHARPNESS));
        assertTrue(EnchantmentConflicts.of(EnchantmentType.BIG_BRAIN).contains(EnchantmentType.SMALL_BRAIN));
        assertTrue(EnchantmentConflicts.of(EnchantmentType.TITAN_KILLER).contains(EnchantmentType.GIANT_KILLER));
        assertTrue(EnchantmentConflicts.of(EnchantmentType.REJUVENATE).contains(EnchantmentType.RESPITE));
    }

    @Test
    void statEnchantmentsGrantExactlyWhatTheirDescriptionSays() {
        int checked = 0;
        for (EnchantmentType type : EnchantmentType.values()) {
            if (!(type.getEnch() instanceof StatEnchantment stat)) continue;
            CatalogueEntry entry = stat.entry();
            for (int level = entry.maxLevel() == 0 ? 1 : 1; level <= entry.maxLevel(); level++) {
                String text = plain(entry.description(level));
                for (ItemStatistic statistic : stat.grants().keySet()) {
                    double value = stat.grantAt(statistic, level);
                    String number = value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
                    if (entry.levels().get(level) == null) continue;
                    assertTrue(Pattern.compile("(^|[^0-9.])" + Pattern.quote(number) + "([^0-9]|$)").matcher(text).find(),
                            type.name() + " level " + level + " grants " + number + " " + statistic + " but says: " + text);
                    checked++;
                }
            }
        }
        assertTrue(checked > 100, "only checked " + checked);
    }

    @Test
    void wiredEnchantmentsAreActiveAndTheRestSayTheyAreNot() {
        List<String> wired = List.of("POWER", "TITAN_KILLER", "TRIPLE_STRIKE", "COMBO", "SWARM", "WOODSPLITTER", "GRAVITY",
                "FEATHER_FALLING", "THORNS", "TIDAL", "INFINITE_QUIVER", "SNIPE", "PIERCING", "PUNCH", "BIG_BRAIN", "TURBO_WHEAT", "WISE", "SOUL_EATER", "ONE_FOR_ALL");
        for (String key : wired) assertTrue(EnchantmentType.valueOf(key).getEnch().isEffectActive(), key);
        for (String key : List.of("LAST_STAND", "REND", "HECATOMB", "LEGION"))
            assertFalse(EnchantmentType.valueOf(key).getEnch().isEffectActive(), key);
    }

    @Test
    void forestPledgeReachesLevelSixAndGrantsTwelveForagingFortune() {
        CatalogueEntry entry = EnchantmentCatalogue.require("FOREST_PLEDGE");
        assertEquals(6, entry.maxLevel());
        assertEquals(12, ((StatEnchantment) EnchantmentType.FOREST_PLEDGE.getEnch()).grantAt(ItemStatistic.FORAGING_FORTUNE, 6));
    }

    @Test
    void sharpnessIsNotAppliedToFishingWeapons() {
        assertFalse(EnchantmentCatalogue.require("SHARPNESS").appliedTo().contains("Fishing Weapon"));
    }
}
