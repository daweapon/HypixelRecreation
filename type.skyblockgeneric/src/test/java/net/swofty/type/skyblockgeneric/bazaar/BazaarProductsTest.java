package net.swofty.type.skyblockgeneric.bazaar;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.gui.inventories.bazaar.BazaarSetLayout;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BazaarProductsTest {
    private static final Set<String> NOT_IN_THIS_SERVER = Set.of(
            "ENCHANTMENT_CURSE_OF_VANISHING_0", "ENCHANTMENT_WITHER_HUNTER_0",
            "ENCHANTMENT_FROST_WALKER_1", "ENCHANTMENT_FROST_WALKER_2",
            "ENCHANTMENT_SUNDER_1", "ENCHANTMENT_SUNDER_2", "ENCHANTMENT_SUNDER_3",
            "ENCHANTMENT_SUNDER_4", "ENCHANTMENT_SUNDER_5", "ENCHANTMENT_SUNDER_6");

    private static List<String> liveIds() throws IOException {
        try (InputStream stream = BazaarProductsTest.class.getResourceAsStream("/bazaar/live_enchantment_ids.txt")) {
            return Arrays.stream(new String(stream.readAllBytes(), StandardCharsets.UTF_8).split("\\R")).filter(line -> !line.isBlank()).toList();
        }
    }

    @Test
    void everyLiveIdMapsToACatalogueEnchantmentAndLevelExceptTheRemovedOnes() throws IOException {
        List<String> ids = liveIds();
        assertEquals(777, ids.size());
        Set<String> unmapped = new HashSet<>();
        for (String id : ids) {
            EnchantmentProduct product = BazaarProducts.parseEnchantment(id);
            if (product == null) {
                unmapped.add(id);
                continue;
            }
            assertEquals(id, product.id());
            assertNotNull(EnchantmentType.valueOf(product.enchantment().key()), id);
        }
        assertEquals(NOT_IN_THIS_SERVER, unmapped);
    }

    @Test
    void soldProductsAreLiveIdsWithOrdersAndResolveById() throws IOException {
        Set<String> live = new HashSet<>(liveIds());
        assertEquals(456, BazaarProducts.enchantments().size());
        for (EnchantmentProduct product : BazaarProducts.enchantments()) {
            assertTrue(live.contains(product.id()), product.id());
            assertSame(product, BazaarProducts.byId(product.id()));
        }
    }

    @Test
    void productNamesUseRomanNumerals() {
        assertEquals("Sharpness VI", BazaarProducts.enchantment("ENCHANTMENT_SHARPNESS_6").name());
        assertEquals("Drain IV", BazaarProducts.enchantment("ENCHANTMENT_SYPHON_4").name());
        assertEquals("Fatal Tempo V", BazaarProducts.enchantment("ENCHANTMENT_ULTIMATE_FATAL_TEMPO_5").name());
    }

    @Test
    void groupCountsAndLabelsMatchTheVideo() {
        List<BazaarProductGroup> normal = BazaarProducts.enchantmentGroups(false);
        List<BazaarProductGroup> ultimate = BazaarProducts.enchantmentGroups(true);
        assertEquals(113, normal.size());
        assertEquals(26, ultimate.size());
        assertEquals(113 + 26, normal.size() + ultimate.size());
        List<String> normalLabels = normal.stream().map(BazaarProductGroup::label).toList();
        for (String label : List.of("Bug Blender 1-5", "Dedication 1-4", "Drain 4-5", "Cultivating 1",
                "Bane of Arthropods 6", "Caster 6", "Cleave 6", "Woodsplitter 6")) {
            assertTrue(normalLabels.contains(label), label);
        }
        assertEquals("Woodsplitter 6", normalLabels.getLast());
        List<String> ultimateLabels = ultimate.stream().map(BazaarProductGroup::label).toList();
        assertTrue(ultimateLabels.containsAll(List.of("Bank 1-5", "Fatal Tempo 1-5", "Flowstate 1-3", "Wisdom 1-5")));
        assertTrue(ultimate.stream().allMatch(group -> group.products().stream().allMatch(product -> ((EnchantmentProduct) product).ultimate())));
        assertTrue(normal.stream().noneMatch(group -> ((EnchantmentProduct) group.first()).ultimate()));
    }

    @Test
    void everyProductBelongsToOneGroupAndLevelsAreContiguous() {
        int products = 0;
        for (boolean ultimate : new boolean[]{false, true}) {
            for (BazaarProductGroup group : BazaarProducts.enchantmentGroups(ultimate)) {
                int level = group.firstLevel();
                for (BazaarProduct product : group.products()) {
                    assertEquals(level++, ((EnchantmentProduct) product).level());
                    assertSame(group, BazaarProducts.groupOf((EnchantmentProduct) product));
                    products++;
                }
                assertEquals(group.lastLevel(), level - 1);
            }
        }
        assertEquals(BazaarProducts.enchantments().size(), products);
    }

    @Test
    void ultimateTilesSitWhereTheVideoShowedThem() {
        List<BazaarProductGroup> ultimate = BazaarProducts.enchantmentGroups(true);
        int[] slots = new BazaarSetLayout(ultimate.size()).slots(0);
        Map<String, Integer> expected = Map.ofEntries(
                Map.entry("Bank", 10), Map.entry("Chimera", 12), Map.entry("Combo", 13), Map.entry("Crop Fever", 14),
                Map.entry("Fatal Tempo", 16), Map.entry("Flowstate", 21), Map.entry("Inferno", 23), Map.entry("Legion", 25),
                Map.entry("No Pain No Gain", 29), Map.entry("Rend", 32), Map.entry("Swarm", 37), Map.entry("Wisdom", 41));
        for (int i = 0; i < ultimate.size(); i++) {
            Integer slot = expected.get(ultimate.get(i).name());
            if (slot != null) {
                assertEquals(slot, slots[i], ultimate.get(i).name());
            }
        }
    }

    @Test
    void enchantmentMenusPageLikeTheVideo() {
        BazaarSetLayout normal = new BazaarSetLayout(113);
        assertEquals(5, normal.pageCount());
        assertEquals(1, normal.itemsOnPage(4));
        assertEquals(10, normal.slots(4)[0]);
        BazaarSetLayout ultimate = new BazaarSetLayout(26);
        assertEquals(1, ultimate.pageCount());
        assertEquals(26, ultimate.slots(0).length);
    }

    @Test
    void essencesAreTheElevenOfTheVideoInOrder() {
        List<String> ids = BazaarProducts.essences().stream().map(EssenceProduct::id).toList();
        assertEquals(List.of("ESSENCE_WITHER", "ESSENCE_SPIDER", "ESSENCE_UNDEAD", "ESSENCE_DRAGON", "ESSENCE_GOLD",
                "ESSENCE_DIAMOND", "ESSENCE_ICE", "ESSENCE_CRIMSON", "ESSENCE_FOREST", "ESSENCE_FOSSIL", "ESSENCE_SAFARI"), ids);
        assertEquals("Wither Essence", BazaarProducts.byId("ESSENCE_WITHER").name());
        assertEquals(EssenceType.SAFARI, ((EssenceProduct) BazaarProducts.byId("ESSENCE_SAFARI")).essence());
        assertTrue(BazaarProducts.essences().stream().noneMatch(product -> product.essence() == EssenceType.SUN_GECKO));
    }

    @Test
    void essenceConversionMathsClampToTheBalanceAndTheCap() {
        assertEquals(0, EssenceProduct.heldAmount(0));
        assertEquals(12_345, EssenceProduct.heldAmount(12_345));
        assertEquals(Integer.MAX_VALUE, EssenceProduct.heldAmount(3_000_000_000L));
        assertEquals((int) EssenceType.CAP, EssenceProduct.roomFor(0));
        assertEquals(1_999_999_000, EssenceProduct.roomFor(1_000));
        assertEquals(0, EssenceProduct.roomFor(EssenceType.CAP));
        assertEquals(0, EssenceProduct.roomFor(EssenceType.CAP + 5));
    }

    @Test
    void idsResolveToItemsEssencesAndEnchantmentsOnly() {
        assertSame(BazaarProducts.of(ItemType.BOOSTER_COOKIE), BazaarProducts.byId("BOOSTER_COOKIE"));
        assertEquals(ItemType.BOOSTER_COOKIE, BazaarProducts.byId("BOOSTER_COOKIE").itemType());
        assertNull(BazaarProducts.byId("NOT_A_PRODUCT"));
        assertNull(BazaarProducts.byId("ESSENCE_SUN_GECKO"));
        assertNull(BazaarProducts.byId("ENCHANTMENT_SHARPNESS_1"));
        assertNull(BazaarProducts.byId(null));
    }

    @Test
    void oddityTilesHoldTheEnchantmentAndEssenceProducts() {
        BazaarTile enchantments = BazaarCategories.ODDITIES.tiles().stream().filter(tile -> tile.name().equals("Enchantments")).findFirst().orElseThrow();
        assertTrue(enchantments.nested());
        assertEquals(List.of("Normal Enchantments", "Ultimate Enchantments"), enchantments.sets().stream().map(BazaarItemSet::name).toList());
        assertEquals(113, enchantments.sets().get(0).productCount());
        assertEquals(26, enchantments.sets().get(1).productCount());
        assertEquals("gui_bazaar.tile.normal_enchantments", enchantments.sets().get(0).descriptionKey);
        assertEquals("gui_bazaar.tile.ultimate_enchantments", enchantments.sets().get(1).descriptionKey);

        BazaarTile essence = BazaarCategories.ODDITIES.tiles().stream().filter(tile -> tile.name().equals("Essence")).findFirst().orElseThrow();
        assertEquals(11, essence.productCount());
        assertEquals(BazaarProducts.essences(), essence.first().products());

        BazaarProduct wither = BazaarProducts.byId("ESSENCE_WITHER");
        assertSame(essence.first(), BazaarCategories.getFromProduct(wither).getValue());
        BazaarProduct drain = BazaarProducts.byId("ENCHANTMENT_SYPHON_4");
        assertSame(BazaarCategories.ODDITIES, BazaarCategories.getFromProduct(drain).getKey());
        assertEquals("Normal Enchantments", BazaarCategories.getFromProduct(drain).getValue().name());
        assertNull(BazaarCategories.getFromItem(ItemType.ENCHANTED_BOOK));
    }
}
