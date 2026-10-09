package net.swofty.type.kuudra.fight;

import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.type.kuudra.fight.world.BallistaSite;
import net.swofty.type.kuudra.fight.world.KuudraItems;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KuudraItemsTest {

    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
    }

    @Test
    void skyBlocksItemUpdaterLeavesRunItemsAlone() {
        for (KuudraItems.Kind kind : KuudraItems.Kind.values()) {
            ItemStack item = KuudraItems.create(kind);
            ItemStack updated = PlayerItemUpdater.playerUpdateFull(null, item, true).getValue().build();
            assertEquals(kind, KuudraItems.kindOf(updated), kind.name());
            assertEquals(item.material(), updated.material(), kind.name());
            assertEquals(item.get(DataComponents.ITEM_MODEL), updated.get(DataComponents.ITEM_MODEL), kind.name());
            assertEquals(item, updated, kind.name());
        }
    }

    @Test
    void theBallistaTurnsItsMuzzleWithTheRider() {
        Pos base = new Pos(0, 79, 0);
        Vec muzzle = new Vec(0, 2.8, -2.5);
        Pos north = BallistaSite.placed(base, 180, muzzle);
        assertEquals(-2.5, north.z(), 1e-9);
        assertEquals(0, north.x(), 1e-9);
        Pos east = BallistaSite.placed(base, -90, muzzle);
        assertEquals(2.5, east.x(), 1e-9);
        assertEquals(0, east.z(), 1e-9);
        Pos south = BallistaSite.placed(base, 0, muzzle);
        assertEquals(2.5, south.z(), 1e-9);
        Pos west = BallistaSite.placed(base, 90, muzzle);
        assertEquals(-2.5, west.x(), 1e-9);
    }
}
