package dev.daweapon.type.skyblockgeneric.spidersden;

import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.region.mining.MineableBlock;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GrandmaWolfPetTest {
    @Test
    void upgradeChainAndMilestones() {
        assertEquals(Rarity.UNCOMMON, GrandmaWolfPet.next(Rarity.COMMON));
        assertEquals(Rarity.RARE, GrandmaWolfPet.next(Rarity.UNCOMMON));
        assertEquals(Rarity.EPIC, GrandmaWolfPet.next(Rarity.RARE));
        assertEquals(Rarity.LEGENDARY, GrandmaWolfPet.next(Rarity.EPIC));
        assertNull(GrandmaWolfPet.next(Rarity.LEGENDARY));
        assertEquals(5, GrandmaWolfPet.milestoneFor(Rarity.UNCOMMON));
        assertEquals(10, GrandmaWolfPet.milestoneFor(Rarity.RARE));
        assertEquals(15, GrandmaWolfPet.milestoneFor(Rarity.EPIC));
        assertEquals(20, GrandmaWolfPet.milestoneFor(Rarity.LEGENDARY));
    }

    @Test
    void thePetItemExists() {
        assertNotNull(GrandmaWolfPet.type());
    }

    @Test
    void gravelBreaksOnVanillaTimings() {
        MineableBlock gravel = MineableBlock.get(Block.GRAVEL);
        assertNotNull(gravel);
        assertEquals(true, gravel.getMiningHandler().usesVanillaBreakTime());
    }
}
