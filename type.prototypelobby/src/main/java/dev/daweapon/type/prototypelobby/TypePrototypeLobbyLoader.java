package dev.daweapon.type.prototypelobby;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.data.GameDataHandler;
import dev.daweapon.type.generic.data.handlers.PrototypeLobbyDataHandler;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.tab.EmptyTabModule;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.lobby.LobbyTypeLoader;
import dev.daweapon.type.lobby.events.LobbyAFKEvents;
import dev.daweapon.type.lobby.events.LobbyItemEvents;
import dev.daweapon.type.lobby.events.LobbyParkourEvents;
import dev.daweapon.type.lobby.events.LobbyPlayerJoinEvents;
import dev.daweapon.type.lobby.events.LobbyPlayerMove;
import dev.daweapon.type.lobby.events.LobbyPlayerSpawnEvents;
import dev.daweapon.type.lobby.events.LobbyWorldEvent;
import dev.daweapon.type.lobby.item.LobbyItem;
import dev.daweapon.type.lobby.item.LobbyItemHandler;
import dev.daweapon.type.lobby.item.impl.Collectibles;
import dev.daweapon.type.lobby.item.impl.HidePlayers;
import dev.daweapon.type.lobby.item.impl.LobbySelector;
import dev.daweapon.type.lobby.item.impl.PlayCompass;
import dev.daweapon.type.lobby.item.impl.ProfileItem;
import dev.daweapon.type.lobby.launchpad.LaunchPad;
import dev.daweapon.type.lobby.parkour.LobbyParkourManager;
import dev.daweapon.type.lobby.parkour.Parkour;
import dev.daweapon.type.prototypelobby.parkour.PrototypeLobbyParkour;
import dev.daweapon.type.prototypelobby.util.PrototypeLobbyMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Loader that wires up the Prototype Lobby server type: spawn, scoreboard, parkour, items, NPCs, and events.
public class TypePrototypeLobbyLoader implements LobbyTypeLoader {
    private static final LobbyItemHandler itemHandler = new LobbyItemHandler();
    public static LobbyParkourManager parkourManager;
    private final Pos SPAWN_POS = new Pos(11.5, 75, 0.5, 90, 0);

    @Override
    public ServerType getType() {
        return ServerType.PROTOTYPE_LOBBY;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        PrototypeLobbyScoreboard.start();

        PrototypeLobbyMap prototypeLobbyMap = new PrototypeLobbyMap();
        prototypeLobbyMap.placeItemFrames(HypixelConst.getInstanceContainer());

        // Register all hotbar items
        getHotbarItems().values().forEach(itemHandler::add);

        parkourManager = new LobbyParkourManager(getParkour());

        LobbyTypeLoader.registerLobbyCommands();
    }

    @Override
    public Parkour getParkour() {
        return new PrototypeLobbyParkour();
    }

    @Override
    public @Nullable LobbyParkourManager getParkourManager() {
        return parkourManager;
    }

    @Override
    public LobbyItemHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public List<LaunchPad> getLaunchPads() {
        return List.of(); // No launch pads for prototype lobby
    }

    @Override
    public Map<Integer, LobbyItem> getHotbarItems() {
        return Map.of(
                0, new PlayCompass(),
                1, new ProfileItem(),
            4, new Collectibles(),
                7, new HidePlayers(),
                8, new LobbySelector()
        );
    }

    @Override
    public List<ServiceType> getRequiredServices() {
        return List.of();
    }

    @Override
    public TablistManager getTablistManager() {
        return new TablistManager() {
            @Override
            public List<TablistModule> getModules() {
                return List.of(
                        new EmptyTabModule(),
                        new EmptyTabModule(),
                        new EmptyTabModule(),
                        new EmptyTabModule()
                );
            }
        };
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(
            (type) -> SPAWN_POS,
                false
        );
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        List<HypixelEventClass> events = new ArrayList<>(HypixelGenericLoader.loopThroughPackage(
                "dev.daweapon.type.prototypelobby.events",
                HypixelEventClass.class
        ).toList());
        // Add lobby base events
        events.add(new LobbyAFKEvents());
        events.add(new LobbyItemEvents());
        events.add(new LobbyPlayerJoinEvents());
        events.add(new LobbyPlayerSpawnEvents());
        events.add(new LobbyParkourEvents());
        events.add(new LobbyWorldEvent());
        events.add(new LobbyPlayerMove(SPAWN_POS));
        return events;
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return HypixelGenericLoader.loopThroughPackage(
                "dev.daweapon.type.prototypelobby.events.custom",
                HypixelEventClass.class
        ).toList();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        return HypixelGenericLoader.loopThroughPackage(
                "dev.daweapon.type.prototypelobby.npcs",
                HypixelNPC.class
        ).toList();
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<RedisMessageHandler<?, ?>> getServiceHandlers() {
        return (List) HypixelGenericLoader.loopThroughPackage(
                "dev.daweapon.type.prototypelobby.redis.service",
                RedisMessageHandler.class
        ).toList();
    }

    @Override
    public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
        return List.of();
    }

    @Override
    public List<Class<? extends GameDataHandler>> getAdditionalDataHandlers() {
        return List.of(PrototypeLobbyDataHandler.class);
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return CustomWorlds.PROTOTYPE_LOBBY;
    }
}
