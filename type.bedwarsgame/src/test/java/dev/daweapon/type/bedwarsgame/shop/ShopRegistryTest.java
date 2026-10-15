package dev.daweapon.type.bedwarsgame.shop;

import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ShopRegistryTest {
    @Test
    void teamShopRegistersEveryUpgradeIdOnce() {
        TeamShopManager manager = new TeamShopManager();
        assertEquals(TeamUpgradeId.values().length, manager.getUpgrades().size());
        for (TeamUpgradeId id : TeamUpgradeId.values()) {
            assertNotNull(manager.getUpgrade(id), id.name());
            assertEquals(id, manager.getUpgrade(id).getId());
        }
    }

    @Test
    void upgradeTiersAreSequentialWithPositivePrices() {
        for (TeamUpgrade upgrade : new TeamShopManager().getUpgrades()) {
            assertFalse(upgrade.getTiers().isEmpty(), upgrade.getName());
            for (int i = 0; i < upgrade.getTiers().size(); i++) {
                TeamUpgradeTier tier = upgrade.getTiers().get(i);
                assertEquals(i + 1, tier.getLevel(), upgrade.getName());
                assertTrue(tier.getPrice() > 0, upgrade.getName());
            }
        }
    }

    @Test
    void trapManagerRegistersEveryTrapIdWithUniqueKeys() {
        TrapManager manager = new TrapManager();
        assertEquals(TrapId.values().length, manager.getTraps().size());
        Set<String> keys = new HashSet<>();
        for (TrapId id : TrapId.values()) {
            assertEquals(id, manager.getTrap(id).getId());
            assertTrue(keys.add(id.key()));
        }
    }

    @Test
    void upgradeIdKeysAreUnique() {
        Set<String> keys = new HashSet<>();
        for (TeamUpgradeId id : TeamUpgradeId.values()) {
            assertTrue(keys.add(id.key()));
        }
    }

    @Test
    void currencyLookupByMaterial() {
        assertEquals(Currency.IRON, Currency.byMaterial(Material.IRON_INGOT));
        assertEquals(Currency.EMERALD, Currency.byMaterial(Material.EMERALD));
        assertNull(Currency.byMaterial(Material.DIRT));
    }
}
