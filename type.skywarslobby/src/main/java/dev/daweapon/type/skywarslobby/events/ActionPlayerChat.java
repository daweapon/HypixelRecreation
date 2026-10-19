package dev.daweapon.type.skywarslobby.events;

import net.minestom.server.event.player.PlayerChatEvent;
import dev.daweapon.commons.skywars.SkyWarsLevelColor;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.chat.ChatChannelRouter;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointBoolean;
import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.generic.data.handlers.SkywarsDataHandler;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skywarslobby.level.SkywarsLevelRegistry;

import java.util.List;

public class ActionPlayerChat implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerChatEvent event) {
        if (event.isCancelled()) return;
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        event.setCancelled(true);

        HypixelDataHandler dataHandler = player.getDataHandler();
        if (dataHandler == null) return;

        SkywarsDataHandler skywarsDataHandler = SkywarsDataHandler.getUser(player);
        if (skywarsDataHandler == null) {
            player.sendMessage("<c>An error occurred while processing your chat message. Please try again later.");
            return;
        }

        Rank rank = player.getRank();
        String finalMessage = ChatChannelRouter.route(player, event.getRawMessage()).orElse(null);
        if (finalMessage == null) return;

        List<HypixelPlayer> receivers = HypixelGenericLoader.getLoadedPlayers();

        boolean hideLevel = skywarsDataHandler.get(SkywarsDataHandler.Data.HIDE_LEVEL, DatapointBoolean.class).getValue();
        Text levelPrefix;
        if (!hideLevel) {
            int level = SkywarsLevelRegistry.calculateLevel(
                    skywarsDataHandler.get(SkywarsDataHandler.Data.EXPERIENCE, DatapointLong.class).getValue()
            );
            levelPrefix = Text.of("{} ", SkyWarsLevelColor.getLevelDisplay(level));
        } else {
            levelPrefix = Text.empty();
        }

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
