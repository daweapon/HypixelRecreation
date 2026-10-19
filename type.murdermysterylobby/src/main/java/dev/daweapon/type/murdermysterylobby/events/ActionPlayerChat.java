package dev.daweapon.type.murdermysterylobby.events;

import net.minestom.server.event.player.PlayerChatEvent;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.chat.ChatChannelRouter;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

import java.util.List;

public class ActionPlayerChat implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerChatEvent event) {
        if (event.isCancelled()) return;
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        event.setCancelled(true);

        HypixelDataHandler dataHandler = player.getDataHandler();
        if (dataHandler == null) return;

        Rank rank = player.getRank();
        String finalMessage = ChatChannelRouter.route(player, event.getRawMessage()).orElse(null);
        if (finalMessage == null) return;

        List<HypixelPlayer> receivers = HypixelGenericLoader.getLoadedPlayers();

        receivers.forEach(onlinePlayer -> {
            if (rank.equals(Rank.DEFAULT))
                onlinePlayer.sendMessage("{}{}<7>: {}", player.getRankPrefix(),
                        player.getUsername(), finalMessage);
            else
                onlinePlayer.sendMessage("{}{}<f>: {}", player.getRankPrefix(),
                        player.getUsername(), finalMessage);
        });
    }
}
