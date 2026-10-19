package dev.daweapon.type.bedwarslobby.events;

import net.minestom.server.event.player.PlayerChatEvent;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.chat.ChatChannelRouter;
import dev.daweapon.type.generic.collectibles.bedwars.prestige.BedWarsPrestigeRenderer;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.handlers.BedWarsDataHandler;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;

import java.util.List;

// Handles Bedwars lobby chat: routes messages by chat type and sanitizes input.
public class ActionPlayerChat implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER , requireDataLoaded = false)
    public void run(PlayerChatEvent event) {
        if (event.isCancelled()) return;
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        event.setCancelled(true);

        HypixelDataHandler dataHandler = player.getDataHandler();
        if (dataHandler == null) return;

        BedWarsDataHandler bedWarsDataHandler = BedWarsDataHandler.getUser(player);
        if (bedWarsDataHandler == null) {
            player.sendMessage("<c>An error occurred while processing your chat message. Please try again later.");
            return;
        }

        Rank rank = player.getRank();
        String finalMessage = ChatChannelRouter.route(player, event.getRawMessage()).orElse(null);
        if (finalMessage == null) return;

        List<HypixelPlayer> receivers = HypixelGenericLoader.getLoadedPlayers();

        Text levelPrefix = BedWarsPrestigeRenderer.renderBrackets(player).append(" ");

        receivers.forEach(onlinePlayer -> {
            if (rank.equals(Rank.DEFAULT))
                onlinePlayer.sendMessage("{}{}{}<7>: {}", levelPrefix, player.getRankPrefix(),
                        player.getUsername(), finalMessage);
            else
                onlinePlayer.sendMessage("{}{}{}<f>: {}", levelPrefix, player.getRankPrefix(),
                        player.getUsername(), finalMessage);
        });
    }
}
