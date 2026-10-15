package dev.daweapon.type.lobby;

import net.minestom.server.MinecraftServer;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.HypixelTypeLoader;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.lobby.item.LobbyItem;
import dev.daweapon.type.lobby.item.LobbyItemHandler;
import dev.daweapon.type.lobby.launchpad.LaunchPad;
import dev.daweapon.type.lobby.parkour.LobbyParkourManager;
import dev.daweapon.type.lobby.parkour.Parkour;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;

// Loader contract implemented by each lobby variant, defining its items, launch pads, and parkour setup.
public interface LobbyTypeLoader extends HypixelTypeLoader {

    /**
     * Returns the item handler for this lobby.
     * The handler manages all lobby items and dispatches events.
     */
    LobbyItemHandler getItemHandler();

    /**
     * Returns launch pad definitions for this lobby.
     * Empty list if no launch pads.
     */
    List<LaunchPad> getLaunchPads();

    /**
     * Configurable slot mapping - each lobby defines which items go where.
     * Returns Map<slot, LobbyItem> - only items in map are given to players.
     */
    Map<Integer, LobbyItem> getHotbarItems();

    default Parkour getParkour() {
        return null;
    }

    @Nullable LobbyParkourManager getParkourManager();

    static void registerLobbyCommands() {
        HypixelGenericLoader.loopThroughPackage("dev.daweapon.type.lobby.commands", HypixelCommand.class).forEach(command -> {
            try {
                MinecraftServer.getCommandManager().register(command.getCommand());
            } catch (Exception e) {
                Logger.error(e, "Failed to register command {} in class {}",
                        command.getCommand().getName(), command.getClass().getSimpleName());
            }
        });
    }
}
