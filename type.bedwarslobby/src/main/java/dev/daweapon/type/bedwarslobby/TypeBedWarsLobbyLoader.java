package dev.daweapon.type.bedwarslobby;

import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.bedwarslobby.hologram.LeaderboardHologramManager;
import dev.daweapon.type.bedwarslobby.item.impl.BedWarsMenu;
import dev.daweapon.type.bedwarslobby.launchpad.BedWarsLaunchPads;
import dev.daweapon.type.bedwarslobby.parkour.BedWarsLobbyParkour;
import dev.daweapon.type.bedwarslobby.util.BedWarsLobbyMap;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.collectibles.bedwars.BedWarsCollectibleCatalog;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.GameDataHandler;
import dev.daweapon.type.generic.data.handlers.BedWarsDataHandler;
import dev.daweapon.type.generic.entity.hologram.PlayerHolograms;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.leaderboard.BedWarsLeaderboardAggregator;
import dev.daweapon.type.generic.tab.EmptyTabModule;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.lobby.LobbyTypeLoader;
import dev.daweapon.type.lobby.events.LobbyAFKEvents;
import dev.daweapon.type.lobby.events.LobbyItemEvents;
import dev.daweapon.type.lobby.events.LobbyLaunchPadEvents;
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
import dev.daweapon.type.lobby.launchpad.LaunchPadHandler;
import dev.daweapon.type.lobby.parkour.LobbyParkourManager;
import dev.daweapon.type.lobby.parkour.Parkour;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// Entry point that bootstraps the Bedwars lobby server type: map, parkour, holograms, and launch pads.
public class TypeBedWarsLobbyLoader implements LobbyTypeLoader {
    public static BedWarsLobbyMap bedWarsLobbyMap = new BedWarsLobbyMap();
    public static LobbyParkourManager parkourManager;
    private final Pos spawnPoint = new Pos(-39.5, 72, 0, -90, 0);

    @Getter
    private final LobbyItemHandler itemHandler = new LobbyItemHandler();

    @Override
    public ServerType getType() {
        return ServerType.BEDWARS_LOBBY;
    }

    @Override
    public Parkour getParkour() {
        return new BedWarsLobbyParkour();
    }

    @Override
    public void onInitialize(MinecraftServer server) {
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        BedWarsCollectibleCatalog.initialize();
        BedWarsLobbyScoreboard.start();
        bedWarsLobbyMap.placeItemFrames(HypixelConst.getInstanceContainer());

        // Register launch pads
        LaunchPadHandler.register(MinecraftServer.getSchedulerManager(), getLaunchPads());

        // Register all hotbar items
        getHotbarItems().values().forEach(itemHandler::add);

        // Initialize leaderboard holograms
        LeaderboardHologramManager.initialize(HypixelConst.getInstanceContainer());

        // Register commands
        HypixelGenericLoader.loopThroughPackage("dev.daweapon.type.bedwarslobby.commands", HypixelCommand.class).forEach(command -> {
            try {
                MinecraftServer.getCommandManager().register(command.getCommand());
            } catch (Exception e) {
                Logger.error(e, "Failed to register command " + command.getCommand().getName() + " in class " + command.getClass().getSimpleName());
            }
        });

        // Start the leaderboard aggregator (for daily/weekly/monthly leaderboard updates)
        BedWarsLeaderboardAggregator.initialize();

        // Schedule hologram updates every 2 seconds
        MinecraftServer.getSchedulerManager().buildTask(PlayerHolograms::updateExternalHolograms).delay(TaskSchedule.seconds(5))
          .repeat(TaskSchedule.seconds(2))
          .schedule();

        parkourManager = new LobbyParkourManager(getParkour());

        LobbyTypeLoader.registerLobbyCommands();
    }

    @Override
    public List<LaunchPad> getLaunchPads() {
        return Arrays.asList(BedWarsLaunchPads.values());
    }

    @Override
    public @Nullable LobbyParkourManager getParkourManager() {
        return parkourManager;
    }

    @Override
    public Map<Integer, LobbyItem> getHotbarItems() {
        return Map.of(
                0, new PlayCompass(),
                1, new ProfileItem(),
                2, new BedWarsMenu(),
                4, new Collectibles(),
                7, new HidePlayers(),
                8, new LobbySelector()
        );
    }

    @Override
    public List<ServiceType> getRequiredServices() {
        return List.of(ServiceType.ORCHESTRATOR);
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
                (type) -> spawnPoint,
                false
        );
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        List<HypixelEventClass> events = new ArrayList<>(HypixelGenericLoader.loopThroughPackage(
                "dev.daweapon.type.bedwarslobby.events",
                HypixelEventClass.class
        ).toList());
        // Add lobby base events
        events.add(new LobbyAFKEvents());
        events.add(new LobbyItemEvents());
        events.add(new LobbyParkourEvents());
        events.add(new LobbyLaunchPadEvents());
        events.add(new LobbyPlayerJoinEvents());
        events.add(new LobbyPlayerSpawnEvents());
        events.add(new LobbyWorldEvent());
        events.add(new LobbyPlayerMove(spawnPoint));
        return events;
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return HypixelGenericLoader.loopThroughPackage(
                "dev.daweapon.type.bedwarslobby.events.custom",
                HypixelEventClass.class
        ).toList();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        return HypixelGenericLoader.loopThroughPackage(
                "dev.daweapon.type.bedwarslobby.npcs",
                HypixelNPC.class
        ).toList();
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<RedisMessageHandler<?, ?>> getServiceHandlers() {
        return (List) HypixelGenericLoader.loopThroughPackage(
                "dev.daweapon.type.bedwarslobby.redis.service",
                RedisMessageHandler.class
        ).toList();
    }

    @Override
    public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
        return List.of();
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return CustomWorlds.BEDWARS_LOBBY;
    }

    @Override
    public List<Class<? extends GameDataHandler>> getAdditionalDataHandlers() {
        return List.of(BedWarsDataHandler.class);
    }
}
