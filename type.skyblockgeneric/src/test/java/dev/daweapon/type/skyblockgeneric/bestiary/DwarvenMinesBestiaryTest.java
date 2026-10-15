package dev.daweapon.type.skyblockgeneric.bestiary;

import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.goblin.MobGoblin;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategories;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategory;
import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DwarvenMinesBestiaryTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
    }

    @Test
    void dwarvenMinesCategoryListsEveryBuiltFamily() {
        assertTrue(BestiaryCategories.DWARVEN_MINES.getEntries().length >= 5);
        assertFalse(BestiaryCategories.DWARVEN_MINES.getAllMobs().isEmpty());
    }

    @Test
    void everyMobHasAUniqueIdAndAnIcon() {
        Set<String> ids = new HashSet<>();
        for (BestiaryMob mob : BestiaryCategories.DWARVEN_MINES.getAllMobs()) {
            assertTrue(ids.add(mob.getMobID()), mob.getMobID());
        }
        for (BestiaryCategory.DWARVEN_MINES entry : BestiaryCategory.DWARVEN_MINES.values()) assertNotNull(entry.getGuiMaterial(), entry.name());
    }

    @Test
    void theGoblinFamilyHoldsAllFourGoblinKinds() {
        assertEquals(4, BestiaryCategory.DWARVEN_MINES.GOBLIN.getMobs().size());
    }

    @Test
    void goblinsPopulateAThirdFewerThanBefore() {
        assertTrue(new MobGoblin().getPopulators().getFirst().minimumAmountToPopulate() <= 13);
    }
}
