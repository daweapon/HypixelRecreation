package dev.daweapon.type.catacombs;

import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.proxyapi.ProxyService;
import dev.daweapon.type.catacombs.redis.CatacombsInstantiateGameHandler;
import dev.daweapon.type.catacombs.run.CatacombsHooks;
import dev.daweapon.type.catacombs.run.CatacombsInstanceHost;
import dev.daweapon.type.catacombs.run.CatacombsRuns;
import dev.daweapon.type.catacombs.run.CatacombsSidebar;
import dev.daweapon.type.game.instance.InstanceHeartbeat;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.SkyBlockTypeLoader;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.tab.AreaServerModule;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.tabmodules.AccountInformationModule;
import dev.daweapon.type.skyblockgeneric.tabmodules.SkyBlockPlayersOnlineModule;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * The Catacombs server: hosts up to {@link CatacombsRuns#MAX_RUNS} runs, each in its own generated dungeon. There is no
 * shared world; players wait in the empty instance until they are routed into their run.
 */
public class TypeCatacombsLoader implements SkyBlockTypeLoader {
    public static final Pos WAITING_POSITION = new Pos(0.5, 100, 0.5);

    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_CATACOMBS;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        SkyBlockGenericLoader.loopThroughPackage("dev.daweapon.type.catacombs.commands", HypixelCommand.class).forEach(command -> {
            try {
                MinecraftServer.getCommandManager().register(command.getCommand());
            } catch (Exception exception) {
                Logger.error(exception, "Failed to register command {}", command.getClass().getSimpleName());
            }
        });
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        CatacombsSidebar.register();
        CatacombsHooks.register();
        CatacombsRuns.startCleanup();
        startOrchestratorHeartbeat();
    }

    private void startOrchestratorHeartbeat() {
        ProxyService orchestrator = new ProxyService(ServiceType.ORCHESTRATOR);
        InstanceHeartbeat.start(new CatacombsInstanceHost(), new InstanceHeartbeat.Identity(
                HypixelConst::getServerUUID,
                HypixelConst::getShortenedServerName,
                HypixelConst::getMaxPlayers,
                () -> MinecraftServer.getConnectionManager().getOnlinePlayers().size()), orchestrator::handleRequest);
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(WAITING_POSITION, true);
    }

    @Override
    public TablistManager getTablistManager() {
        return new TablistManager() {
            @Override
            public List<TablistModule> getModules() {
                return new ArrayList<>(List.of(
                        new SkyBlockPlayersOnlineModule(1),
                        new SkyBlockPlayersOnlineModule(2),
                        new AreaServerModule("tablist.server_info.area.catacombs"),
                        new AccountInformationModule()
                ));
            }
        };
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SkyBlockGenericLoader.loopThroughPackage("dev.daweapon.type.catacombs.events", HypixelEventClass.class).toList();
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
    public List<RedisMessageHandler<?, ?>> getServiceHandlers() {
        return List.of(new CatacombsInstantiateGameHandler());
    }

    @Override
    public List<ServiceType> getRequiredServices() {
        return List.of(ServiceType.ORCHESTRATOR, ServiceType.ITEM_TRACKER);
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return null;
    }
}
