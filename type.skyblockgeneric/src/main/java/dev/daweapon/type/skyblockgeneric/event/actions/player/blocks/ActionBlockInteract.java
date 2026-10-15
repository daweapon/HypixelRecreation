package dev.daweapon.type.skyblockgeneric.event.actions.player.blocks;

import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.item.ItemStack;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.block.SkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockInteractable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.BlockDataToolComponent;
import dev.daweapon.type.skyblockgeneric.item.components.RegionSelectorComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Handles block interactions for region-selector and block-data debug tools, and custom interactable blocks
public class ActionBlockInteract implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onInteract(PlayerBlockInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        ItemStack stack = event.getPlayer().getItemInMainHand();
        SkyBlockItem item = new SkyBlockItem(stack);

        if (item.hasComponent(RegionSelectorComponent.class)) {
            RegionSelectorComponent.SelectedRegion region = RegionSelectorComponent.getPlayerRegionSelection().get(player);
            if (region == null) {
                region = new RegionSelectorComponent.SelectedRegion(null, null);
            }
            region.setPos2(event.getBlockPosition());
            RegionSelectorComponent.getPlayerRegionSelection().put(player, region);
            player.sendMessage("<a>Position 2 set to <e>{}<a>.", event.getBlockPosition());
            event.setCancelled(true);
            return;
        }

        if (item.hasComponent(BlockDataToolComponent.class)) {
            player.sendMessage("<a>Block data for <e>{}<a>:", event.getBlockPosition());
            player.sendMessage("<e>- Block ID: <f>{}", event.getBlock().material());
            player.sendMessage("<e>- Block State: <f>{}", event.getBlock().state());
            event.setCancelled(true);
            return;
        }

        if (!SkyBlockBlock.isSkyBlockBlock(event.getBlock())) return;

        SkyBlockBlock block = new SkyBlockBlock(event.getBlock());

        if (block.getGenericInstance() instanceof BlockInteractable interactable) {
            interactable.onInteract(event, block);
        }
    }
}
