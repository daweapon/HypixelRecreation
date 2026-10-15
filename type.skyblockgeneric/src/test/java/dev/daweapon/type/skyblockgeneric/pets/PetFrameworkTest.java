package dev.daweapon.type.skyblockgeneric.pets;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributePetData;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.commons.text.TextParser;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointPetData;
import dev.daweapon.type.skyblockgeneric.item.ItemConfigParser;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition.ConfiguredPetAbility;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition.PetAbilityKind;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition.PetComponentParser;
import dev.daweapon.type.skyblockgeneric.staff.MaxPets;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;
import dev.daweapon.type.skyblockgeneric.user.pets.PetScore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PetFrameworkTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final Set<String> YAML_PETS = new TreeSet<>();

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
                            .anyMatch(c -> c instanceof Map<?, ?> m && "PET".equals(m.get("id")))) {
                        YAML_PETS.add(String.valueOf(item.get("id")));
                    }
                }
            }
        }
    }

    @Test
    void everyPetRendersAtEveryRarityAndLevel() {
        List<String> failures = new ArrayList<>();
        for (String id : YAML_PETS) {
            SkyBlockItem pet = new SkyBlockItem(ItemType.valueOf(id));
            if (!pet.hasComponent(PetComponent.class)) {
                failures.add(id + ": PET component failed to parse");
                continue;
            }
            PetComponent component = pet.getComponent(PetComponent.class);
            if (component.getSkullTexture().isBlank()) failures.add(id + ": no skull texture");
            for (Rarity rarity : component.getRarities()) {
                pet.getAttributeHandler().setRarity(rarity);
                for (int level : new int[]{1, 100, component.getMaxLevel()}) {
                    PetLevels.setLevel(pet, Math.min(level, component.getMaxLevel()));
                    try {
                        for (String line : component.getAbsoluteLore(null, pet)) TextParser.parse(line);
                        TextParser.parse(pet.getDisplayName());
                    } catch (RuntimeException exception) {
                        failures.add(id + " " + rarity + " lvl " + level + ": " + exception.getMessage());
                    }
                }
            }
            for (ConfiguredPetAbility ability : component.getAbilities()) {
                if (ability.getKind() == PetAbilityKind.HANDLER && ability.getHandler() == null) {
                    failures.add(id + ": handler '" + ability.getHandlerId() + "' is not registered");
                }
                if (!component.getRarities().contains(ability.getMinRarity())
                        && component.getRarities().stream().noneMatch(ability::appliesAt)) {
                    failures.add(id + ": ability " + ability.getName() + " never unlocks");
                }
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
        assertFalse(YAML_PETS.isEmpty());
    }

    @Test
    void xpTableMatchesHypixel() {
        assertEquals(5_624_785, ItemAttributePetData.totalExperienceForLevel(100, ItemAttributePetData.rarityOffset(Rarity.COMMON)));
        assertEquals(8_644_220, ItemAttributePetData.totalExperienceForLevel(100, ItemAttributePetData.rarityOffset(Rarity.UNCOMMON)));
        assertEquals(12_626_665, ItemAttributePetData.totalExperienceForLevel(100, ItemAttributePetData.rarityOffset(Rarity.RARE)));
        assertEquals(18_608_500, ItemAttributePetData.totalExperienceForLevel(100, ItemAttributePetData.rarityOffset(Rarity.EPIC)));
        assertEquals(25_353_230, ItemAttributePetData.totalExperienceForLevel(100, ItemAttributePetData.rarityOffset(Rarity.LEGENDARY)));
        assertEquals(25_353_230, ItemAttributePetData.totalExperienceForLevel(100, ItemAttributePetData.rarityOffset(Rarity.MYTHIC)));
        assertEquals(214_023_230, ItemAttributePetData.totalExperienceForLevel(200, ItemAttributePetData.rarityOffset(Rarity.LEGENDARY)));
        assertEquals(660, ItemAttributePetData.totalExperienceForLevel(2, ItemAttributePetData.rarityOffset(Rarity.LEGENDARY)));
        assertEquals(1_390, ItemAttributePetData.totalExperienceForLevel(3, ItemAttributePetData.rarityOffset(Rarity.LEGENDARY)));
        assertEquals(97_985, ItemAttributePetData.totalExperienceForLevel(51, ItemAttributePetData.rarityOffset(Rarity.COMMON)));

        ItemAttributePetData.PetData data = new ItemAttributePetData.PetData(0);
        assertEquals(1, data.getAsLevel(20, 200));
        data.setLevel(150, 20, 200);
        assertEquals(150, data.getAsLevel(20, 200));
        assertEquals(100, data.getAsLevel(20, 100));
        data.setExperience(Double.MAX_VALUE / 2);
        assertEquals(200, data.getAsLevel(20, 200));
    }

    @Test
    void lionStatsAndAbilitiesScaleWithLevel() {
        SkyBlockItem lion = new SkyBlockItem(ItemType.LION_PET);
        lion.getAttributeHandler().setRarity(Rarity.LEGENDARY);
        PetComponent component = lion.getComponent(PetComponent.class);

        PetLevels.setLevel(lion, 1);
        ItemStatistics level1 = component.statisticsAt(Rarity.LEGENDARY, PetLevels.level(lion));
        assertEquals(0.5, level1.getOverall(ItemStatistic.STRENGTH), 1e-9);

        PetLevels.setLevel(lion, 100);
        ItemStatistics level100 = component.statisticsAt(Rarity.LEGENDARY, PetLevels.level(lion));
        assertEquals(50, level100.getOverall(ItemStatistic.STRENGTH), 1e-9);
        assertEquals(5, level100.getOverall(ItemStatistic.FEROCITY), 1e-9);
        assertEquals(25, level100.getOverall(ItemStatistic.SPEED), 1e-9);

        ConfiguredPetAbility primalForce = component.getAbilities().getFirst();
        assertEquals(20, primalForce.valuesFor(lion)[0], 1e-9);
        assertEquals(3, primalForce.valuesAt(Rarity.COMMON, 100)[0], 1e-9);
        assertEquals(0.03, primalForce.valuesAt(Rarity.COMMON, 1)[0], 1e-9);
    }

    @Test
    void maxPetsCoversEveryPetAndIsIdempotent() {
        Set<String> candidates = new TreeSet<>(MaxPets.petTypes().stream().map(ItemType::name).toList());
        assertEquals(YAML_PETS, candidates);

        DatapointPetData.UserPetData data = new DatapointPetData.UserPetData(new HashMap<>());
        MaxPets.maxOut(data);
        assertEquals(YAML_PETS.size(), data.getPetsMap().size());
        for (SkyBlockItem pet : data.getPetsMap().keySet()) {
            PetComponent component = pet.getComponent(PetComponent.class);
            assertEquals(component.getMaxRarity(), pet.getAttributeHandler().getRarity(), pet.getDisplayName());
            assertEquals(component.getMaxLevel(), PetLevels.level(pet), pet.getDisplayName());
        }
        int score = PetScore.of(data.getPetsMap().keySet());

        MaxPets.maxOut(data);
        assertEquals(YAML_PETS.size(), data.getPetsMap().size());
        assertEquals(score, PetScore.of(data.getPetsMap().keySet()));
    }

    @Test
    void maxPetsUpgradesAnOwnedCopyInPlace() {
        DatapointPetData.UserPetData data = new DatapointPetData.UserPetData(new HashMap<>());
        SkyBlockItem owned = new SkyBlockItem(ItemType.LION_PET);
        owned.getAttributeHandler().setRarity(Rarity.COMMON);
        data.addPet(owned);
        MaxPets.maxOut(data);
        assertTrue(data.getPetsMap().containsKey(owned));
        assertEquals(Rarity.LEGENDARY, owned.getAttributeHandler().getRarity());
        assertEquals(100, PetLevels.level(owned));
        assertEquals(1, data.getPetsMap().keySet().stream()
                .filter(pet -> pet.getAttributeHandler().getPotentialType() == ItemType.LION_PET).count());
    }

    @Test
    void savedPetsRoundTrip() throws Exception {
        DatapointPetData.UserPetData data = new DatapointPetData.UserPetData(new HashMap<>());
        MaxPets.maxOut(data);
        DatapointPetData datapoint = new DatapointPetData("pet_data", data);
        String json = datapoint.getSerializedValue();
        DatapointPetData restored = new DatapointPetData("pet_data");
        restored.deserializeValue(json);
        DatapointPetData.UserPetData loaded = restored.getValue();
        assertEquals(data.getPetsMap().size(), loaded.getPetsMap().size());
        assertEquals(PetScore.of(data.getPetsMap().keySet()), PetScore.of(loaded.getPetsMap().keySet()));
    }

    @Test
    void levelTwoHundredPetsUseTheirBaseLevel() {
        PetComponent dragon = PetComponentParser.parse(Map.of(
                "pet_name", "Golden Dragon",
                "pet_type", "COMBAT",
                "rarities", List.of("LEGENDARY"),
                "max_level", 200,
                "stats_base_level", 100,
                "skull_texture", "test",
                "statistics", Map.of("LEGENDARY", Map.of("strength", Map.of("base", 25, "per_level", 0.25))),
                "abilities", List.of(Map.of(
                        "name", "Gold's Power",
                        "kind", "TEXT",
                        "text", List.of("<7>by <a>{0}%"),
                        "values", List.of(Map.of("base", 5, "per_level", 0.05))))));
        assertEquals(0, dragon.statisticsAt(Rarity.LEGENDARY, 1).getOverall(ItemStatistic.STRENGTH), 1e-9);
        assertTrue(dragon.isEggForm(99));
        assertEquals(25, dragon.statisticsAt(Rarity.LEGENDARY, 100).getOverall(ItemStatistic.STRENGTH), 1e-9);
        assertEquals(50, dragon.statisticsAt(Rarity.LEGENDARY, 200).getOverall(ItemStatistic.STRENGTH), 1e-9);
        assertEquals(10, dragon.getAbilities().getFirst().valuesAt(Rarity.LEGENDARY, 200)[0], 1e-9);
        assertEquals(5, dragon.getAbilities().getFirst().valuesAt(Rarity.LEGENDARY, 100)[0], 1e-9);
    }

    @Test
    void snowmanValuesMatchNeuAtLevelsOneAndOneHundred() {
        PetComponent snowman = new SkyBlockItem(ItemType.SNOWMAN_PET).getComponent(PetComponent.class);
        ConfiguredPetAbility blizzard = ability(snowman, "Blizzard");
        assertEquals(8.08, blizzard.valuesAt(Rarity.LEGENDARY, 1)[0], 1e-9);
        assertEquals(16, blizzard.valuesAt(Rarity.LEGENDARY, 100)[0], 1e-9);
        assertEquals(10, blizzard.valuesAt(Rarity.LEGENDARY, 100)[1], 1e-9);
        assertEquals(10, ability(snowman, "Frostbite").valuesAt(Rarity.LEGENDARY, 100)[0], 1e-9);
        assertEquals(10.1, ability(snowman, "Snow Cannon").valuesAt(Rarity.LEGENDARY, 1)[0], 1e-9);
        assertEquals(20, ability(snowman, "Snow Cannon").valuesAt(Rarity.LEGENDARY, 100)[0], 1e-9);
    }

    @Test
    void loreOnlyFlagsAbilitiesThatAreNotActive() {
        SkyBlockItem griffin = new SkyBlockItem(ItemType.GRIFFIN_PET);
        griffin.getAttributeHandler().setRarity(Rarity.MYTHIC);
        PetLevels.setLevel(griffin, 100);
        PetComponent component = griffin.getComponent(PetComponent.class);
        assertFalse(ability(component, "King of Kings").getDescription(griffin).contains("<8>(Not active yet)"));
        for (ItemType type : ItemType.values()) {
            if (!type.name().endsWith("_PET")) continue;
            SkyBlockItem pet = new SkyBlockItem(type);
            if (!pet.hasComponent(PetComponent.class)) continue;
            for (ConfiguredPetAbility perk : pet.getComponent(PetComponent.class).getAbilities()) {
                if (perk.isActive() || perk.getUnavailable() != null) continue;
                pet.getAttributeHandler().setRarity(perk.getMinRarity());
                assertTrue(perk.getDescription(pet).contains("<8>(Not active yet)"));
            }
        }
    }

    private static ConfiguredPetAbility ability(PetComponent pet, String name) {
        return pet.getAbilities().stream().filter(a -> a.getName().equals(name)).findFirst().orElseThrow();
    }
}
