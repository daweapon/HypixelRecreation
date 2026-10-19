package dev.daweapon.type.skywarsgame.events;

import net.minestom.server.event.player.PlayerChatEvent;
import dev.daweapon.commons.skywars.SkyWarsLevelColor;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.chat.ChatChannelRouter;
import dev.daweapon.type.generic.data.datapoints.DatapointBoolean;
import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.generic.data.handlers.SkywarsDataHandler;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skywarsgame.TypeSkywarsGameLoader;
import dev.daweapon.type.skywarsgame.game.SkywarsGame;
import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;
import dev.daweapon.type.skywarslobby.level.SkywarsLevelRegistry;

public class ActionPlayerChat implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerChatEvent event) {
        if (event.isCancelled()) return;
        final SkywarsPlayer player = (SkywarsPlayer) event.getPlayer();
        event.setCancelled(true);

        if (player.getDataHandler() == null) return;

        SkywarsDataHandler skywarsDataHandler = SkywarsDataHandler.getUser(player);
        if (skywarsDataHandler == null) {
            player.sendMessage("<c>An error occurred while processing your chat message. Please try again later.");
            return;
        }

        Rank rank = player.getRank();
        String finalMessage = ChatChannelRouter.route(player, event.getRawMessage()).orElse(null);
        if (finalMessage == null) return;

        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
        if (game == null) return;

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

        Text speaker = levelPrefix.append(Text.of("{}{}", player.getRankPrefix(),
                player.getUsername()));
        Text line = Text.of(rank.equals(Rank.DEFAULT) ? "{}<7>: {}" : "{}<f>: {}",
                speaker, finalMessage);

        for (SkywarsPlayer gamePlayer : game.getPlayers()) {
            gamePlayer.sendMessage(line);
        }
    }
}
