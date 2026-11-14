package dev.daweapon.type.skywarsgame.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skywars.SkywarsGameType;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skywarsgame.TypeSkywarsGameLoader;
import dev.daweapon.type.skywarsgame.game.SkywarsGame;
import dev.daweapon.type.skywarsgame.game.SkywarsGameStatus;
import dev.daweapon.type.skywarsgame.luckyblock.LuckyBlock;
import dev.daweapon.type.skywarsgame.luckyblock.LuckyBlockType;
import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;

public class ActionLuckyBlockPlace implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onBlockPlace(PlayerBlockPlaceEvent event) {
        if (!(event.getPlayer() instanceof SkywarsPlayer player)) return;

        ItemStack itemInHand = player.getItemInMainHand();
        if (itemInHand.material() != Material.PLAYER_HEAD) return;

        String luckyBlockTypeName = itemInHand.getTag(LuckyBlockType.LUCKY_BLOCK_TYPE_TAG);
        if (luckyBlockTypeName == null) return;

        LuckyBlockType type = LuckyBlockType.fromName(luckyBlockTypeName);
        if (type == null) return;

        event.setCancelled(true);

        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
        if (game == null) return;

        if (game.getGameType() != SkywarsGameType.SOLO_LUCKY_BLOCK) return;

        if (game.getGameStatus() != SkywarsGameStatus.IN_PROGRESS) return;

        LuckyBlock luckyBlockManager = game.getLuckyBlockManager();
        if (luckyBlockManager == null) return;

        Pos blockPos = new Pos(event.getBlockPosition());
        luckyBlockManager.placeLuckyBlock(blockPos, type);

        ItemStack newStack = itemInHand.withAmount(itemInHand.amount() - 1);
        player.setItemInMainHand(newStack);
    }
}
