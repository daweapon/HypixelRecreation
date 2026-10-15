package net.swofty.type.skyblockgeneric.block.blocks;

import lombok.NonNull;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.block.SkyBlockBlock;
import net.swofty.type.skyblockgeneric.block.impl.BlockBreakable;
import net.swofty.type.skyblockgeneric.block.impl.BlockInteractable;
import net.swofty.type.skyblockgeneric.block.impl.BlockPlaceable;
import net.swofty.type.skyblockgeneric.block.impl.CustomSkyBlockBlock;
import net.swofty.type.skyblockgeneric.gui.inventories.experiments.GUIExperimentationTable;
import net.swofty.type.skyblockgeneric.island.IslandUtilityBlocks;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.experiments.ExperimentationTableDisplay;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class BlockExperimentationTable implements CustomSkyBlockBlock, BlockPlaceable, BlockBreakable, BlockInteractable {
    @Override
    public @NonNull Block getDisplayMaterial() {
        return Block.BARRIER;
    }

    @Override
    public @NonNull Boolean shouldPlace(SkyBlockPlayer player) {
        return HypixelConst.isIslandServer();
    }

    @Override
    public @NonNull Boolean shouldDestroy(SkyBlockPlayer player) {
        return HypixelConst.isIslandServer();
    }

    @Override
    public void onPlace(PlayerBlockPlaceEvent event, SkyBlockBlock block) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        event.setCancelled(true);
        SkyBlockItem item = new SkyBlockItem(player.getItemInMainHand());
        if (item.getAttributeHandler().getPotentialType() != ItemType.EXPERIMENTATION_TABLE) return;
        if (IslandUtilityBlocks.has(event.getInstance(), ItemType.EXPERIMENTATION_TABLE)) {
            player.sendMessage("<c>You can only place one Experimentation Table on your island!");
            return;
        }
        event.getInstance().setBlock(event.getBlockPosition(), block.toBlock());
        ExperimentationTableDisplay.spawn(event.getInstance(), event.getBlockPosition());
        IslandUtilityBlocks.place(event.getInstance(), event.getBlockPosition(), ItemType.EXPERIMENTATION_TABLE);
        ItemStack held = player.getItemInMainHand();
        player.setItemInMainHand(held.withAmount(held.amount() - 1));
    }

    @Override
    public void onBreak(PlayerBlockBreakEvent event, SkyBlockBlock block) {
        pickUp(event.getInstance(), event.getBlockPosition(), (SkyBlockPlayer) event.getPlayer());
        event.setResultBlock(Block.AIR);
    }

    @Override
    public void onInteract(PlayerBlockInteractEvent event, SkyBlockBlock block) {
        event.setBlockingItemUse(true);
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (player.isSneaking()) {
            pickUp(event.getInstance(), event.getBlockPosition(), player);
            event.getInstance().setBlock(event.getBlockPosition(), Block.AIR);
            return;
        }
        player.openView(new GUIExperimentationTable());
    }

    private void pickUp(net.minestom.server.instance.Instance instance, net.minestom.server.coordinate.Point position, SkyBlockPlayer player) {
        IslandUtilityBlocks.remove(instance, position);
        ExperimentationTableDisplay.remove(instance, position);
        player.addAndUpdateItem(new SkyBlockItem(ItemType.EXPERIMENTATION_TABLE));
    }
}
