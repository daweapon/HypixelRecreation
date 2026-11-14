package dev.daweapon.type.ravengarddungeon;

import com.google.gson.Gson;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.Tuple;
import dev.daweapon.commons.text.Text;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.HypixelTypeLoader;
import dev.daweapon.type.generic.RavengardTypeLoader;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.world.HypixelWorldLoader;
import dev.daweapon.type.ravengarddungeon.config.RavengardDungeonConfig;
import dev.daweapon.type.ravengarddungeon.game.RavengardDungeonGame;
import dev.daweapon.type.ravengarddungeon.user.RavengardDungeonPlayer;
import dev.daweapon.type.ravengardgeneric.RavengardGenericLoader;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

// RavengardTypeLoader for the Ravengard dungeon server type: loads the dungeon map/config, spawns the game, and reports heartbeats to the orchestrator.
public class TypeRavengardDungeonLoader implements RavengardTypeLoader {
    private static final Path CONFIGURATION = Path.of("./configuration/ravengard/dungeon_1.json");

    @Getter
    private static RavengardDungeonGame game;
    private RavengardDungeonConfig config;

    @Override
    public ServerType getType() {
        return ServerType.RAVENGARD_DUNGEON;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        try {
            config = new Gson().fromJson(Files.readString(CONFIGURATION), RavengardDungeonConfig.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load " + CONFIGURATION, exception);
        }
        Path polarPath = CONFIGURATION.getParent().resolve(config.polar());
        if (!Files.isRegularFile(polarPath)) {
            throw new IllegalStateException("Ravengard dungeon map not found: " + polarPath);
        }
        InstanceContainer temp = MinecraftServer.getInstanceManager().createInstanceContainer();
        final Instance instance;
        try {
            instance = HypixelWorldLoader.loadFrom(polarPath, temp, MinecraftServer.getInstanceManager());
        } catch (IOException e) {
            throw new IllegalStateException("Could not load Ravengard dungeon map: " + polarPath);
        }
        game = new RavengardDungeonGame(instance, config);
        MinecraftServer.getConnectionManager().setPlayerProvider((connection, profile) ->
                new RavengardDungeonPlayer(connection, profile));
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        // same recipe as skyblock islands: a fullbright dimension renders on the
        // client without any light data, which runtime-built instances lack
        MinecraftServer.getDimensionTypeRegistry().register(
                net.kyori.adventure.key.Key.key("ravengard:dungeon"),
                net.minestom.server.world.DimensionType.builder()
                        .ambientLight(1)
                        .build());
        dev.daweapon.type.ravengarddungeon.game.DungeonInstanceRegistry.startExpiryTask();
        dev.daweapon.type.ravengarddungeon.game.DungeonMinimapIcons.register();
        dev.daweapon.type.ravengarddungeon.interactables.InteractableRegistry.startTask();
        dev.daweapon.type.ravengardgeneric.commands.SpawnSatchelCommand.spawner = player ->
                dev.daweapon.type.ravengarddungeon.interactables.DungeonSatchel.spawn(
                        player.getInstance(), player.getPosition());
        dev.daweapon.type.ravengardgeneric.commands.DungeonCommand.localJoin = (player, instanceId) -> {
            var instance = dev.daweapon.type.ravengarddungeon.game.DungeonInstanceRegistry.findByIdPrefix(instanceId);
            if (instance == null) {
                return false;
            }
            dev.daweapon.type.ravengarddungeon.game.DungeonInstanceRegistry.sendPlayerIn(player, instance);
            return true;
        };
        startOrchestratorHeartbeat();
    }

    private void startOrchestratorHeartbeat() {
        dev.daweapon.proxyapi.ProxyService orchestrator = new dev.daweapon.proxyapi.ProxyService(ServiceType.ORCHESTRATOR);
        dev.daweapon.type.game.instance.InstanceHeartbeat.start(
                new dev.daweapon.type.ravengarddungeon.game.RavengardInstanceHost(),
                new dev.daweapon.type.game.instance.InstanceHeartbeat.Identity(
                        dev.daweapon.type.generic.HypixelConst::getServerUUID,
                        dev.daweapon.type.generic.HypixelConst::getShortenedServerName,
                        dev.daweapon.type.generic.HypixelConst::getMaxPlayers,
                        () -> MinecraftServer.getConnectionManager().getOnlinePlayers().size()),
                orchestrator::handleRequest);
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
                return List.of();
            }
        };
    }

    @Override
    public HypixelTypeLoader.LoaderValues getLoaderValues() {
        Pos spawn = config == null ? new Pos(0.5, 65, 0.5) : config.spawnPosition();
        return new HypixelTypeLoader.LoaderValues(spawn, false);
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return RavengardGenericLoader.loopThroughPackage(
                "dev.daweapon.type.ravengarddungeon.events",
                HypixelEventClass.class
        ).toList();
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
        return List.of(new dev.daweapon.type.ravengarddungeon.redis.DungeonInstantiateGameHandler());
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return null;
    }

    @Override
    public boolean headerFooterPerPlayer() {
        return true;
    }

    @Override
    public @NonNull Optional<Tuple<Text, Text>> headerFooter() {
        return Optional.of(new Tuple<>(Text.empty(), Text.empty()));
    }
}
