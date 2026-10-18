package dev.daweapon.type.skyblockgeneric.block.blocks;

import lombok.NonNull;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.achievement.SkyBlockItemAchievements;
import dev.daweapon.type.skyblockgeneric.block.SkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockBreakable;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockInteractable;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockPlaceable;
import dev.daweapon.type.skyblockgeneric.block.impl.CustomSkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.gui.inventories.GUIMinionStorage;
import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.minion.storage.MinionStorage;
import dev.daweapon.type.skyblockgeneric.minion.storage.MinionStorageChest;
import dev.daweapon.type.skyblockgeneric.minion.storage.MinionStorageLinks;
import dev.daweapon.type.skyblockgeneric.minion.storage.MinionStorageTier;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Optional;

public class BlockMinionStorage implements CustomSkyBlockBlock, BlockPlaceable, BlockBreakable, BlockInteractable {
    private static final Tag<String> ITEM_TYPE_TAG = Tag.String("item_type");
    private static final Tag<String> BLOCK_TYPE_TAG = Tag.String("block_type");
    private static final String BLOCK_TYPE = "MINION_STORAGE";

    @Override
    public @NonNull Block getDisplayMaterial() {
        return Block.CHEST;
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
        if (!HypixelConst.isIslandServer()) return;

        ItemType type = new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType();
        Optional<MinionStorageTier> tier = MinionStorageTier.fromItem(type);
        if (tier.isEmpty()) return;

        Instance instance = event.getInstance();
        Point position = event.getBlockPosition();

        String refusal = adjacentChestRefusal(instance, position);
        if (refusal != null) {
            player.sendMessage(refusal);
            return;
        }

        IslandMinionData minions = player.getSkyBlockIsland().getMinionData();
        if (!MinionStorage.hasAdjacentMinion(minions, position)) {
            player.sendMessage("<c>You need to place this next to a minion!");
            return;
        }
        Optional<IslandMinionData.IslandMinion> linked = MinionStorage.findLinkable(minions, position);
        if (linked.isEmpty()) {
            player.sendMessage("<c>This minion already has a chest!");
            return;
        }

        IslandMinionData.IslandMinion minion = linked.get();
        Block placed = block.toBlock()
                .withProperty("facing", MinionStorageLinks.facingToward(position, minion.getPosition()))
                .withTag(ITEM_TYPE_TAG, type.name());
        instance.setBlock(position, placed);
        MinionStorage.attach(minion, new MinionStorageChest(tier.get(),
                new net.minestom.server.coordinate.Vec(position.blockX(), position.blockY(), position.blockZ()),
                new java.util.ArrayList<>()));
        ItemStack held = player.getItemInMainHand();
        player.setItemInMainHand(held.withAmount(held.amount() - 1));
        SkyBlockItemAchievements.onStoragePlaced(player, type);
    }

    @Override
    public void onBreak(PlayerBlockBreakEvent event, SkyBlockBlock block) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        String type = event.getBlock().getTag(ITEM_TYPE_TAG);
        if (type == null) return;

        event.setResultBlock(Block.AIR);
        Point position = event.getBlockPosition();
        MinionStorage.findByChest(player.getSkyBlockIsland().getMinionData(), position).ifPresent(minion -> {
            List<ItemQuantifiable> contents = MinionStorage.drain(minion);
            MinionStorage.detach(minion);
            contents.forEach(entry -> player.addAndUpdateItem(entry.toSkyBlockItem()));
        });
        player.addAndUpdateItem(new SkyBlockItem(ItemType.valueOf(type)));
    }

    @Override
    public void onInteract(PlayerBlockInteractEvent event, SkyBlockBlock block) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!HypixelConst.isIslandServer()) return;
        event.setBlockingItemUse(true);
        MinionStorage.findByChest(player.getSkyBlockIsland().getMinionData(), event.getBlockPosition())
                .ifPresent(minion -> new GUIMinionStorage(minion).open(player));
    }

    private static String adjacentChestRefusal(Instance instance, Point position) {
        boolean normal = false;
        boolean trapped = false;
        boolean storage = false;
        for (Point neighbour : MinionStorageLinks.linkOrder(position)) {
            Block block = instance.getBlock(neighbour);
            if (BLOCK_TYPE.equals(block.getTag(BLOCK_TYPE_TAG))) storage = true;
            else if (block.compare(Block.CHEST)) normal = true;
            else if (block.compare(Block.TRAPPED_CHEST)) trapped = true;
        }
        if (normal) return "<c>You can't place minion storage next to another chest!";
        if (trapped) return "<c>You can't place this next to other chests!";
        if (storage) return "<c>You can't place a chest here!";
        return null;
    }
}
