package net.swofty.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket;
import net.minestom.server.network.packet.server.play.BlockBreakAnimationPacket;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.PlayerDamageSkyBlockBlockEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.region.mining.BreakingTask;
import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import net.swofty.type.skyblockgeneric.region.mining.handler.SkyBlockMiningHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Handles custom SkyBlock block breaking: validates tool eligibility and drives the break animation/task
public class ActionPlayerDamageBlock implements HypixelEventClass {
    public static final Map<UUID, BreakingTask> CLICKING = new HashMap<>();

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerDamageSkyBlockBlockEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        Instance instance = player.getInstance();
        if (instance == null || !instance.isChunkLoaded(event.getBlockPosition())) {
            BreakingTask prev = CLICKING.remove(player.getUuid());
            if (prev != null) prev.cancel();
            return;
        }
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(event.getBlockPosition());

        Material currentMaterial = Material.fromKey(instance.getBlock(event.getBlockPosition()).key());
        boolean statusNotStarted = event.getStatus() != ClientPlayerActionPacket.Status.STARTED_DIGGING;
        boolean regionNull = region == null;
        boolean handlerNull = !regionNull && region.getType().getMiningHandler() == null;
        boolean materialNull = currentMaterial == null;
        boolean notMineableHere = !regionNull && !handlerNull && !materialNull
                && !region.getType().getMiningHandler().getMineableBlocks(instance, event.getBlockPosition()).contains(currentMaterial);
        boolean isCreative = player.getGameMode().equals(GameMode.CREATIVE);

        if (statusNotStarted || regionNull || handlerNull || materialNull || notMineableHere || isCreative) {
            // Vanilla clients resend CANCELLED_DIGGING or FINISHED_DIGGING for the same block while
            // the player is still continuously mining it whenever the client's own break-time
            // prediction elapses before the server actually finishes the break (FINISHED_DIGGING is
            // sent once the client's own crack animation reaches 100%, even if the server isn't done
            // yet). The server's own BreakingTask counter is the authority on when a block actually
            // breaks, so ignore these same-position signals and let the existing task keep its
            // progress instead of wiping it - otherwise a block whose configured break time doesn't
            // exactly match vanilla's client-side prediction can never be broken at all, since every
            // attempt gets killed just before (or exactly at) completion.
            boolean clientSideKeepAlive = event.getStatus() == ClientPlayerActionPacket.Status.CANCELLED_DIGGING
                    || event.getStatus() == ClientPlayerActionPacket.Status.FINISHED_DIGGING;
            if (clientSideKeepAlive && !regionNull && !handlerNull && !materialNull && !notMineableHere && !isCreative) {
                BreakingTask existing = CLICKING.get(player.getUuid());
                if (existing != null && samePosition(existing.getPosition(), event.getBlockPosition())) {
                    return;
                }
            }

            // Cancel the task if the player is no longer breaking the block or changed block
            BreakingTask prev = CLICKING.get(player.getUuid());
            if (prev != null)
                prev.cancel();
            CLICKING.remove(player.getUuid());

            MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                BlockBreakAnimationPacket breakAnim = new BlockBreakAnimationPacket(player.getEntityId(),
                        event.getBlockPosition(),
                        (byte) -1);
                player.sendPacket(breakAnim);
            }, TaskSchedule.tick(2), TaskSchedule.stop());
            return;
        }

        if (CLICKING.containsKey(player.getUuid())) {
            return;
        }

        // Check if the player's tool can break this block using the handler system
        SkyBlockItem item = new SkyBlockItem(player.getItemInMainHand());
        MineableBlock mineableBlock = MineableBlock.get(instance.getBlock(event.getBlockPosition()));
        if (mineableBlock != null) {
            SkyBlockMiningHandler handler = mineableBlock.getMiningHandler();
            // If block doesn't break instantly and tool can't break it, return
            if (!handler.breaksInstantly() && !handler.canToolBreak(item)) {
                return;
            }
        }

        BreakingTask task = new BreakingTask(
                player,
                new BreakingTask.PositionedBlock(
                        instance.getBlock(event.getBlockPosition()),
                        event.getBlockPosition().asPos()),
                item,
                event.getSequence());
        MinecraftServer.getSchedulerManager().submitTask(task::run);
        CLICKING.put(player.getUuid(), task);
    }

    private boolean samePosition(Point a, Point b) {
        return a.blockX() == b.blockX() && a.blockY() == b.blockY() && a.blockZ() == b.blockZ();
    }
}
