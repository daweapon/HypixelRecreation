package dev.daweapon.type.skyblockgeneric.block.blocks;

import lombok.NonNull;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.block.SkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockBreakable;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockInteractable;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockPlaceable;
import dev.daweapon.type.skyblockgeneric.block.impl.CustomSkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.island.pad.TeleportPad;
import dev.daweapon.type.skyblockgeneric.island.pad.TeleportPadSet;
import dev.daweapon.type.skyblockgeneric.island.pad.TeleportPadView;
import dev.daweapon.type.skyblockgeneric.island.pad.TeleportPads;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;

public class BlockTeleportPad implements CustomSkyBlockBlock, BlockPlaceable, BlockBreakable, BlockInteractable {
    @Override
    public @NonNull Block getDisplayMaterial() {
        return Block.END_PORTAL_FRAME;
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
        if (new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType() != ItemType.TELEPORT_PAD) return;

        SkyBlockIsland island = SkyBlockIsland.forInstance(event.getInstance());
        if (!HypixelConst.isIslandServer() || island == null || !TeleportPads.isMember(player, island)) {
            player.sendMessage("<c>You can only place Teleport Pads on your own island!");
            return;
        }
        TeleportPad pad = TeleportPads.of(island).add(
                event.getBlockPosition().blockX(), event.getBlockPosition().blockY(), event.getBlockPosition().blockZ());
        if (pad == null) {
            player.sendMessage("<c>You can only place up to {} Teleport Pads on your island!", TeleportPadSet.MAX_PADS);
            return;
        }
        event.getInstance().setBlock(event.getBlockPosition(), block.toBlock());
        TeleportPads.save(island);
        ItemStack held = player.getItemInMainHand();
        player.setItemInMainHand(held.withAmount(held.amount() - 1));
        player.sendMessage("<a>You placed a Teleport Pad! <7>({}/{})", TeleportPads.of(island).size(), TeleportPadSet.MAX_PADS);
    }

    @Override
    public void onBreak(PlayerBlockBreakEvent event, SkyBlockBlock block) {
        event.setCancelled(true);
        ((SkyBlockPlayer) event.getPlayer()).sendMessage("<c>Open the Teleport Pad's menu to pick it up!");
    }

    @Override
    public void onInteract(PlayerBlockInteractEvent event, SkyBlockBlock block) {
        if (event.getHand() != PlayerHand.MAIN) return;
        event.setBlockingItemUse(true);
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        SkyBlockIsland island = SkyBlockIsland.forInstance(event.getInstance());
        if (island == null) return;
        TeleportPad pad = TeleportPads.of(island).at(
                event.getBlockPosition().blockX(), event.getBlockPosition().blockY(), event.getBlockPosition().blockZ());
        if (pad == null) return;
        if (!TeleportPads.isMember(player, island)) {
            player.sendMessage("<c>Only members of this island can change its Teleport Pads!");
            return;
        }
        player.openView(new TeleportPadView(island, pad));
    }
}
