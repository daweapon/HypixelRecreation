package dev.daweapon.type.skywarsgame.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skywarsgame.TypeSkywarsGameLoader;
import dev.daweapon.type.skywarsgame.game.SkywarsGame;
import dev.daweapon.type.skywarsgame.game.SkywarsGameStatus;
import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;

public class ActionChestOpen implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onBlockInteract(PlayerBlockInteractEvent event) {
        if (!(event.getPlayer() instanceof SkywarsPlayer player)) return;

        Block block = event.getBlock();
        if (block.compare(Block.CHEST) || block.compare(Block.TRAPPED_CHEST)) {
            SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
            if (game == null) return;

            if (game.getGameStatus() != SkywarsGameStatus.IN_PROGRESS) {
                event.setCancelled(true);
                return;
            }

            Pos chestPos = new Pos(event.getBlockPosition());
            if (game.getChestManager().isChestPosition(chestPos)) {
                player.addChestOpened();
                player.openInventory(game.getChestManager().getChestInventory(chestPos));
                event.setCancelled(true);
            }
        }
    }
}
