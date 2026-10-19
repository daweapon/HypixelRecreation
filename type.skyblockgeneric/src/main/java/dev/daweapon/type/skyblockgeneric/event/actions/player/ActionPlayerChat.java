package dev.daweapon.type.skyblockgeneric.event.actions.player;

import net.minestom.server.event.player.PlayerChatEvent;
import dev.daweapon.commons.ServerType;
import net.kyori.adventure.text.format.NamedTextColor;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.chat.ChatChannelRouter;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// Intercepts vanilla chat and routes it to the player's selected SkyBlock chat channel (all/party/staff)
public class ActionPlayerChat implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerChatEvent event) {
        if (event.isCancelled()) return;
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        event.setCancelled(true);

        SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();
        if (dataHandler == null) return;

        Rank rank = player.getRank();
        String finalMessage = ChatChannelRouter.route(player, event.getRawMessage()).orElse(null);
        if (finalMessage == null) return;

        List<SkyBlockPlayer> receivers = SkyBlockGenericLoader.getLoadedPlayers();

        receivers.removeIf(receiver -> {
            return HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_ISLAND &&
                    !receiver.getInstance().equals(player.getInstance());
        });

        receivers.forEach(onlinePlayer -> {
            boolean showLevel = onlinePlayer.getToggles().get(DatapointToggles.Toggles.ToggleType.SKYBLOCK_LEVELS_IN_CHAT);

            Text namePrefix = showLevel
                    ? player.getFullDisplayName()
                    : Text.of("{}", player.getRank().displayName(player.getRankColor(), player.isMvpPlusPlusAqua() ? NamedTextColor.AQUA : NamedTextColor.GOLD, player.getUsername()));

            if (rank.equals(Rank.DEFAULT))
                onlinePlayer.sendMessage(namePrefix.append("<7>: ").append(Text.literal(finalMessage)));
            else
                onlinePlayer.sendMessage(namePrefix.append("<f>: ").append(Text.literal(finalMessage)));
        });
    }
}
