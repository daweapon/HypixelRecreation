package dev.daweapon.type.ravengardgeneric;

import dev.daweapon.commons.reflection.PackageScanner;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.CustomWorlds;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.HypixelTypeLoader;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.generic.packet.HypixelPacketClientListener;
import dev.daweapon.type.generic.packet.HypixelPacketServerListener;
import dev.daweapon.type.generic.redis.RedisOriginServer;
import dev.daweapon.type.generic.resourcepack.ResourcePackManager;
import dev.daweapon.type.generic.world.HypixelWorldLoader;
import dev.daweapon.type.ravengardgeneric.resourcepack.RavengardPack;
import dev.daweapon.type.ravengardgeneric.user.RavengardPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

// Bootstraps Ravengard-specific systems (items, shops, regions, events, commands, resource pack) on top of a HypixelTypeLoader.
public record RavengardGenericLoader(HypixelTypeLoader typeLoader) {

    @Getter
    private static MinecraftServer server;

    @Nullable
    public static Instance tutorialInstance;

    @SneakyThrows
    public void initialize(MinecraftServer server) {
        RavengardGenericLoader.server = server;
        dev.daweapon.type.generic.data.domain.PlayerDataService.register(new dev.daweapon.type.ravengardgeneric.data.RavengardDomain());

        if (typeLoader.getType() == ServerType.RAVENGARD_LOBBY) {
            tutorialInstance = HypixelWorldLoader.load(CustomWorlds.RAVENGARD_TUTORIAL, MinecraftServer.getInstanceManager());
        }

        dev.daweapon.type.ravengardgeneric.item.RavengardItemRegistry.load();
        dev.daweapon.type.ravengardgeneric.shop.RavengardShopRegistry.load();
        dev.daweapon.type.ravengardgeneric.item.attribute.RavengardItemAttribute.registerItemAttributes();
        dev.daweapon.type.ravengardgeneric.entity.mob.RavengardMob.startTicking();
        connectRegions();
        dev.daweapon.type.ravengardgeneric.region.RavengardRegion.cacheRegions();
        dev.daweapon.type.ravengardgeneric.region.RavengardRegionTracker.start();

        loopThroughPackage("dev.daweapon.type.ravengardgeneric.event.actions", HypixelEventClass.class)
                .forEach(HypixelEventHandler::registerEventMethods);

        loopThroughPackage("dev.daweapon.type.ravengardgeneric.commands", HypixelCommand.class)
                .forEach(command -> {
                    try {
                        MinecraftServer.getCommandManager().register(command.getCommand());
                    } catch (Exception e) {
                        Logger.error(e, "Failed to register command {} in class {}",
                                command.getCommand().getName(), command.getClass().getSimpleName());
                    }
                });

        loopThroughPackage("dev.daweapon.type.ravengardgeneric.packets.client", HypixelPacketClientListener.class)
                .forEach(HypixelPacketClientListener::cacheListener);
        loopThroughPackage("dev.daweapon.type.ravengardgeneric.packets.server", HypixelPacketServerListener.class)
                .forEach(HypixelPacketServerListener::cacheListener);

        HypixelPacketClientListener.register(HypixelConst.getEventHandler());
        HypixelPacketServerListener.register(HypixelConst.getEventHandler());
        HypixelEventHandler.register(HypixelConst.getEventHandler());

        RavengardPack testingPack = RavengardPack.fromConfig();
        ResourcePackManager packManager = new ResourcePackManager(testingPack);
        HypixelConst.setResourcePackManager(packManager);
        packManager.initialize();

        MinecraftServer.getConnectionManager().setPlayerProvider((playerConnection, gameProfile) -> {
            RavengardPlayer player = new RavengardPlayer(playerConnection, gameProfile);

            UUID uuid = playerConnection.getPlayer().getUuid();
            String username = playerConnection.getPlayer().getUsername();

            ServerType originServer = RedisOriginServer.consume(uuid);

            if (originServer != null) {

                player.setOriginServer(originServer);
            }

            Logger.info("Received new Ravengard player: {} ({})", username, uuid);
            return player;
        });
    }

    private static void connectRegions() {
        try {
            com.mongodb.ConnectionString connection = new com.mongodb.ConnectionString(
                    dev.daweapon.commons.config.ConfigProvider.settings().getMongodb());
            com.mongodb.MongoClientSettings settings = com.mongodb.MongoClientSettings.builder()
                    .applyConnectionString(connection).build();
            com.mongodb.client.MongoClient client = com.mongodb.client.MongoClients.create(settings);
            dev.daweapon.type.ravengardgeneric.data.monogdb.RavengardRegionDatabase.connect(client);
            dev.daweapon.type.ravengardgeneric.data.monogdb.RavengardProfileDatabase.connect(client);
            dev.daweapon.type.ravengardgeneric.data.monogdb.RavengardTrackedItemsDatabase.connect(client);
        } catch (Exception exception) {
            Logger.warn(exception, "Could not connect the Ravengard region collection; "
                    + "only built-in regions will be available");
        }
    }

    public static List<RavengardPlayer> getLoadedPlayers() {
        List<RavengardPlayer> players = new ArrayList<>();
        MinecraftServer.getConnectionManager().getOnlinePlayers()
                .stream()
                .filter(player -> player instanceof RavengardPlayer)
                .filter(player -> player.getInstance() != null)
                .filter(player -> dev.daweapon.type.generic.data.DataHandler
                        .findUser(player.getUuid()).isPresent())
                .forEach(player -> players.add((RavengardPlayer) player));
        return players;
    }

    public static @Nullable RavengardPlayer getFromUUID(UUID uuid) {
        return getLoadedPlayers().stream()
                .filter(player -> player.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public static <T> Stream<T> loopThroughPackage(String packageName, Class<T> clazz) {
        return PackageScanner.instantiateSubtypes(packageName, clazz);
    }
}
