package dev.daweapon.type.skyblockgeneric.hunting.npc;

import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureCatalog;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HuntingTutorialProgressTest {
    @Test
    void huntTriggersAreLowercaseWithUnderscores() {
        assertEquals("hunt.phanpyre", HuntingTutorialProgress.huntTrigger("PHANPYRE"));
        assertEquals("hunt.mountain_goat", HuntingTutorialProgress.huntTrigger("Mountain Goat"));
    }

    @Test
    void lassoAndNetCreaturesAreSeparated() {
        CreatureCatalog.load(new File("../configuration/skyblock/hunting"));
        List<CreatureSpec> all = CreatureCatalog.all();
        List<String> lasso = HuntingTutorialProgress.huntTriggers(all, CreatureSpec.Capture.LASSO);
        List<String> net = HuntingTutorialProgress.huntTriggers(all, CreatureSpec.Capture.FISHING_NET);
        assertTrue(lasso.contains("hunt.phanpyre"));
        assertTrue(net.contains("hunt.cod"));
        assertFalse(net.contains("hunt.phanpyre"));
        assertFalse(lasso.contains("hunt.cod"));
    }
}
