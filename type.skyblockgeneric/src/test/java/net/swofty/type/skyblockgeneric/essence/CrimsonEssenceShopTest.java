package net.swofty.type.skyblockgeneric.essence;

import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CrimsonEssenceShopTest {
    @Test
    void skyBlockXPAddsUpToTheSource() {
        assertEquals(SkyBlockXPSource.CRIMSON_ESSENCE_SHOP.getMaxXP(), CrimsonEssenceShop.SHOP.maxSkyBlockXP());
    }

    @Test
    void maxingEveryPerkCostsTheWikiTotal() {
        assertEquals(37_500, Arrays.stream(CrimsonEssencePerk.values()).mapToLong(EssencePerk::totalCost).sum());
    }

    @Test
    void kuudraPerksAreUnavailable() {
        assertEquals(EssenceShop.Result.UNAVAILABLE,
                CrimsonEssenceShop.SHOP.check(0, CrimsonEssencePerk.HEADSTART, Long.MAX_VALUE));
        assertEquals(EssenceShop.Result.BOUGHT,
                CrimsonEssenceShop.SHOP.check(0, CrimsonEssencePerk.FUNGUS_FORTUNA, 100));
    }
}
