package net.swofty.type.skyblockgeneric.pets;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributePetData;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.TextParser;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointPetData;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.ConfiguredPetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetAbilityKind;
import net.swofty.type.skyblockgeneric.staff.MaxPets;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;
import net.swofty.type.skyblockgeneric.user.pets.PetScore;
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
        Set<String> candidates = new TreeSet<>(MaxPets.petTypes().stream().map(Enum::name).toList());
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
}
