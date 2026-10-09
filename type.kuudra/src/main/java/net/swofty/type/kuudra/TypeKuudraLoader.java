package net.swofty.type.kuudra;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.CustomWorlds;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.orchestrator.GameHeartbeatProtocol;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.game.game.GameObject;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.SkyBlockTypeLoader;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.tab.TablistManager;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.kuudra.redis.KuudraInstantiateGameHandler;
import net.swofty.type.kuudra.run.KuudraRun;
import net.swofty.type.kuudra.run.KuudraRuns;
import net.swofty.type.kuudra.run.KuudraSidebar;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import net.swofty.type.skyblockgeneric.tabmodules.AccountInformationModule;
import net.swofty.type.skyblockgeneric.tabmodules.SkyBlockPlayersOnlineModule;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * The Kuudra server: hosts up to {@link KuudraRuns#MAX_RUNS} runs, each in its own copy of Kuudra's Hollow.
 * There is no shared world; players wait in the empty instance until they are routed into their run.
 */
public class TypeKuudraLoader implements SkyBlockTypeLoader {
    public static final Pos WAITING_POSITION = new Pos(0.5, 100, 0.5);

    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_KUUDRA;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        SkyBlockGenericLoader.loopThroughPackage("net.swofty.type.kuudra.commands", HypixelCommand.class).forEach(command -> {
            try {
                MinecraftServer.getCommandManager().register(command.getCommand());
            } catch (Exception exception) {
                Logger.error(exception, "Failed to register command {}", command.getClass().getSimpleName());
            }
        });
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        KuudraConfig.get();
        KuudraSidebar.register();
        KuudraRuns.startCleanup();
        startOrchestratorHeartbeat();
    }

    private void startOrchestratorHeartbeat() {
        ProxyService orchestrator = new ProxyService(ServiceType.ORCHESTRATOR);
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            List<GameObject> games = new ArrayList<>();
            for (KuudraRun run : KuudraRuns.all()) {
                GameObject game = new GameObject();
                game.setGameId(run.id());
                game.setType(ServerType.SKYBLOCK_KUUDRA);
                game.setMap("kuudras_hollow");
                game.setGameTypeName(run.tier().name());
                game.setAcceptingJoins(false);
                game.setInvolvedPlayers(new ArrayList<>(run.roster()));
                game.setDisconnectedPlayers(run.disconnectedMembers());
                games.add(game);
            }
            orchestrator.handleRequest(new GameHeartbeatProtocol.HeartbeatMessage(
                    HypixelConst.getServerUUID(),
                    HypixelConst.getShortenedServerName(),
                    getType(),
                    HypixelConst.getMaxPlayers(),
                    MinecraftServer.getConnectionManager().getOnlinePlayers().size(),
                    games,
                    List.of(),
                    KuudraRuns.remainingSlots()));
        }).delay(TaskSchedule.seconds(5)).repeat(TaskSchedule.seconds(1)).schedule();
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(_ -> KuudraConfig.get().arena().respawn(), true);
    }

    @Override
    public TablistManager getTablistManager() {
        return new TablistManager() {
            @Override
            public List<TablistModule> getModules() {
                return new ArrayList<>(List.of(
                        new SkyBlockPlayersOnlineModule(1),
                        new AccountInformationModule()
                ));
            }
        };
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SkyBlockGenericLoader.loopThroughPackage("net.swofty.type.kuudra.events", HypixelEventClass.class).toList();
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
        return List.of(new KuudraInstantiateGameHandler());
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
