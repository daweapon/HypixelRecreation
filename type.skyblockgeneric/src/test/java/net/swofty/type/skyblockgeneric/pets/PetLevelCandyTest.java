package net.swofty.type.skyblockgeneric.pets;

import net.minestom.server.MinecraftServer;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.type.skyblockgeneric.item.ItemConfigParser;
import net.swofty.type.skyblockgeneric.item.SafeConfig;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetCandyComponent;
import net.swofty.type.skyblockgeneric.user.pets.PetCandy;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PetLevelCandyTest {
    private static final Path ITEMS = Path.of("../configuration/skyblock/items");

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

    private static PetCandyComponent candy(Map<String, Object> settings) {
        return PetCandyComponent.parse(SafeConfig.of(new HashMap<>(settings)));
    }

    private static SkyBlockItem lion(int level) {
        SkyBlockItem pet = new SkyBlockItem(ItemType.LION_PET);
        pet.getAttributeHandler().setRarity(Rarity.LEGENDARY);
        PetLevels.setLevel(pet, level);
        return pet;
    }

    @Test
    void levelCandyRaisesTheLevelWithoutUsingTheLifetimeCap() {
        PetCandyComponent feather = candy(Map.of("levels", 1, "counts_towards_cap", false));
        assertTrue(feather.grantsLevels());
        SkyBlockItem pet = lion(10);
        assertTrue(PetCandy.apply(pet, feather));
        assertEquals(11, PetLevels.level(pet));
        assertEquals(0, pet.getAttributeHandler().getPetCandyUsed());
    }

    @Test
    void levelCandyNeverPassesTheMaxLevel() {
        PetCandyComponent feather = candy(Map.of("levels", 5, "counts_towards_cap", false));
        SkyBlockItem pet = lion(PetLevels.maxLevel(lion(1)) - 2);
        assertTrue(PetCandy.apply(pet, feather));
        assertTrue(PetLevels.isMaxLevel(pet));
        assertFalse(PetCandy.apply(pet, feather));
    }

    @Test
    void maxLevelCandyJumpsStraightToTheCap() {
        PetCandyComponent cupcake = candy(Map.of("to_max_level", true, "counts_towards_cap", false));
        SkyBlockItem pet = lion(1);
        assertTrue(PetCandy.apply(pet, cupcake));
        assertTrue(PetLevels.isMaxLevel(pet));
        assertFalse(PetCandy.apply(pet, cupcake));
    }

    @Test
    void experienceCandyIsNotALevelCandy() {
        assertFalse(candy(Map.of("experience", 20000.0)).grantsLevels());
    }
}
