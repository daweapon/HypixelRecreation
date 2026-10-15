package dev.daweapon.type.skywarslobby;

import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.data.GameDataHandler;
import dev.daweapon.type.generic.data.handlers.SkywarsDataHandler;
import dev.daweapon.type.generic.entity.hologram.PlayerHolograms;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.lobby.LobbyTypeLoader;
import dev.daweapon.type.lobby.events.LobbyAFKEvents;
import dev.daweapon.type.lobby.events.LobbyItemEvents;
import dev.daweapon.type.lobby.events.LobbyLaunchPadEvents;
import dev.daweapon.type.lobby.events.LobbyPlayerJoinEvents;
import dev.daweapon.type.lobby.events.LobbyPlayerMove;
import dev.daweapon.type.lobby.events.LobbyPlayerSpawnEvents;
import dev.daweapon.type.lobby.events.LobbyWorldEvent;
import dev.daweapon.type.lobby.item.LobbyItem;
import dev.daweapon.type.lobby.item.LobbyItemHandler;
import dev.daweapon.type.lobby.item.impl.HidePlayers;
import dev.daweapon.type.lobby.item.impl.LobbySelector;
import dev.daweapon.type.lobby.item.impl.PlayCompass;
import dev.daweapon.type.lobby.item.impl.ProfileItem;
import dev.daweapon.type.lobby.launchpad.LaunchPad;
import dev.daweapon.type.lobby.parkour.LobbyParkourManager;
import dev.daweapon.type.lobby.parkour.Parkour;
import dev.daweapon.type.skywarslobby.hologram.LeaderboardHologramManager;
import dev.daweapon.type.skywarslobby.hologram.SoulWellHologramManager;
import dev.daweapon.type.skywarslobby.item.SkywarsMenuItem;
import dev.daweapon.type.skywarslobby.kit.SkywarsKitRegistry;
import dev.daweapon.type.skywarslobby.level.SkywarsLevelRegistry;
import dev.daweapon.type.skywarslobby.parkour.SkywarsLobbyParkour;
import dev.daweapon.type.skywarslobby.perk.SkywarsPerkRegistry;
import dev.daweapon.type.skywarslobby.soulwell.SoulWellParticleManager;
import dev.daweapon.type.skywarslobby.soulwell.SoulWellUpgradeRegistry;
import dev.daweapon.type.skywarslobby.util.SkyWarsLobbyMap;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TypeSkyWarsLobbyLoader implements LobbyTypeLoader {
    @Getter
    private final LobbyItemHandler itemHandler = new LobbyItemHandler();
    @Getter
    private static LeaderboardHologramManager leaderboardManager;
    private LobbyParkourManager parkourManager;
    private final Pos spawnPont = new Pos(-3.5, 66, 0.5, -90, 0);

    @Override
    public ServerType getType() {
        return ServerType.SKYWARS_LOBBY;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        // Initialize registries
        SkywarsKitRegistry.initialize();
        SkywarsPerkRegistry.initialize();
        SkywarsLevelRegistry.initialize();
        SoulWellUpgradeRegistry.initialize();
        Logger.info("Initialized SkyWars kit, perk, level, and soul well upgrade registries");

        SkyWarsLobbyScoreboard.start();

        // Place map item frames in the lobby
        SkyWarsLobbyMap skywarsLobbyMap = new SkyWarsLobbyMap();
        skywarsLobbyMap.placeItemFrames(HypixelConst.getInstanceContainer());

        // Initialize parkour manager
        parkourManager = new LobbyParkourManager(getParkour());

        // Initialize leaderboard holograms
        LeaderboardHologramManager.initialize(HypixelConst.getInstanceContainer());

        // Initialize Soul Well hologram
        SoulWellHologramManager.initialize(HypixelConst.getInstanceContainer());

        // Initialize Soul Well particles
        SoulWellParticleManager.initialize(HypixelConst.getInstanceContainer());

        // Register all hotbar items
        getHotbarItems().values().forEach(itemHandler::add);

        // Register commands
        HypixelGenericLoader.loopThroughPackage("dev.daweapon.type.skywarslobby.commands", HypixelCommand.class).forEach(command -> {
            try {
                MinecraftServer.getCommandManager().register(command.getCommand());
            } catch (Exception e) {
                Logger.error(e, "Failed to register command " + command.getCommand().getName() + " in class " + command.getClass().getSimpleName());
            }
        });

        // Schedule hologram updates every 2 seconds
        MinecraftServer.getSchedulerManager().buildTask(PlayerHolograms::updateExternalHolograms)
                .delay(TaskSchedule.seconds(5))
                .repeat(TaskSchedule.seconds(2))
                .schedule();

        LobbyTypeLoader.registerLobbyCommands();
    }

    @Override
    public List<LaunchPad> getLaunchPads() {
        return List.of();
    }

    @Override
    public Map<Integer, LobbyItem> getHotbarItems() {
        return Map.of(
                0, new PlayCompass(),
                1, new ProfileItem(),
                2, new SkywarsMenuItem(),
                7, new HidePlayers(),
                8, new LobbySelector()
        );
    }

    @Override
    public Parkour getParkour() {
        return new SkywarsLobbyParkour();
    }

    @Override
    public LobbyParkourManager getParkourManager() {
        return parkourManager;
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
                return List.of();
            }
        };
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(
                (type) -> spawnPont,
                false
        );
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        List<HypixelEventClass> events = new ArrayList<>(HypixelGenericLoader.loopThroughPackage(
                "dev.daweapon.type.skywarslobby.events",
                HypixelEventClass.class
        ).toList());
        // Add lobby base events
        events.add(new LobbyAFKEvents());
        events.add(new LobbyItemEvents());
        events.add(new LobbyLaunchPadEvents());
        events.add(new LobbyPlayerJoinEvents());
        events.add(new LobbyPlayerSpawnEvents());
        events.add(new LobbyWorldEvent());
        events.add(new LobbyPlayerMove(spawnPont));
        return events;
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return HypixelGenericLoader.loopThroughPackage(
                "dev.daweapon.type.skywarslobby.events.custom",
                HypixelEventClass.class
        ).toList();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        return HypixelGenericLoader.loopThroughPackage(
                "dev.daweapon.type.skywarslobby.npcs",
                HypixelNPC.class
        ).toList();
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<RedisMessageHandler<?, ?>> getServiceHandlers() {
        return (List) HypixelGenericLoader.loopThroughPackage(
                "dev.daweapon.type.skywarslobby.redis.service",
                RedisMessageHandler.class
        ).toList();
    }

    @Override
    public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
        return List.of();
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return CustomWorlds.SKYWARS_LOBBY;
    }

    @Override
    public List<Class<? extends GameDataHandler>> getAdditionalDataHandlers() {
        return List.of(SkywarsDataHandler.class);
    }
}
