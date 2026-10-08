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
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc.MiscPetHooks;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.ConfiguredPetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetType;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiscPetsTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final Map<String, PetType> MISC = Map.ofEntries(
            Map.entry("BINGO_PET", PetType.ALL), Map.entry("EAGLE_PET", PetType.HUNTING),
            Map.entry("GUARDIAN_PET", PetType.ENCHANTING), Map.entry("HEDGEHOG_PET", PetType.FARMING),
            Map.entry("JELLYFISH_PET", PetType.ALCHEMY), Map.entry("MONTEZUMA_PET", PetType.FRACTURED_SOUL),
            Map.entry("MOSQUITO_PET", PetType.FARMING), Map.entry("ORCHID_MANTIS_PET", PetType.FARMING),
            Map.entry("OWL_PET", PetType.TAMING), Map.entry("PARROT_PET", PetType.ALCHEMY),
            Map.entry("PIG_PET", PetType.FARMING), Map.entry("RABBIT_PET", PetType.FARMING),
            Map.entry("ROSE_DRAGON_PET", PetType.FARMING), Map.entry("SHEEP_PET", PetType.ALCHEMY),
            Map.entry("SLUG_PET", PetType.FARMING), Map.entry("DROPLET_WISP_PET", PetType.GABAGOOL),
            Map.entry("FROST_WISP_PET", PetType.GABAGOOL), Map.entry("GLACIAL_WISP_PET", PetType.GABAGOOL),
            Map.entry("SUBZERO_WISP_PET", PetType.GABAGOOL), Map.entry("WITCH_PET", PetType.ALCHEMY));

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
    void everyMiscPetLoadsWithItsSkill() {
        MISC.forEach((id, type) -> {
            SkyBlockItem pet = new SkyBlockItem(ItemType.valueOf(id));
            assertTrue(pet.hasComponent(PetComponent.class), id);
            assertEquals(type, pet.getComponent(PetComponent.class).getPetType(), id);
        });
    }

    @Test
    void roseDragonScalesToLevelTwoHundred() {
        PetComponent rose = component(ItemType.ROSE_DRAGON_PET);
        assertEquals(200, rose.getMaxLevel());
        assertTrue(rose.isEggForm(99));
        assertEquals(40, rose.statisticsAt(Rarity.LEGENDARY, 200).getOverall(ItemStatistic.FARMING_FORTUNE), 1e-9);
        assertEquals(3, ability(rose, "Garden Power").valuesAt(Rarity.LEGENDARY, 200)[0], 1e-9);
        assertEquals(40, ability(rose, "Dragon's Gluttony").valuesAt(Rarity.LEGENDARY, 200)[0], 1e-9);
        assertTrue(ability(rose, "Symbiosis").isActive());
        assertFalse(ability(rose, "Rosy Scales").isActive());
    }

    @Test
    void specialLevellingRulesFollowHypixel() {
        PetComponent bingo = component(ItemType.BINGO_PET);
        assertEquals(0, bingo.xpOffset(Rarity.LEGENDARY));
        assertTrue(bingo.isPassive());
        assertTrue(component(ItemType.OWL_PET).isPassive());
        PetComponent eagle = component(ItemType.EAGLE_PET);
        assertTrue(eagle.isFixedLevel());
        assertFalse(eagle.isCountsForPetScore());
        assertTrue(component(ItemType.MONTEZUMA_PET).isFixedLevel());
    }

    @Test
    void eachWispIsItsOwnPetAtOneRarity() {
        assertEquals(List.of(Rarity.UNCOMMON), component(ItemType.DROPLET_WISP_PET).getRarities());
        assertEquals(List.of(Rarity.RARE), component(ItemType.FROST_WISP_PET).getRarities());
        assertEquals(List.of(Rarity.EPIC), component(ItemType.GLACIAL_WISP_PET).getRarities());
        assertEquals(List.of(Rarity.LEGENDARY), component(ItemType.SUBZERO_WISP_PET).getRarities());
        assertEquals("Subzero Wisp", component(ItemType.SUBZERO_WISP_PET).getPetName());
        assertTrue(ability(component(ItemType.DROPLET_WISP_PET), "Blaze Slayer").isActive());
        assertTrue(ability(component(ItemType.SUBZERO_WISP_PET), "Ephemeral Stability").isActive());
    }

    @Test
    void wiredAbilitiesAreActiveAndUnbuiltOnesAreNot() {
        PetComponent guardian = component(ItemType.GUARDIAN_PET);
        assertTrue(ability(guardian, "Lazerbeam").isActive());
        assertTrue(ability(guardian, "Mana Pool").isActive());
        assertFalse(ability(guardian, "Lucky Seven").isActive());
        assertTrue(ability(component(ItemType.ORCHID_MANTIS_PET), "Swift Sickles").isActive());
        assertTrue(ability(component(ItemType.RABBIT_PET), "Efficient Farming").isActive());
        assertTrue(ability(component(ItemType.PARROT_PET), "Flamboyant").isActive());
        assertFalse(ability(component(ItemType.HEDGEHOG_PET), "Spiky Quills").isActive());
        assertFalse(ability(component(ItemType.JELLYFISH_PET), "Stored Energy").isActive());
    }

    @Test
    void owlTrainsItselfMoreEfficiently() {
        SkyBlockItem owl = new SkyBlockItem(ItemType.OWL_PET);
        owl.getAttributeHandler().setRarity(Rarity.LEGENDARY);
        PetLevels.setLevel(owl, 100);
        assertEquals(1.1, MiscPetHooks.trainingXpMultiplier(owl), 1e-9);
        assertEquals(1, MiscPetHooks.trainingXpMultiplier(new SkyBlockItem(ItemType.SHEEP_PET)), 1e-9);
    }

    @Test
    void onlyUnbuiltSourcesAppearInLore() {
        assertNotNull(component(ItemType.HEDGEHOG_PET).getSource());
        assertTrue(lore(ItemType.HEDGEHOG_PET).stream().anyMatch(line -> line.contains("Pesthunter Phillip (Not active yet)")));
        assertTrue(lore(ItemType.ANKYLOSAURUS_PET).stream().anyMatch(line -> line.contains("Source: The Forge")));
        assertNull(component(ItemType.RABBIT_PET).getSource());
        assertTrue(lore(ItemType.RABBIT_PET).stream().noneMatch(line -> line.contains("Source:")));
    }

    private static List<String> lore(ItemType type) {
        SkyBlockItem pet = new SkyBlockItem(type);
        return pet.getComponent(PetComponent.class).getAbsoluteLore(null, pet);
    }

    private static PetComponent component(ItemType type) {
        return new SkyBlockItem(type).getComponent(PetComponent.class);
    }

    private static ConfiguredPetAbility ability(PetComponent pet, String name) {
        return pet.getAbilities().stream().filter(a -> a.getName().equals(name)).findFirst().orElseThrow();
    }
}
