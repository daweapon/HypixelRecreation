package dev.daweapon.type.skyblockgeneric.block.blocks;

import lombok.NonNull;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.block.SkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockBreakable;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockPlaceable;
import dev.daweapon.type.skyblockgeneric.block.impl.CustomSkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.island.IslandUtilityBlocks;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class BlockIslandUtility implements CustomSkyBlockBlock, BlockPlaceable, BlockBreakable {
    private static final Tag<String> ITEM_TYPE_TAG = Tag.String("item_type");

    @Override
    public @NonNull Block getDisplayMaterial() {
        return Block.NOTE_BLOCK;
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
        ItemType type = new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType();
        event.setCancelled(true);
        if (type == null || !HypixelConst.isIslandServer()) return;

        Instance instance = event.getInstance();
        Point position = event.getBlockPosition();
        instance.setBlock(position, block.toBlock().withTag(ITEM_TYPE_TAG, type.name()));
        IslandUtilityBlocks.place(instance, position, type);
        ItemStack held = player.getItemInMainHand();
        player.setItemInMainHand(held.withAmount(held.amount() - 1));
    }

    @Override
    public void onBreak(PlayerBlockBreakEvent event, SkyBlockBlock block) {
        String type = event.getBlock().getTag(ITEM_TYPE_TAG);
        if (type == null) return;

        event.setResultBlock(Block.AIR);
        IslandUtilityBlocks.remove(event.getInstance(), event.getBlockPosition());
        ((SkyBlockPlayer) event.getPlayer()).addAndUpdateItem(new SkyBlockItem(ItemType.get(type)));
    }
}
