package net.swofty.type.skyblockgeneric.pets;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.fishing.festival.FestivalShark;
import net.swofty.type.skyblockgeneric.fishing.festival.SharkChanceSource;
import net.swofty.type.skyblockgeneric.fishing.festival.SharkDrop;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatheringPetsTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");
    private static final Map<String, PetType> GATHERING = Map.ofEntries(
            Map.entry("AMMONITE_PET", PetType.FISHING), Map.entry("DOLPHIN_PET", PetType.FISHING),
            Map.entry("FLYING_FISH_PET", PetType.FISHING), Map.entry("HERMIT_CRAB_PET", PetType.FISHING),
            Map.entry("MEGALODON_PET", PetType.FISHING), Map.entry("PENGUIN_PET", PetType.FISHING),
            Map.entry("SEAL_PET", PetType.FISHING), Map.entry("SPINOSAURUS_PET", PetType.FISHING),
            Map.entry("SQUID_PET", PetType.FISHING), Map.entry("ARMADILLO_PET", PetType.MINING),
            Map.entry("BAL_PET", PetType.MINING), Map.entry("BAT_PET", PetType.MINING),
            Map.entry("ENDERMITE_PET", PetType.MINING), Map.entry("GLACITE_GOLEM_PET", PetType.MINING),
            Map.entry("GOBLIN_PET", PetType.MINING), Map.entry("MITHRIL_GOLEM_PET", PetType.MINING),
            Map.entry("MOLE_PET", PetType.MINING), Map.entry("ROCK_PET", PetType.MINING),
            Map.entry("SCATHA_PET", PetType.MINING), Map.entry("SILVERFISH_PET", PetType.MINING),
            Map.entry("WITHER_SKELETON_PET", PetType.MINING), Map.entry("FROG_PET", PetType.FORAGING),
            Map.entry("JADE_DRAGON_PET", PetType.FORAGING), Map.entry("OCELOT_PET", PetType.FORAGING),
            Map.entry("SLOTH_PET", PetType.FORAGING));

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
    void everyGatheringPetLoadsWithItsSkill() {
        GATHERING.forEach((id, type) -> {
            SkyBlockItem pet = new SkyBlockItem(ItemType.valueOf(id));
            assertTrue(pet.hasComponent(PetComponent.class), id);
            assertEquals(type, pet.getComponent(PetComponent.class).getPetType(), id);
        });
    }

    @Test
    void jadeDragonScalesToLevelTwoHundred() {
        PetComponent jade = component(ItemType.JADE_DRAGON_PET);
        assertEquals(200, jade.getMaxLevel());
        assertEquals(50, jade.statisticsAt(Rarity.LEGENDARY, 200).getOverall(ItemStatistic.FORAGING_FORTUNE), 1e-9);
        assertEquals(100, ability(jade, "Forest Power").valuesAt(Rarity.LEGENDARY, 200)[0], 1e-9);
        assertEquals(15, ability(jade, "Jade Scale").valuesAt(Rarity.LEGENDARY, 150)[0], 1e-9);
    }

    @Test
    void frogHappyTreeFriendsOnlyAddsFishingSpeedAtMythic() {
        ConfiguredPetAbility friends = ability(component(ItemType.FROG_PET), "Happy Tree Friends");
        assertEquals(7.5, friends.valuesAt(Rarity.RARE, 100)[0], 1e-9);
        assertEquals(10, friends.valuesAt(Rarity.MYTHIC, 100)[0], 1e-9);
        assertEquals(0, friends.valuesAt(Rarity.LEGENDARY, 100)[1], 1e-9);
        assertEquals(2, friends.valuesAt(Rarity.MYTHIC, 100)[1], 1e-9);
    }

    @Test
    void wiredAbilitiesAreActiveAndUnbuiltOnesAreNot() {
        PetComponent ammonite = component(ItemType.AMMONITE_PET);
        assertTrue(ability(ammonite, "Heart of the Sea").isActive());
        assertTrue(ability(ammonite, "Gift of the Ammonite").isActive());
        assertFalse(ability(ammonite, "Expert Cave Fisher").isActive());
        assertTrue(ability(component(ItemType.BAT_PET), "Nightmare").isActive());
        assertTrue(ability(component(ItemType.JADE_DRAGON_PET), "Symbiosis").isActive());
        assertFalse(ability(component(ItemType.ROCK_PET), "Rideable").isActive());
    }

    @Test
    void megalodonDropsFromSharksAtTheirRarities() {
        assertEquals(Rarity.EPIC, megalodonDrop(FestivalShark.TIGER_SHARK).rarity());
        assertEquals(Rarity.LEGENDARY, megalodonDrop(FestivalShark.GREAT_WHITE_SHARK).rarity());
        assertTrue(SharkChanceSource.MEGALODON_PET.isAvailable());
        SkyBlockItem megalodon = new SkyBlockItem(ItemType.MEGALODON_PET);
        megalodon.getAttributeHandler().setRarity(Rarity.LEGENDARY);
        PetLevels.setLevel(megalodon, 100);
        assertEquals(20, ability(component(ItemType.MEGALODON_PET), "Feeding Frenzy").valuesFor(megalodon)[0], 1e-9);
    }

    private static SharkDrop megalodonDrop(FestivalShark shark) {
        return shark.getExtraDrops().stream().filter(drop -> drop.item() == ItemType.MEGALODON_PET).findFirst().orElseThrow();
    }

    private static PetComponent component(ItemType type) {
        return new SkyBlockItem(type).getComponent(PetComponent.class);
    }

    private static ConfiguredPetAbility ability(PetComponent pet, String name) {
        return pet.getAbilities().stream().filter(a -> a.getName().equals(name)).findFirst().orElseThrow();
    }
}
