package dev.daweapon.type.skyblockgeneric.staff;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.entity.FishingHook;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.FishingRodMetadataComponent;
import dev.daweapon.type.skyblockgeneric.item.components.InteractableComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RodCastTest {
    @Test
    void everyRodCastsAHookOnRightClick() throws Exception {
        MaxProfileTest.init();
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 48, Block.LAVA));
        for (int cx = -2; cx <= 2; cx++) for (int cz = -2; cz <= 2; cz++) instance.loadChunk(cx, cz).join();
        SkyBlockPlayer player = MaxProfileTest.player();
        player.setInstance(instance, new Pos(0, 51, 0, 0, 30)).join();

        for (ItemType type : List.of(ItemType.FISHING_ROD, ItemType.MAGMA_ROD, ItemType.INFERNO_ROD, ItemType.HELLFIRE_ROD, ItemType.ROD_OF_THE_SEA)) {
            SkyBlockItem rod = new SkyBlockItem(type);
            assertTrue(rod.hasComponent(FishingRodMetadataComponent.class), type.name());
            assertTrue(rod.hasComponent(InteractableComponent.class), type + " has no right-click handler");
            player.setItemInMainHand(rod.getItemStack());
            rod.getComponent(InteractableComponent.class).onRightClick(player, rod);
            assertTrue(FishingHook.getFishingHookForOwner(player) != null, type + " cast no hook");
            FishingHook.getFishingHookForOwner(player).remove();
        }
    }
}
