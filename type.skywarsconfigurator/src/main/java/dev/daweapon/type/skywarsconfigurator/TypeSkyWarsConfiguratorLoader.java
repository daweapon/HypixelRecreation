package dev.daweapon.type.skywarsconfigurator;

import dev.daweapon.type.generic.event.join.SpawnOnJoin;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.HypixelTypeLoader;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.redis.RedisOriginServer;
import dev.daweapon.type.generic.tab.EmptyTabModule;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;
import java.util.UUID;

public class TypeSkyWarsConfiguratorLoader implements HypixelTypeLoader {

    @Setter
    @Getter
    private static MapConfigurationSession currentSession;

    @Override
    public ServerType getType() {
        return ServerType.SKYWARS_CONFIGURATOR;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        MinecraftServer.getConnectionManager().setPlayerProvider((PlayerConnection playerConnection, GameProfile gameProfile) -> {
            HypixelPlayer player = new HypixelPlayer(playerConnection, gameProfile);

            UUID uuid = gameProfile.uuid();
            String username = gameProfile.name();

            ServerType originServer = RedisOriginServer.consume(uuid);

            if (originServer != null) {

                player.setOriginServer(originServer);
            }

            // Set player to creative mode for configuration
            player.setGameMode(GameMode.CREATIVE);

            Logger.info("Configurator: " + username + " joined");

            return player;
        });
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        HypixelGenericLoader.loopThroughPackage("dev.daweapon.type.skywarsconfigurator.commands", HypixelCommand.class).forEach(command -> {
            try {
                MinecraftServer.getCommandManager().register(command.getCommand());
            } catch (Exception e) {
                Logger.error(e, "Failed to register command " + command.getCommand().getName());
            }
        });

        Logger.info("SkyWars Configurator loaded. Use /swconfig to start.");
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
                return List.of(new EmptyTabModule());
            }
        };
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(
                (_) -> new Pos(0, 100, 0, 0, 0),
                false
        );
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SpawnOnJoin.intoEmptyInstance(HypixelGenericLoader.loopThroughPackage(
                "dev.daweapon.type.skywarsconfigurator.events",
                HypixelEventClass.class
        ));
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return List.of();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        return List.of();
    }


    @Override
    public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
        return List.of();
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return null;
    }
}
