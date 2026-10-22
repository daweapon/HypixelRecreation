package dev.daweapon.type.skyblockgeneric.block.blocks;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.beacon.BeaconData;
import dev.daweapon.type.skyblockgeneric.beacon.BeaconEffectTicker;
import dev.daweapon.type.skyblockgeneric.beacon.BeaconFuel;
import dev.daweapon.type.skyblockgeneric.beacon.BeaconMenuView;
import dev.daweapon.type.skyblockgeneric.beacon.BeaconService;
import dev.daweapon.type.skyblockgeneric.block.SkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockBreakable;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockInteractable;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockPlaceable;
import dev.daweapon.type.skyblockgeneric.block.impl.CustomSkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import lombok.NonNull;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;

public class BlockBeacon implements CustomSkyBlockBlock, BlockPlaceable, BlockBreakable, BlockInteractable {
    @Override
    public @NonNull Block getDisplayMaterial() {
        return Block.BEACON;
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
        SkyBlockIsland island = SkyBlockIsland.forInstance(event.getInstance());
        ItemType type = new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType();
        int tier = BeaconService.tierOf(type);
        if (tier == 0 || island == null || island != player.getSkyBlockIsland()) {
            player.sendMessage("<c>You can only place a Beacon on your own island!");
            return;
        }
        if (BeaconService.get(island) != null) {
            player.sendMessage("<c>You can only place one Beacon on your island!");
            return;
        }
        event.getInstance().setBlock(event.getBlockPosition(), block.toBlock());
        BeaconService.save(island.getIslandID(), BeaconData.placed(tier, event.getBlockPosition().blockX(),
                event.getBlockPosition().blockY(), event.getBlockPosition().blockZ()));
        BeaconEffectTicker.ensureRunning(island);
        ItemStack held = player.getItemInMainHand();
        player.setItemInMainHand(held.withAmount(held.amount() - 1));
        player.sendMessage("<a>You placed a Beacon! Right-click it to open the Beacon menu.");
    }

    @Override
    public void onBreak(PlayerBlockBreakEvent event, SkyBlockBlock block) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        SkyBlockIsland island = SkyBlockIsland.forInstance(event.getInstance());
        if (island == null || island != player.getSkyBlockIsland()) {
            event.setCancelled(true);
            return;
        }
        BeaconData data = BeaconService.get(island);
        event.setResultBlock(Block.AIR);
        if (data == null) return;
        BeaconService.save(island.getIslandID(), null);
        ItemType item = BeaconService.beaconItem(data.tier());
        if (item != null) player.addAndUpdateItem(new SkyBlockItem(item));
        if (data.isPowered(System.currentTimeMillis())) player.sendMessage("<c>The Power Crystals inside the Beacon were lost!");
    }

    @Override
    public void onInteract(PlayerBlockInteractEvent event, SkyBlockBlock block) {
        if (event.getHand() != PlayerHand.MAIN) return;
        event.setBlockingItemUse(true);
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        SkyBlockIsland island = SkyBlockIsland.forInstance(event.getInstance());
        if (island == null || island != player.getSkyBlockIsland()) {
            player.sendMessage("<c>Only the island owner can use this Beacon!");
            return;
        }
        if (BeaconService.get(island) == null) return;
        if (BeaconFuel.insertHeld(player)) return;
        player.openView(new BeaconMenuView());
    }
}
