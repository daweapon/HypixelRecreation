package net.swofty.type.skyblockgeneric.pets;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.ConfiguredPetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetAbilityKind;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PetPerksUnavailableTest {
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

    private static SkyBlockItem pet(ItemType type, Rarity rarity, int level) {
        SkyBlockItem pet = new SkyBlockItem(type);
        pet.getAttributeHandler().setRarity(rarity);
        PetLevels.setLevel(pet, level);
        return pet;
    }

    private static ConfiguredPetAbility ability(SkyBlockItem pet, String name) {
        return pet.getComponent(PetComponent.class).getAbilities().stream()
                .filter(a -> a.getName().equals(name) && a.appliesAt(pet.getAttributeHandler().getRarity()))
                .findFirst().orElseThrow();
    }

    @Test
    void unavailablePerkIsInactiveAndSaysWhy() {
        for (int level : new int[]{1, 50, 100}) {
            SkyBlockItem sheep = pet(ItemType.SHEEP_PET, Rarity.LEGENDARY, level);
            ConfiguredPetAbility perk = ability(sheep, "Dungeon Wizard");
            assertEquals("Needs Dungeons", perk.getUnavailable());
            assertFalse(perk.isActive());
            assertEquals(PetAbilityKind.INACTIVE, perk.getKind());
            List<String> lines = perk.getDescription(sheep);
            assertEquals("<8>(Unavailable: Needs Dungeons)", lines.get(lines.size() - 1));
            assertFalse(lines.contains("<8>(Not active yet)"));
        }
    }

    @Test
    void unavailablePerkWithoutReasonStillSaysNotActive() {
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

    @Test
    void markedPerksAcrossSystemsAreUnavailable() {
        assertUnavailable(ItemType.BEE_PET, Rarity.MYTHIC, "Powered by Pollen", "Needs the Garden");
        assertUnavailable(ItemType.MONTEZUMA_PET, Rarity.LEGENDARY, "Nine Lives", "Needs the Rift");
        assertUnavailable(ItemType.MAMMOTH_PET, Rarity.LEGENDARY, "Corpse Crusher", "Needs Glacite Mineshafts");
        assertUnavailable(ItemType.EAGLE_PET, Rarity.LEGENDARY, "Critter Capturer", "Needs Critter Safari");
    }

    @Test
    void everyListedPerkCarriesAReason() {
        int marked = 0;
        for (ItemType type : ItemType.values()) {
            SkyBlockItem item = new SkyBlockItem(type);
            if (!item.hasComponent(PetComponent.class)) continue;
            PetComponent component = item.getComponent(PetComponent.class);
            for (ConfiguredPetAbility ability : component.getAbilities()) {
                if (ability.getUnavailable() == null) continue;
                marked++;
                assertFalse(ability.isActive(), type + " " + ability.getName());
                assertTrue(ability.getStatistics(null, item).getOverall(ItemStatistic.HEALTH) == 0);
                assertFalse(ability.getUnavailable().isBlank());
            }
        }
        assertTrue(marked >= 82, "marked " + marked);
    }

    private static void assertUnavailable(ItemType type, Rarity rarity, String perk, String reason) {
        SkyBlockItem pet = pet(type, rarity, 100);
        ConfiguredPetAbility ability = ability(pet, perk);
        assertEquals(reason, ability.getUnavailable());
        assertFalse(ability.isActive());
    }
}
