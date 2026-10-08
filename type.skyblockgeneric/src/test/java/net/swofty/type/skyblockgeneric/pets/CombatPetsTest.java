package net.swofty.type.skyblockgeneric.pets;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.ConfiguredPetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetAbilityKind;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatPetsTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final List<String> HYPIXEL_COMBAT_PETS = List.of(
            "ANKYLOSAURUS", "BLACK_CAT", "BLAZE", "CROW", "EERIE", "ENDER_DRAGON", "ENDERMAN", "GHOUL",
            "GOLDEN_DRAGON", "GOLEM", "GRANDMA_WOLF", "GRIFFIN", "HORSE", "HOUND", "JERRY", "KUUDRA", "MAGMA_CUBE",
            "MAMMOTH", "PHOENIX", "PIGMAN", "PRECURSOR_DRONE", "RAT", "RIFT_FERRET", "SKELETON", "SKELETON_HORSE",
            "SNOWMAN", "SPIDER", "SPIRIT", "TYRANNOSAURUS", "TARANTULA", "TIGER", "TURTLE", "WOLF", "ZOMBIE");

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

    @Test
    void everyHypixelCombatPetExists() {
        for (String id : HYPIXEL_COMBAT_PETS) {
            PetComponent pet = component(ItemType.valueOf(id + "_PET"));
            assertNotNull(pet, id);
            assertEquals(PetType.COMBAT, pet.getPetType(), id);
        }
    }

    @Test
    void goldenDragonHatchesAtOneHundredAndMaxesAtTwoHundred() {
        PetComponent dragon = component(ItemType.GOLDEN_DRAGON_PET);
        assertEquals(200, dragon.getMaxLevel());
        assertTrue(dragon.isEggForm(99));
        assertEquals(25, dragon.statisticsAt(Rarity.LEGENDARY, 100).getOverall(ItemStatistic.STRENGTH), 1e-9);
        assertEquals(50, dragon.statisticsAt(Rarity.LEGENDARY, 200).getOverall(ItemStatistic.BONUS_ATTACK_SPEED), 1e-9);
        assertEquals(10, ability(dragon, "Gold's Power").valuesAt(Rarity.LEGENDARY, 200)[0], 1e-9);
        assertEquals(250, ability(dragon, "Legendary Treasure").valuesAt(Rarity.LEGENDARY, 200)[1], 1e-9);
    }

    @Test
    void phoenixIsSpecialAndGivesNoPetScore() {
        PetComponent phoenix = component(ItemType.PHOENIX_PET);
        assertEquals(List.of(Rarity.SPECIAL, Rarity.VERY_SPECIAL), phoenix.getRarities());
        assertFalse(phoenix.isCountsForPetScore());
        assertTrue(phoenix.getKatUpgrades().isEmpty());
    }

    @Test
    void armoredTankConvertsHalfOfDefenseAtLevelOneHundred() {
        ConfiguredPetAbility armoredTank = ability(component(ItemType.ANKYLOSAURUS_PET), "Armored Tank");
        assertEquals(PetAbilityKind.STAT_PER_STAT, armoredTank.getKind());
        double[] values = armoredTank.valuesAt(Rarity.LEGENDARY, 100);
        assertEquals(50, values[0], 1e-9);
        assertEquals(0.5, values[1], 1e-9);
        assertEquals(500, armoredTank.getPerStat().max(), 1e-9);
    }

    @Test
    void wiredAbilitiesAreActiveAndUnbuiltOnesAreNot() {
        assertTrue(ability(component(ItemType.ENDER_DRAGON_PET), "End Strike").isActive());
        assertTrue(ability(component(ItemType.WOLF_PET), "Alpha Dog").isActive());
        assertTrue(ability(component(ItemType.GOLEM_PET), "Toss").isActive());
        assertFalse(ability(component(ItemType.SPIRIT_PET), "Half Life").isActive());
        assertFalse(ability(component(ItemType.PRECURSOR_DRONE_PET), "Creative Mind").isActive());
        assertTrue(component(ItemType.KUUDRA_PET).isPassive());
    }

    private static PetComponent component(ItemType type) {
        SkyBlockItem item = new SkyBlockItem(type);
        return item.hasComponent(PetComponent.class) ? item.getComponent(PetComponent.class) : null;
    }

    private static ConfiguredPetAbility ability(PetComponent pet, String name) {
        return pet.getAbilities().stream().filter(a -> a.getName().equals(name)).findFirst().orElseThrow();
    }
}
