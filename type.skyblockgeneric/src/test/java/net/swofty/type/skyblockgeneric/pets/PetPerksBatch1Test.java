package net.swofty.type.skyblockgeneric.pets;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.type.skyblockgeneric.gifts.ReindeerPet;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetPerkMath;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.ConfiguredPetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetAbilityKind;
import net.swofty.type.skyblockgeneric.mythological.GriffinPet;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PetPerksBatch1Test {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void init() throws IOException {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
        try (Stream<Path> files = Files.walk(ITEMS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                if (YamlFileUtils.loadYaml(file.toFile()).get("items") instanceof List<?> list) {
                    for (Object entry : list) ItemConfigParser.parseItem((Map<String, Object>) entry);
                }
            }
        }
    }

    private static PetComponent component(ItemType type) {
        return new SkyBlockItem(type).getComponent(PetComponent.class);
    }

    private static ConfiguredPetAbility ability(ItemType pet, String name, Rarity rarity) {
        return component(pet).getAbilities().stream()
                .filter(a -> a.getName().equals(name) && a.appliesAt(rarity))
                .reduce((first, second) -> second).orElseThrow();
    }

    private static SkyBlockItem pet(ItemType type, Rarity rarity, int level) {
        SkyBlockItem pet = new SkyBlockItem(type);
        pet.getAttributeHandler().setRarity(rarity);
        PetLevels.setLevel(pet, level);
        return pet;
    }

    @Test
    void enderDragonBuffsTheAspectOfTheDragonsAtEveryLevel() {
        for (Rarity rarity : List.of(Rarity.EPIC, Rarity.LEGENDARY)) {
            ConfiguredPetAbility perk = ability(ItemType.ENDER_DRAGON_PET, "One with the Dragons", rarity);
            assertEquals(PetAbilityKind.HANDLER, perk.getKind());
            assertTrue(perk.isActive());
            assertEquals(0.5, perk.valuesAt(rarity, 1)[0], 1e-9);
            assertEquals(25, perk.valuesAt(rarity, 50)[0], 1e-9);
            assertEquals(50, perk.valuesAt(rarity, 100)[0], 1e-9);
            assertEquals(30, perk.valuesAt(rarity, 100)[1], 1e-9);
        }
    }

    @Test
    void magmaCubeHotEmberIsLegendaryOnlyAndScalesWithLevel() {
        ConfiguredPetAbility hotEmber = ability(ItemType.MAGMA_CUBE_PET, "Hot Ember", Rarity.LEGENDARY);
        assertTrue(hotEmber.isActive());
        assertFalse(hotEmber.appliesAt(Rarity.EPIC));
        assertEquals(0.5, hotEmber.valuesAt(Rarity.LEGENDARY, 1)[0], 1e-9);
        assertEquals(25, hotEmber.valuesAt(Rarity.LEGENDARY, 50)[0], 1e-9);
        assertEquals(50, hotEmber.valuesAt(Rarity.LEGENDARY, 100)[0], 1e-9);
    }

    @Test
    void saltBladeAppearsAtRareAndScalesByRarity() {
        ConfiguredPetAbility saltBlade = ability(ItemType.MAGMA_CUBE_PET, "Salt Blade", Rarity.RARE);
        assertTrue(saltBlade.isActive());
        assertFalse(saltBlade.appliesAt(Rarity.UNCOMMON));
        assertEquals(0.2, saltBlade.valuesAt(Rarity.RARE, 1)[0], 1e-9);
        assertEquals(20, saltBlade.valuesAt(Rarity.RARE, 100)[0], 1e-9);
        assertEquals(12.5, saltBlade.valuesAt(Rarity.EPIC, 50)[0], 1e-9);
        assertEquals(25, saltBlade.valuesAt(Rarity.LEGENDARY, 100)[0], 1e-9);
    }

    @Test
    void houndFinderRaisesArmorDropChance() {
        ConfiguredPetAbility finder = ability(ItemType.HOUND_PET, "Finder", Rarity.EPIC);
        assertTrue(finder.isActive());
        for (Rarity rarity : List.of(Rarity.EPIC, Rarity.LEGENDARY)) {
            assertEquals(0.3, finder.valuesAt(rarity, 1)[0], 1e-9);
            assertEquals(15, finder.valuesAt(rarity, 50)[0], 1e-9);
            assertEquals(30, finder.valuesAt(rarity, 100)[0], 1e-9);
        }
        assertEquals(13, PetPerkMath.boostChance(10, finder.valuesAt(Rarity.LEGENDARY, 100)[0]), 1e-9);
        assertEquals(10.03, PetPerkMath.boostChance(10, finder.valuesAt(Rarity.EPIC, 1)[0]), 1e-9);
        assertEquals(10, PetPerkMath.boostChance(10, 0), 1e-9);
        assertEquals(7, CombatPetHooks.armorDropChance(null, 7), 1e-9);
    }

    @Test
    void blackCatLootingIsMythicAndScalesCollectionDrops() {
        ConfiguredPetAbility looting = ability(ItemType.BLACK_CAT_PET, "Looting", Rarity.MYTHIC);
        assertTrue(looting.isActive());
        assertFalse(looting.appliesAt(Rarity.LEGENDARY));
        assertEquals(0.15, looting.valuesAt(Rarity.MYTHIC, 1)[0], 1e-9);
        assertEquals(7.5, looting.valuesAt(Rarity.MYTHIC, 50)[0], 1e-9);
        assertEquals(15, looting.valuesAt(Rarity.MYTHIC, 100)[0], 1e-9);

        Random random = new Random(7);
        int trials = 20_000;
        long total = 0;
        for (int i = 0; i < trials; i++) {
            int amount = PetPerkMath.scaledAmount(10, 15, random);
            assertTrue(amount == 11 || amount == 12, "amount " + amount);
            total += amount;
        }
        assertEquals(11.5, total / (double) trials, 0.03);
        assertEquals(10, PetPerkMath.scaledAmount(10, 0, random));
        assertEquals(20, PetPerkMath.scaledAmount(10, 100, random));
        assertEquals(0, PetPerkMath.scaledAmount(0, 100, random));
        assertEquals(3, CombatPetHooks.collectionDropAmount(null, ItemType.ROTTEN_FLESH, 3));
    }

    @Test
    void jerryCandyPerkOnlyAppliesToMythicJerry() {
        List<ConfiguredPetAbility> jerryPerks = component(ItemType.JERRY_PET).getAbilities().stream()
                .filter(a -> a.getName().equals("Jerry")).toList();
        ConfiguredPetAbility candy = jerryPerks.getLast();
        assertTrue(candy.isActive());
        assertTrue(candy.appliesAt(Rarity.MYTHIC));
        assertFalse(candy.appliesAt(Rarity.LEGENDARY));
        assertFalse(CombatPetHooks.findsJerryCandy(null, 100));
    }

    @Test
    void kuudraCrimsonAddsEssenceByRarityAndLevel() {
        ConfiguredPetAbility crimson = ability(ItemType.KUUDRA_PET, "Crimson", Rarity.RARE);
        assertTrue(crimson.isActive());
        assertEquals(0.15, crimson.valuesAt(Rarity.RARE, 1)[0], 1e-9);
        assertEquals(15, crimson.valuesAt(Rarity.RARE, 100)[0], 1e-9);
        assertEquals(10, crimson.valuesAt(Rarity.EPIC, 50)[0], 1e-9);
        assertEquals(20, crimson.valuesAt(Rarity.LEGENDARY, 100)[0], 1e-9);

        Random random = new Random(11);
        long total = 0;
        for (int i = 0; i < 20_000; i++) total += PetPerkMath.scaledAmount(1000L, 15.05, random);
        assertEquals(1150.5, total / 20_000D, 1);
        assertEquals(500, CombatPetHooks.crimsonEssence(null, 500));
    }

    @Test
    void riftFerretOrbsAreFunIsAFlatTenPercent() {
        ConfiguredPetAbility orbs = ability(ItemType.RIFT_FERRET_PET, "Orbs are Fun", Rarity.EPIC);
        assertTrue(orbs.isActive());
        for (Rarity rarity : List.of(Rarity.EPIC, Rarity.LEGENDARY)) {
            for (int level : new int[]{1, 50, 100}) assertEquals(10, orbs.valuesAt(rarity, level)[0], 1e-9);
        }
        assertEquals(1, CombatPetHooks.experienceOrbMultiplier(null), 1e-9);
    }

    @Test
    void tigerApexPredatorScalesAtLegendary() {
        ConfiguredPetAbility apex = ability(ItemType.TIGER_PET, "Apex Predator", Rarity.LEGENDARY);
        assertTrue(apex.isActive());
        assertFalse(apex.appliesAt(Rarity.EPIC));
        assertEquals(1, apex.valuesAt(Rarity.LEGENDARY, 1)[0], 1e-9);
        assertEquals(50, apex.valuesAt(Rarity.LEGENDARY, 50)[0], 1e-9);
        assertEquals(100, apex.valuesAt(Rarity.LEGENDARY, 100)[0], 1e-9);
        assertEquals(15, PetPerkMath.APEX_PREDATOR_RADIUS, 1e-9);
    }

    @Test
    void witherBloodScalesFromEpic() {
        ConfiguredPetAbility witherBlood = ability(ItemType.WITHER_SKELETON_PET, "Wither Blood", Rarity.EPIC);
        assertTrue(witherBlood.isActive());
        assertFalse(witherBlood.appliesAt(Rarity.RARE));
        for (Rarity rarity : List.of(Rarity.EPIC, Rarity.LEGENDARY)) {
            assertEquals(1, witherBlood.valuesAt(rarity, 1)[0], 1e-9);
            assertEquals(50, witherBlood.valuesAt(rarity, 50)[0], 1e-9);
            assertEquals(100, witherBlood.valuesAt(rarity, 100)[0], 1e-9);
        }
    }

    @Test
    void batWingsOfSteelScalesAtLegendary() {
        ConfiguredPetAbility wings = ability(ItemType.BAT_PET, "Wings of Steel", Rarity.LEGENDARY);
        assertTrue(wings.isActive());
        assertFalse(wings.appliesAt(Rarity.EPIC));
        assertEquals(0.5, wings.valuesAt(Rarity.LEGENDARY, 1)[0], 1e-9);
        assertEquals(25, wings.valuesAt(Rarity.LEGENDARY, 50)[0], 1e-9);
        assertEquals(50, wings.valuesAt(Rarity.LEGENDARY, 100)[0], 1e-9);
        assertEquals(50, wings.valuesAt(Rarity.MYTHIC, 100)[0], 1e-9);
    }

    @Test
    void megalodonBloodScentGrowsWithMissingHealth() {
        ConfiguredPetAbility scent = ability(ItemType.MEGALODON_PET, "Blood Scent", Rarity.EPIC);
        assertTrue(scent.isActive());
        for (Rarity rarity : List.of(Rarity.EPIC, Rarity.LEGENDARY)) {
            double atOne = scent.valuesAt(rarity, 1)[0];
            double atHundred = scent.valuesAt(rarity, 100)[0];
            assertEquals(51, atOne, 1e-9);
            assertEquals(150, atHundred, 1e-9);
            assertEquals(0, PetPerkMath.missingHealthPercent(atHundred, 1000, 1000), 1e-9);
            assertEquals(75, PetPerkMath.missingHealthPercent(atHundred, 500, 1000), 1e-9);
            assertEquals(atHundred, PetPerkMath.missingHealthPercent(atHundred, 0, 1000), 1e-9);
            assertEquals(25.5, PetPerkMath.missingHealthPercent(atOne, 500, 1000), 1e-9);
        }
        assertEquals(0, PetPerkMath.missingHealthPercent(100, 5, 0), 1e-9);
        assertEquals(100, PetPerkMath.missingHealthPercent(100, -50, 100), 1e-9);
    }

    @Test
    void giraffeLongNeckOnlyAddsDamageBeyondThreeBlocks() {
        ConfiguredPetAbility longNeck = ability(ItemType.GIRAFFE_PET, "Long Neck", Rarity.LEGENDARY);
        assertTrue(longNeck.isActive());
        assertFalse(longNeck.appliesAt(Rarity.EPIC));
        assertEquals(50.5, longNeck.valuesAt(Rarity.LEGENDARY, 1)[0], 1e-9);
        assertEquals(75, longNeck.valuesAt(Rarity.LEGENDARY, 50)[0], 1e-9);
        assertEquals(100, longNeck.valuesAt(Rarity.LEGENDARY, 100)[0], 1e-9);
        assertEquals(100, PetPerkMath.longNeckDamage(100, 3.5), 1e-9);
        assertEquals(0, PetPerkMath.longNeckDamage(100, 3), 1e-9);
        assertEquals(0, PetPerkMath.longNeckDamage(100, 1), 1e-9);
        assertEquals(0, CombatPetHooks.flatMeleeDamage(null, null), 1e-9);
    }

    @Test
    void reindeerIcyWindAndWinterSprintReadTheYaml() {
        ConfiguredPetAbility icyWind = ability(ItemType.REINDEER_PET, "Icy Wind", Rarity.LEGENDARY);
        assertTrue(icyWind.isActive());
        assertEquals(0.2, icyWind.valuesAt(Rarity.LEGENDARY, 1)[0], 1e-9);
        assertEquals(10, icyWind.valuesAt(Rarity.LEGENDARY, 50)[0], 1e-9);
        assertEquals(20, icyWind.valuesAt(Rarity.LEGENDARY, 100)[0], 1e-9);
        for (int level : new int[]{1, 50, 100}) {
            assertEquals(2, ReindeerPet.winterSprintMultiplier(pet(ItemType.REINDEER_PET, Rarity.LEGENDARY, level)), 1e-9);
        }
        assertEquals(1, ReindeerPet.winterSprintMultiplier(pet(ItemType.WOLF_PET, Rarity.LEGENDARY, 100)), 1e-9);
    }

    @Test
    void griffinAncientEarthIsMythicOnlyAndOnePerBurrow() {
        ConfiguredPetAbility ancientEarth = ability(ItemType.GRIFFIN_PET, "Ancient Earth", Rarity.MYTHIC);
        assertTrue(ancientEarth.isActive());
        assertFalse(ancientEarth.appliesAt(Rarity.LEGENDARY));
        for (int level : new int[]{1, 50, 100}) assertEquals(1, ancientEarth.valuesAt(Rarity.MYTHIC, level)[0], 1e-9);
        assertEquals(0, GriffinPet.ancientEarthTracking(null), 1e-9);
    }

    @Test
    void crowInsightfulGrowsWithLevel() {
        ConfiguredPetAbility insightful = ability(ItemType.CROW_PET, "Insightful", Rarity.LEGENDARY);
        assertTrue(insightful.isActive());
        assertEquals(3.12, insightful.valuesAt(Rarity.LEGENDARY, 1)[0], 1e-9);
        assertEquals(9, insightful.valuesAt(Rarity.LEGENDARY, 50)[0], 1e-9);
        assertEquals(15, insightful.valuesAt(Rarity.LEGENDARY, 100)[0], 1e-9);
        assertFalse(CombatPetHooks.savesMana(null));
    }

    @Test
    void endermiteMoreStonksScalesByRarity() {
        ConfiguredPetAbility stonks = ability(ItemType.ENDERMITE_PET, "More Stonks", Rarity.COMMON);
        assertTrue(stonks.isActive());
        assertEquals(0.5, stonks.valuesAt(Rarity.COMMON, 1)[0], 1e-9);
        assertEquals(40, stonks.valuesAt(Rarity.UNCOMMON, 50)[0], 1e-9);
        assertEquals(100, stonks.valuesAt(Rarity.EPIC, 100)[0], 1e-9);
    }
}
