package net.swofty.type.skyblockgeneric.pets;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.Material;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.TextParser;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetCandyComponent;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.components.PetItemComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.KatUpgrade;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.pets.KatService;
import net.swofty.type.skyblockgeneric.user.pets.PetCandy;
import net.swofty.type.skyblockgeneric.user.pets.PetItemLegacyIds;
import net.swofty.type.skyblockgeneric.user.pets.PetItems;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PetItemsTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final Set<String> PET_ITEMS = new TreeSet<>();

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws IOException {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                Object items = YamlFileUtils.loadYaml(file.toFile()).get("items");
                if (!(items instanceof List<?> list)) continue;
                for (Object entry : list) {
                    Map<String, Object> item = (Map<String, Object>) entry;
                    ItemConfigParser.parseItem(item);
                    Object components = item.get("components");
                    if (components instanceof List<?> componentList && componentList.stream()
                            .anyMatch(c -> c instanceof Map<?, ?> m && "PET_ITEM".equals(m.get("id")))) {
                        PET_ITEMS.add(String.valueOf(item.get("id")));
                    }
                }
            }
        }
    }

    private static SkyBlockItem pet(ItemType type, Rarity rarity, int level) {
        SkyBlockItem pet = new SkyBlockItem(type);
        pet.getAttributeHandler().setRarity(rarity);
        PetLevels.setLevel(pet, level);
        return pet;
    }

    @Test
    void everyPetItemParsesAndRendersItsLore() {
        List<String> failures = new ArrayList<>();
        for (String id : PET_ITEMS) {
            SkyBlockItem item = new SkyBlockItem(ItemType.valueOf(id));
            if (!item.hasComponent(PetItemComponent.class)) {
                failures.add(id + ": PET_ITEM component failed to parse");
                continue;
            }
            PetItemComponent component = item.getComponent(PetItemComponent.class);
            if (component.getDescription().isEmpty()) failures.add(id + ": no description");
            try {
                for (String line : component.effectLines()) TextParser.parse(line);
            } catch (RuntimeException exception) {
                failures.add(id + ": " + exception.getMessage());
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
        assertTrue(PET_ITEMS.size() >= 74, "only " + PET_ITEMS.size() + " pet items");
    }

    @Test
    void heldStatItemsAddToThePetsStats() {
        SkyBlockItem lion = pet(ItemType.LION_PET, Rarity.LEGENDARY, 100);
        double defense = PetItems.statistics(lion).getOverall(ItemStatistic.DEFENSE);
        PetItems.hold(lion, ItemType.PET_ITEM_HARDENED_SCALES_UNCOMMON);
        assertEquals(defense + 25, PetItems.statistics(lion).getOverall(ItemStatistic.DEFENSE), 1e-9);

        PetItems.hold(lion, ItemType.PET_ITEM_TEXTBOOK);
        ItemStatistics base = lion.getComponent(PetComponent.class).statisticsAt(Rarity.LEGENDARY, 100);
        assertEquals(base.getOverall(ItemStatistic.STRENGTH), PetItems.statistics(lion).getOverall(ItemStatistic.STRENGTH), 1e-9);

        PetItems.hold(lion, ItemType.PET_ITEM_QUICK_CLAW);
        assertEquals(50, PetItems.statistics(lion).getOverall(ItemStatistic.MINING_FORTUNE), 1e-9);
        assertEquals(50, PetItems.statistics(lion).getOverall(ItemStatistic.MINING_SPEED), 1e-9);

        PetItems.hold(lion, ItemType.GREEN_BANDANA);
        assertEquals(base.getOverall(ItemStatistic.FARMING_FORTUNE), PetItems.statistics(lion).getOverall(ItemStatistic.FARMING_FORTUNE), 1e-9);
    }

    @Test
    void multiplierItemsScaleOnlyThePetsOwnStats() {
        SkyBlockItem lion = pet(ItemType.LION_PET, Rarity.LEGENDARY, 100);
        double strength = PetItems.statistics(lion).getOverall(ItemStatistic.STRENGTH);
        PetItems.hold(lion, ItemType.ANTIQUE_REMEDIES);
        assertEquals(strength * 1.8, PetItems.statistics(lion).getOverall(ItemStatistic.STRENGTH), 1e-9);
    }

    @Test
    void expBoostsOnlyApplyToTheirSkill() {
        SkyBlockItem lion = pet(ItemType.LION_PET, Rarity.LEGENDARY, 1);
        PetItems.hold(lion, ItemType.PET_ITEM_MINING_SKILL_BOOST_COMMON);
        assertEquals(1.2, PetItems.expMultiplier(lion, SkillCategories.MINING), 1e-9);
        assertEquals(1, PetItems.expMultiplier(lion, SkillCategories.COMBAT), 1e-9);

        PetItems.hold(lion, ItemType.PET_ITEM_ALL_SKILLS_BOOST_COMMON);
        assertEquals(1.1, PetItems.expMultiplier(lion, SkillCategories.FISHING), 1e-9);

        PetItems.hold(lion, ItemType.PET_ITEM_EXP_SHARE);
        assertEquals(0.15, PetItems.expShareRate(lion), 1e-9);
    }

    @Test
    void tierBoostRaisesRarityWhileHeldAndKatRefusesIt() {
        SkyBlockItem lion = pet(ItemType.LION_PET, Rarity.EPIC, 50);
        PetItemComponent tierBoost = new SkyBlockItem(ItemType.PET_ITEM_TIER_BOOST).getComponent(PetItemComponent.class);
        assertNull(PetItems.problem(lion, ItemType.PET_ITEM_TIER_BOOST, tierBoost, null));

        PetItems.hold(lion, ItemType.PET_ITEM_TIER_BOOST);
        assertEquals(Rarity.LEGENDARY, lion.getAttributeHandler().getRarity());
        assertTrue(PetItems.isTierBoosted(lion));
        assertNull(KatService.bill(lion));

        PetItems.hold(lion, ItemType.PET_ITEM_BIG_TEETH_COMMON);
        assertEquals(Rarity.EPIC, lion.getAttributeHandler().getRarity());

        SkyBlockItem legendary = pet(ItemType.LION_PET, Rarity.LEGENDARY, 1);
        assertNotNull(PetItems.problem(legendary, ItemType.PET_ITEM_TIER_BOOST, tierBoost, null));
        SkyBlockItem griffin = pet(ItemType.GRIFFIN_PET, Rarity.COMMON, 1);
        assertNotNull(PetItems.problem(griffin, ItemType.PET_ITEM_TIER_BOOST, tierBoost, null));
    }

    @Test
    void restrictedItemsOnlyFitTheirPet() {
        PetItemComponent needle = new SkyBlockItem(ItemType.GRANDMAS_KNITTING_NEEDLE).getComponent(PetItemComponent.class);
        assertTrue(needle.appliesTo(ItemType.GRANDMA_WOLF_PET));
        assertFalse(needle.appliesTo(ItemType.LION_PET));
    }

    @Test
    void swapFeeFollowsTheHeldItemsRarity() {
        assertEquals(25_000, PetItems.swapFee(Rarity.COMMON));
        assertEquals(500_000, PetItems.swapFee(Rarity.LEGENDARY));
        assertEquals(2_500_000, PetItems.swapFee(Rarity.DIVINE));
    }

    @Test
    void legacyPetItemIdsMigrateToHypixelIds() {
        for (Map.Entry<String, String> renamed : PetItemLegacyIds.RENAMED.entrySet()) {
            assertNull(ItemType.get(renamed.getKey()), renamed.getKey());
            assertNotNull(ItemType.get(PetItemLegacyIds.migrate(renamed.getKey())), renamed.getValue());
        }
        SkyBlockItem lion = pet(ItemType.LION_PET, Rarity.LEGENDARY, 1);
        lion.getAttribute("pet_held_item").setValue("IRON_CLAWS");
        assertEquals(ItemType.PET_ITEM_IRON_CLAWS_COMMON, lion.getAttributeHandler().getPetHeldItem());
    }

    @Test
    void katChargesTheUpgradeItems() {
        SkyBlockItem bee = pet(ItemType.BEE_PET, Rarity.COMMON, 1);
        KatService.Bill bill = KatService.bill(bee);
        assertNotNull(bill);
        assertEquals(Rarity.UNCOMMON, bill.target());
        assertEquals(0, bill.coins());
        assertEquals(List.of(new KatUpgrade.ItemCost(ItemType.SMALL_HONEY_DIPPER, 1)), bill.items());

        bee.getAttributeHandler().setRarity(Rarity.LEGENDARY);
        PetLevels.setLevel(bee, 100);
        bill = KatService.bill(bee);
        assertEquals(Rarity.MYTHIC, bill.target());
        assertEquals(7_000_000, bill.coins());
        assertEquals(List.of(new KatUpgrade.ItemCost(ItemType.ROYAL_JELLY, 1)), bill.items());

        assertMythicItem(ItemType.ELEPHANT_PET, ItemType.TRUNK_POLISH);
        assertMythicItem(ItemType.BLACK_CAT_PET, ItemType.BLACK_WOOLEN_YARN);
        assertMythicItem(ItemType.BABY_YETI_PET, ItemType.POCKET_SIZED_IGLOO);
    }

    private static void assertMythicItem(ItemType petType, ItemType item) {
        SkyBlockItem pet = pet(petType, Rarity.LEGENDARY, 1);
        KatService.Bill bill = KatService.bill(pet);
        assertNotNull(bill, petType.name());
        assertEquals(Rarity.MYTHIC, bill.target());
        assertEquals(List.of(new KatUpgrade.ItemCost(item, 1)), bill.items());
    }

    @Test
    void carrotCandyGrantsExpUpToItsLifetimeCap() {
        PetCandyComponent ultimate = new SkyBlockItem(ItemType.ULTIMATE_CARROT_CANDY).getComponent(PetCandyComponent.class);
        PetCandyComponent cake = new SkyBlockItem(ItemType.PET_CAKE).getComponent(PetCandyComponent.class);
        SkyBlockItem lion = pet(ItemType.LION_PET, Rarity.COMMON, 1);
        for (int i = 0; i < PetCandyComponent.LIFETIME_CAP; i++) {
            if (PetLevels.isMaxLevel(lion)) break;
            assertTrue(PetCandy.apply(lion, ultimate));
        }
        assertTrue(PetLevels.isMaxLevel(lion));
        assertFalse(PetCandy.apply(lion, cake));

        SkyBlockItem tiger = pet(ItemType.TIGER_PET, Rarity.LEGENDARY, 1);
        PetCandyComponent simple = new SkyBlockItem(ItemType.SIMPLE_CARROT_CANDY).getComponent(PetCandyComponent.class);
        for (int i = 0; i < PetCandyComponent.LIFETIME_CAP; i++) assertTrue(PetCandy.apply(tiger, simple));
        assertEquals(PetCandyComponent.LIFETIME_CAP, tiger.getAttributeHandler().getPetCandyUsed());
        assertFalse(PetCandy.apply(tiger, simple));
        assertTrue(PetCandy.apply(tiger, cake));
        assertEquals(PetCandyComponent.LIFETIME_CAP, tiger.getAttributeHandler().getPetCandyUsed());
    }

    @Test
    void generatedItemTypesKeepTheirMaterialAndRarity() {
        assertEquals(Material.DIAMOND, ItemType.PET_ITEM_ALL_SKILLS_BOOST_COMMON.material);
        assertEquals(Rarity.COMMON, ItemType.PET_ITEM_ALL_SKILLS_BOOST_COMMON.rarity);
        assertEquals(Material.PLAYER_HEAD, ItemType.ROYAL_JELLY.material);
        assertEquals(Rarity.MYTHIC, ItemType.ROYAL_JELLY.rarity);
        assertEquals(Rarity.LEGENDARY, ItemType.PET_ITEM_TIER_BOOST.rarity);
    }
}
