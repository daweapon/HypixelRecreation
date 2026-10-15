package dev.daweapon.type.skyblockgeneric.fossil;

import dev.daweapon.type.skyblockgeneric.essence.EssencePerk;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.FossilEssencePerk;
import dev.daweapon.type.skyblockgeneric.essence.FossilEssenceShop;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FossilCatalogueTest {
    @Test
    void everyShapeFitsTheGridAndHasTiles() {
        for (FossilType type : FossilType.values()) {
            assertTrue(type.tileCount() >= 6, type.name());
            assertTrue(type.width() <= ExcavatorGame.COLS && type.height() <= ExcavatorGame.ROWS, type.name());
            assertTrue(type.getShape().stream().allMatch(row -> row.chars().allMatch(c -> c == 'X' || c == '.')), type.name());
        }
    }

    @Test
    void shapesAreAllDifferent() {
        Set<java.util.List<String>> shapes = new HashSet<>();
        for (FossilType type : FossilType.values()) assertTrue(shapes.add(type.getShape()), type.name());
    }

    @Test
    void sixFossilsUnlockAPetRecipeAndHelixAndClawDoNot() {
        assertEquals(6, Arrays.stream(FossilType.values()).filter(FossilType::unlocksPetRecipe).count());
        assertFalse(FossilType.HELIX.unlocksPetRecipe());
        assertFalse(FossilType.CLAW.unlocksPetRecipe());
        assertEquals(FossilType.FOOTPRINT, FossilType.byPetRecipe("TYRANNOSAURUS;4"));
        assertNull(FossilType.byPetRecipe("AMMONITE;4"));
    }

    @Test
    void maxingTheEssenceShopCosts66250() {
        long total = Arrays.stream(FossilEssencePerk.values()).mapToLong(EssencePerk::totalCost).sum();
        assertEquals(66_250, total);
    }

    @Test
    void perkRangesMatchTheWiki() {
        assertEquals(1, FossilEssencePerk.SLEIGHT_OF_HAND.valueAt(1));
        assertEquals(2, FossilEssencePerk.DWARVEN_EXPERTISE.valueAt(1));
        assertEquals(20, FossilEssencePerk.DWARVEN_EXPERTISE.valueAt(10));
        assertEquals(5_500, FossilEssencePerk.DWARVEN_EXPERTISE.totalCost());
        assertEquals(2.5, FossilEssencePerk.CUT_LOOSE.valueAt(5));
        assertEquals(7_500, FossilEssencePerk.CUT_LOOSE.totalCost());
        assertEquals(11_000, FossilEssencePerk.CHILLED_TO_THE_BONE.totalCost());
        assertEquals(2_750, FossilEssencePerk.PREHISTORIAN.totalCost());
        assertEquals(37_500, FossilEssencePerk.RESOURCEFUL.totalCost());
    }

    @Test
    void everyTierTogetherIsWorthTheWholeShopTask() {
        assertEquals(SkyBlockXPSource.FOSSIL_ESSENCE_SHOP.getMaxXP(), FossilEssenceShop.maxSkyBlockXP());
    }

    @Test
    void onlyPerksWithAWorkingEffectCanBeBought() {
        assertNull(FossilEssencePerk.SLEIGHT_OF_HAND.getUnavailableReason());
        assertNull(FossilEssencePerk.DWARVEN_EXPERTISE.getUnavailableReason());
        assertNotNull(FossilEssencePerk.CUT_LOOSE.getUnavailableReason());
        assertNotNull(FossilEssencePerk.RESOURCEFUL.getUnavailableReason());
    }

    @Test
    void fossilEssenceIsAvailableAndPaysSkyBlockXp() {
        assertTrue(EssenceType.FOSSIL.isAvailable());
        assertEquals(SkyBlockXPSource.Availability.LIVE, SkyBlockXPSource.FOSSIL_RESEARCH.getAvailability());
        assertEquals(80, SkyBlockXPSource.FOSSIL_RESEARCH.getMaxXP());
    }
}
