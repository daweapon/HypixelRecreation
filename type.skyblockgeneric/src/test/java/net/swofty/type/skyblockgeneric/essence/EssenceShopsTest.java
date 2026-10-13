package net.swofty.type.skyblockgeneric.essence;

import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EssenceShopsTest {
    private static final List<EssenceType> SHOPS = List.of(EssenceType.WITHER, EssenceType.UNDEAD, EssenceType.GOLD,
            EssenceType.DIAMOND, EssenceType.SPIDER, EssenceType.DRAGON, EssenceType.FOREST, EssenceType.SAFARI,
            EssenceType.SUN_GECKO);

    @BeforeAll
    static void load() {
        EssenceShops.load(Path.of("../configuration/skyblock/essence_shops.json"));
    }

    @Test
    void everyRemainingEssenceHasAShopWithPerks() {
        for (EssenceType type : SHOPS) {
            assertTrue(EssenceShops.shop(type).isPresent(), type.name());
            assertFalse(EssenceShops.shop(type).get().perks().isEmpty(), type.name());
        }
    }

    @Test
    void everyPerkHasOneValueAndOneXpEntryPerTier() {
        for (EssenceType type : SHOPS) {
            EssenceShop<CataloguePerk> shop = EssenceShops.shop(type).orElseThrow();
            for (CataloguePerk perk : shop.perks()) {
                assertEquals(perk.getCosts().length, perk.getValues().length, perk.name());
                assertNotNull(perk.description(), perk.name());
            }
        }
    }

    @Test
    void skyBlockXpAddsUpToEachShopTask() {
        assertEquals(SkyBlockXPSource.WITHER_ESSENCE_SHOP.getMaxXP(), EssenceShops.shop(EssenceType.WITHER).get().maxSkyBlockXP());
        assertEquals(SkyBlockXPSource.UNDEAD_ESSENCE_SHOP.getMaxXP(), EssenceShops.shop(EssenceType.UNDEAD).get().maxSkyBlockXP());
        assertEquals(SkyBlockXPSource.GOLD_ESSENCE_SHOP.getMaxXP(), EssenceShops.shop(EssenceType.GOLD).get().maxSkyBlockXP());
        assertEquals(SkyBlockXPSource.DIAMOND_ESSENCE_SHOP.getMaxXP(), EssenceShops.shop(EssenceType.DIAMOND).get().maxSkyBlockXP());
        assertEquals(SkyBlockXPSource.SPIDER_ESSENCE_SHOP.getMaxXP(), EssenceShops.shop(EssenceType.SPIDER).get().maxSkyBlockXP());
        assertEquals(SkyBlockXPSource.DRAGON_ESSENCE_SHOP.getMaxXP(), EssenceShops.shop(EssenceType.DRAGON).get().maxSkyBlockXP());
        assertEquals(SkyBlockXPSource.FOREST_ESSENCE_SHOP.getMaxXP(), EssenceShops.shop(EssenceType.FOREST).get().maxSkyBlockXP());
        assertEquals(SkyBlockXPSource.SAFARI_ESSENCE_SHOP.getMaxXP(), EssenceShops.shop(EssenceType.SAFARI).get().maxSkyBlockXP());
    }

    @Test
    void shopTotalsMatchTheWiki() {
        assertEquals(33_500, total(EssenceType.WITHER));
        assertEquals(540_600, total(EssenceType.UNDEAD));
        assertEquals(49_300, total(EssenceType.GOLD));
        assertEquals(44_400, total(EssenceType.DIAMOND));
        assertEquals(48_350 + 7_500, total(EssenceType.SAFARI));
    }

    private static final java.util.Set<String> CODE_WIRED = java.util.Set.of("treasures_of_the_earth");

    @Test
    void wiredPerksCanBeBoughtAndTheRestSayWhyNot() {
        CataloguePerk health = perk(EssenceType.WITHER, "FORBIDDEN_HEALTH");
        assertEquals(EssenceShop.Result.TOO_POOR, EssenceShops.shop(EssenceType.WITHER).get().check(0, health, 99));
        assertEquals(EssenceShop.Result.BOUGHT, EssenceShops.shop(EssenceType.WITHER).get().check(0, health, 100));
        for (EssenceType type : SHOPS) {
            for (CataloguePerk perk : EssenceShops.shop(type).get().perks()) {
                assertTrue(perk.getUnavailableReason() != null || perk.stat() != null || CODE_WIRED.contains(perk.id()), perk.name() + " has no effect and no reason");
            }
        }
    }

    private static CataloguePerk perk(EssenceType type, String displayName) {
        return EssenceShops.shop(type).get().perks().stream()
                .filter(p -> p.getDisplayName().toUpperCase().replace(' ', '_').equals(displayName)).findFirst().orElseThrow();
    }

    private static long total(EssenceType type) {
        return EssenceShops.shop(type).get().perks().stream().mapToLong(EssencePerk::totalCost).sum();
    }
}
