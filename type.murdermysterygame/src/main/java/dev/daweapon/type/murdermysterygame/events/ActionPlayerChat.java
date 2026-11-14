package dev.daweapon.type.murdermysterygame.events;

import net.minestom.server.event.player.PlayerChatEvent;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.chat.StaffChat;
import dev.daweapon.type.generic.data.datapoints.DatapointChatType;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.murdermysterygame.TypeMurderMysteryGameLoader;
import dev.daweapon.type.murdermysterygame.game.Game;
import dev.daweapon.type.murdermysterygame.game.GameStatus;
import dev.daweapon.type.murdermysterygame.user.MurderMysteryPlayer;

public class ActionPlayerChat implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerChatEvent event) {
        if (event.isCancelled()) return;
        MurderMysteryPlayer player = (MurderMysteryPlayer) event.getPlayer();
        event.setCancelled(true);

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game == null) return;

        String message = event.getRawMessage();
        Rank rank = player.getRank();

        // Sanitize message
        if (!rank.isStaff()) {
            message = message.replaceAll("[^\\x00-\\x7F]", "");
        }

        String finalMessage = message;

        DatapointChatType.Chats chatType = player.getChatType().currentChatType;
        if (chatType == DatapointChatType.Chats.STAFF) {
            if (!rank.isStaff()) {
                player.sendMessage("<c>Unknown chat type.");
                player.getChatType().switchTo(DatapointChatType.Chats.ALL);
                return;
            }
            StaffChat.sendMessage(player, finalMessage);
            return;
        }

        // Dead players can only talk to other dead players
        if (player.isEliminated() && game.getGameStatus() == GameStatus.IN_PROGRESS) {
            for (MurderMysteryPlayer gamePlayer : game.getPlayers()) {
                if (gamePlayer.isEliminated()) {
                    gamePlayer.sendMessage("<7>[DEAD] <f>{}: <7>{}", player.getUsername(), finalMessage);
                }
            }
            return;
        }

        // Normal chat
        for (MurderMysteryPlayer gamePlayer : game.getPlayers()) {
            gamePlayer.sendMessage("{}<f>{}: {}",
                    player.getRankPrefix(),
                    player.getUsername(),
                    finalMessage);
        }
    }
}
