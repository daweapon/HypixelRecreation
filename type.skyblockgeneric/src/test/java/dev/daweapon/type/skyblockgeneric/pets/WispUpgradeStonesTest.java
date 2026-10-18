package dev.daweapon.type.skyblockgeneric.pets;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.user.pets.WispUpgradeStones;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class WispUpgradeStonesTest {
    @Test
    void stonesChainTheFourWispsInOrder() {
        assertEquals(new WispUpgradeStones.Upgrade(ItemType.DROPLET_WISP_PET, ItemType.FROST_WISP_PET),
                WispUpgradeStones.upgradeFor(ItemType.UPGRADE_STONE_FROST));
        assertEquals(new WispUpgradeStones.Upgrade(ItemType.FROST_WISP_PET, ItemType.GLACIAL_WISP_PET),
                WispUpgradeStones.upgradeFor(ItemType.UPGRADE_STONE_GLACIAL));
        assertEquals(new WispUpgradeStones.Upgrade(ItemType.GLACIAL_WISP_PET, ItemType.SUBZERO_WISP_PET),
                WispUpgradeStones.upgradeFor(ItemType.UPGRADE_STONE_SUBZERO));
        assertNull(WispUpgradeStones.upgradeFor(ItemType.FROST_WISP_PET));
    }

    @Test
    void petMustBeTheRightWispAtLevelEighty() {
        WispUpgradeStones.Upgrade frost = WispUpgradeStones.upgradeFor(ItemType.UPGRADE_STONE_FROST);
        assertNull(WispUpgradeStones.problem(frost, ItemType.DROPLET_WISP_PET, 80));
        assertNull(WispUpgradeStones.problem(frost, ItemType.DROPLET_WISP_PET, 100));
        assertNotNull(WispUpgradeStones.problem(frost, ItemType.DROPLET_WISP_PET, 79));
        assertNotNull(WispUpgradeStones.problem(frost, ItemType.FROST_WISP_PET, 100));
    }
}
