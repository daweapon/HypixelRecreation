package net.swofty.type.skyblockgeneric.essence;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IceEssenceShopTest {

    @Test
    void everyTierTogetherIsWorthTheWholeIceEssenceShopTask() {
        assertEquals(SkyBlockXPSource.ICE_ESSENCE_SHOP.getMaxXP(), IceEssenceShop.maxSkyBlockXP());
    }

    @Test
    void perPerkXpMatchesTheLevelingMenu() {
        assertEquals(19, IceEssenceShop.skyBlockXP(perk -> perk == IceEssencePerk.FROZEN_SKIN ? 5 : 0));
        assertEquals(62, IceEssenceShop.skyBlockXP(perk -> perk == IceEssencePerk.SEASON_OF_JOY ? 10 : 0));
        assertEquals(0, IceEssenceShop.skyBlockXP(perk -> 0));
    }

    @Test
    void buyingNeedsTheNextTiersCost() {
        IceEssencePerk perk = IceEssencePerk.SEASON_OF_JOY;
        assertEquals(IceEssenceShop.Result.TOO_POOR, IceEssenceShop.check(0, perk, 199));
        assertEquals(IceEssenceShop.Result.BOUGHT, IceEssenceShop.check(0, perk, 200));
        assertEquals(IceEssenceShop.Result.TOO_POOR, IceEssenceShop.check(3, perk, 1_499));
        assertEquals(IceEssenceShop.Result.BOUGHT, IceEssenceShop.check(3, perk, 1_500));
        assertEquals(IceEssenceShop.Result.MAXED, IceEssenceShop.check(10, perk, Long.MAX_VALUE));
    }

    @Test
    void perksWithoutAnEffectCannotBeBought() {
        assertEquals(IceEssenceShop.Result.UNAVAILABLE,
                IceEssenceShop.check(0, IceEssencePerk.COLD_EFFICIENCY, 1_000_000));
    }

    @Test
    void seasonOfJoyNeverTakesGiftsAway() {
        UUID player = null;
        assertEquals(7, IceEssenceShop.boostedCount(player, ItemType.WHITE_GIFT, 7, 0));
        assertEquals(0, IceEssenceShop.boostedCount(player, ItemType.WHITE_GIFT, 0, 10));
        for (int i = 0; i < 200; i++) {
            int boosted = IceEssenceShop.boostedCount(player, ItemType.WHITE_GIFT, 5, 10);
            assertEquals(true, boosted == 5 || boosted == 6);
        }
    }
}
