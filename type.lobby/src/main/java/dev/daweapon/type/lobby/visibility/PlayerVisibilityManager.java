package dev.daweapon.type.lobby.visibility;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.entity.hologram.HologramEntity;
import dev.daweapon.type.generic.entity.npc.impl.NPCEntityImpl;
import dev.daweapon.type.generic.user.HypixelPlayer;

public class PlayerVisibilityManager {

    public static void setupViewerRule(HypixelPlayer player, boolean showPlayers) {
        player.updateViewerRule((entity) -> {
            if (entity instanceof NPCEntityImpl) return true;
            if (entity instanceof HologramEntity) return true;
            if (entity.getEntityType() == EntityType.ITEM_FRAME) return true;
            return (entity instanceof Player) == showPlayers;
        });
    }

    public static void setupViewerRuleFromToggle(HypixelPlayer player) {
        boolean showPlayers = player.getToggles().get(DatapointToggles.Toggles.ToggleType.LOBBY_SHOW_PLAYERS);
        setupViewerRule(player, showPlayers);
    }

    public static boolean toggleVisibility(HypixelPlayer player) {
        boolean newState = player.getToggles().inverse(DatapointToggles.Toggles.ToggleType.LOBBY_SHOW_PLAYERS);
        setupViewerRule(player, newState);
        return newState;
    }
}
