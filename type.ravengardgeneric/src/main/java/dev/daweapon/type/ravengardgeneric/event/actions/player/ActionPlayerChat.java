package dev.daweapon.type.ravengardgeneric.event.actions.player;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.NamedTextColor;
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
import dev.daweapon.type.ravengardgeneric.classes.RavengardClass;
import dev.daweapon.type.ravengardgeneric.user.RavengardPlayer;

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

        Text line = format(player, rank, finalMessage);
        List<HypixelPlayer> receivers = HypixelGenericLoader.getLoadedPlayers();
        receivers.forEach(onlinePlayer -> onlinePlayer.sendMessage(line));
    }

    private static Text format(HypixelPlayer player, Rank rank, String message) {
        String icon = "";
        int level = 1;
        if (player instanceof RavengardPlayer ravengardPlayer) {
            RavengardClass playerClass = ravengardPlayer.getRavengardClass();
            if (playerClass != null) {
                icon = String.valueOf(playerClass.getIcon());
            }
            level = ravengardPlayer.getRavengardLevel();
        }

        TextColor messageColor = rank.equals(Rank.DEFAULT) ? NamedTextColor.GRAY : NamedTextColor.WHITE;

        return Text.of("{} ", icon)
                .append(player.getFullDisplayName())
                .append("<color:{}> <6><l>{}</l><color:{}>: {}", messageColor, level, messageColor, message);
    }
}
