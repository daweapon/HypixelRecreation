package dev.daweapon.type.kuudra;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.proxyapi.ProxyService;
import dev.daweapon.type.game.instance.InstanceHeartbeat;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.SkyBlockTypeLoader;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.kuudra.fight.KuudraFightHooks;
import dev.daweapon.type.kuudra.redis.KuudraInstantiateGameHandler;
import dev.daweapon.type.kuudra.run.KuudraInstanceHost;
import dev.daweapon.type.kuudra.run.KuudraRequeue;
import dev.daweapon.type.kuudra.run.KuudraRuns;
import dev.daweapon.type.kuudra.run.KuudraSidebar;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.queue.KuudraQueue;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.SkyBlockTablistManager;

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
        SkyBlockGenericLoader.loopThroughPackage("dev.daweapon.type.kuudra.commands", HypixelCommand.class).forEach(command -> {
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
        KuudraFightHooks.register();
        KuudraRuns.startCleanup();
        KuudraQueue.useLocalRuns(KuudraRequeue::startHere);
        startOrchestratorHeartbeat();
    }

    private void startOrchestratorHeartbeat() {
        ProxyService orchestrator = new ProxyService(ServiceType.ORCHESTRATOR);
        InstanceHeartbeat.start(new KuudraInstanceHost(), new InstanceHeartbeat.Identity(
                HypixelConst::getServerUUID,
                HypixelConst::getShortenedServerName,
                HypixelConst::getMaxPlayers,
                () -> MinecraftServer.getConnectionManager().getOnlinePlayers().size()), orchestrator::handleRequest);
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(_ -> KuudraConfig.get().arena().respawn(), true);
    }

    @Override
    public TablistManager getTablistManager() {
        return new SkyBlockTablistManager(TabLocation.KUUDRA);
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SkyBlockGenericLoader.loopThroughPackage("dev.daweapon.type.kuudra.events", HypixelEventClass.class).toList();
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
