package dev.daweapon.type.skyblockgeneric.fishing.trophy;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.fishing.catches.TrophyTier;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingRegistry;
import dev.daweapon.type.skyblockgeneric.fishing.registry.TrophyFishDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TrophyFishCatalogueTest {

    @BeforeAll
    static void load() {
        FishingRegistry.loadAll(new File("../configuration/skyblock/fishing"));
    }

    @Test
    void eighteenFishInOdgersOrder() {
        List<String> ids = FishingRegistry.getTrophyFish().stream().map(TrophyFishDefinition::id).toList();
        assertEquals(18, ids.size());
        assertEquals("SULPHUR_SKITTER", ids.getFirst());
        assertEquals("GOLDEN_FISH", ids.getLast());
    }

    @Test
    void everyTierIsARealItemOfTheFishRarity() {
        for (TrophyFishDefinition fish : FishingRegistry.getTrophyFish()) {
            for (TrophyTier tier : TrophyTier.values()) {
                ItemType type = fish.itemType(tier);
                assertNotNull(type, fish.itemId(tier));
                assertEquals(fish.rarity(), type.rarity, fish.itemId(tier));
            }
            assertEquals(4, fish.filletMagmafish().size(), fish.id());
        }
    }

    @Test
    void oldSavedIdsBecomeHypixelIds() {
        assertEquals("LAVA_HORSE_BRONZE", TrophyFishLegacyIds.migrate("BRONZE_LAVAHORSE"));
        assertEquals("OBFUSCATED_FISH_2_DIAMOND", TrophyFishLegacyIds.migrate("DIAMOND_OBFUSCATED_2"));
        assertEquals("GOLDEN_FISH_GOLD", TrophyFishLegacyIds.migrate("GOLD_GOLDEN_FISH"));
        assertEquals("BRONZE_HUNTER_HELMET", TrophyFishLegacyIds.migrate("BRONZE_HUNTER_HELMET"));
        for (TrophyFishDefinition fish : FishingRegistry.getTrophyFish()) {
            for (TrophyTier tier : TrophyTier.values()) {
                assertNotNull(ItemType.get(TrophyFishLegacyIds.migrate(tier.name() + "_" + fish.id())), fish.id());
            }
        }
    }
}
