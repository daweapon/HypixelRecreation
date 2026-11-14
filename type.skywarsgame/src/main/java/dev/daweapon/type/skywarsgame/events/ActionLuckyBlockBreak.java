package dev.daweapon.type.skywarsgame.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.block.Block;
import dev.daweapon.commons.skywars.SkywarsGameType;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skywarsgame.TypeSkywarsGameLoader;
import dev.daweapon.type.skywarsgame.game.SkywarsGame;
import dev.daweapon.type.skywarsgame.game.SkywarsGameStatus;
import dev.daweapon.type.skywarsgame.luckyblock.LuckyBlock;
import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;

public class ActionLuckyBlockBreak implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onBlockBreak(PlayerBlockBreakEvent event) {
        if (!(event.getPlayer() instanceof SkywarsPlayer player)) return;

        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
        if (game == null) return;

        if (game.getGameStatus() != SkywarsGameStatus.IN_PROGRESS) return;

        Pos blockPos = new Pos(event.getBlockPosition());

        if (game.getChestManager().isChestPosition(blockPos)) {
            event.setCancelled(true);
            return;
        }

        if (game.getGameType() != SkywarsGameType.SOLO_LUCKY_BLOCK) return;

        Block block = event.getBlock();
        LuckyBlock luckyBlockManager = game.getLuckyBlockManager();

        if (luckyBlockManager != null && luckyBlockManager.isLuckyBlockMaterial(block)) {
            event.setCancelled(true);
            luckyBlockManager.breakLuckyBlock(player, blockPos);
        }
    }
}
