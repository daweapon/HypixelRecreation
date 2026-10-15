package dev.daweapon.type.skyblockgeneric.block.blocks;

import lombok.NonNull;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.block.SkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockBreakable;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockInteractable;
import dev.daweapon.type.skyblockgeneric.block.impl.BlockPlaceable;
import dev.daweapon.type.skyblockgeneric.block.impl.CustomSkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.glacite.ColdService;
import dev.daweapon.type.skyblockgeneric.glacite.GlaciteZones;
import dev.daweapon.type.skyblockgeneric.glacite.PortableCampfire;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;

public class BlockPortableCampfire implements CustomSkyBlockBlock, BlockPlaceable, BlockInteractable, BlockBreakable {
    private static final Tag<Long> PLACED_AT = Tag.Long("campfire_placed_at");

    @Override
    public @NonNull Block getDisplayMaterial() {
        return Block.CAMPFIRE;
    }

    @Override
    public @NonNull Boolean shouldPlace(SkyBlockPlayer player) {
        return inColdZone(player);
    }

    @Override
    public @NonNull Boolean shouldDestroy(SkyBlockPlayer player) {
        return false;
    }

    @Override
    public void onPlace(PlayerBlockPlaceEvent event, SkyBlockBlock block) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        event.setCancelled(true);
        if (!inColdZone(player)) {
            player.sendMessage("<c>You can only place a Portable Campfire in the Glacite Tunnels!");
            return;
        }
        long now = System.currentTimeMillis();
        long wait = PortableCampfire.remainingCooldown(player.getUuid(), now);
        if (wait > 0) {
            player.sendMessage("<c>You can place another campfire in {}s!", (wait + 999) / 1000);
            return;
        }

        Instance instance = event.getInstance();
        Point position = event.getBlockPosition();
        instance.setBlock(position, block.toBlock().withTag(PLACED_AT, now));
        PortableCampfire.markPlaced(player.getUuid(), now);
        ItemStack held = player.getItemInMainHand();
        player.setItemInMainHand(held.withAmount(held.amount() - 1));
        MinecraftServer.getSchedulerManager().buildTask(() -> removeIfPlaced(instance, position, now))
                .delay(TaskSchedule.duration(Duration.ofMillis(PortableCampfire.LIFETIME_MILLIS)))
                .schedule();
    }

    @Override
    public void onInteract(PlayerBlockInteractEvent event, SkyBlockBlock block) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        event.setBlockingItemUse(true);
        Long placedAt = event.getBlock().getTag(PLACED_AT);
        if (placedAt == null) return;
        if (PortableCampfire.expired(placedAt, System.currentTimeMillis())) {
            event.getInstance().setBlock(event.getBlockPosition(), Block.AIR);
            return;
        }
        ColdService.addCold(player, PortableCampfire.COLD_REMOVED);
        event.getInstance().setBlock(event.getBlockPosition(), Block.AIR);
        player.sendMessage("<a>The campfire warms you up! <7>Removed <a>{} <b>❄ Cold<7>.", (int) PortableCampfire.COLD_REMOVED);
    }

    @Override
    public void onBreak(PlayerBlockBreakEvent event, SkyBlockBlock block) {
        event.setCancelled(true);
    }

    private static void removeIfPlaced(Instance instance, Point position, long placedAt) {
        Block current = instance.getBlock(position);
        Long stamp = current.getTag(PLACED_AT);
        if (stamp != null && stamp == placedAt) instance.setBlock(position, Block.AIR);
    }

    private static boolean inColdZone(SkyBlockPlayer player) {
        SkyBlockRegion region = player.getRegion();
        return region != null && GlaciteZones.isColdZone(region.getType());
    }
}
