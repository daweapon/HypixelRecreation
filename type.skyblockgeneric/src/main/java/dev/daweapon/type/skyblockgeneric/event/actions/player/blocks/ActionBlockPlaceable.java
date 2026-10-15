package dev.daweapon.type.skyblockgeneric.event.actions.player.blocks;

import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.item.ItemStack;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.block.SkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockPlaceable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PlaceableComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Delegates block placement to a placeable SkyBlock item's custom BlockPlaceable behavior
public class ActionBlockPlaceable implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onPlace(PlayerBlockPlaceEvent event) {
        ItemStack itemStack = event.getPlayer().getItemInMainHand();
        SkyBlockItem item = new SkyBlockItem(itemStack);
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (item.toConfigurableItem() == null) return;

        if (item.hasComponent(PlaceableComponent.class)) {
            PlaceableComponent placeable = item.getComponent(PlaceableComponent.class);
            if (placeable.getBlockType() == null) return;
            SkyBlockBlock skyBlockBlock = new SkyBlockBlock(placeable.getBlockType());
            if (skyBlockBlock.getGenericInstance() instanceof BlockPlaceable blockPlaceable) {
                blockPlaceable.onPlace(event, skyBlockBlock);
            }
        }
    }
}
