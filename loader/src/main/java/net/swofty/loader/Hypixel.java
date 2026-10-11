package net.swofty.loader;

import io.sentry.ProfileLifecycle;
import io.sentry.Sentry;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.anticheat.flag.FlagType;
import net.swofty.anticheat.loader.PunishmentHandler;
import net.swofty.anticheat.loader.SwoftyAnticheat;
import net.swofty.anticheat.loader.SwoftyValues;
import net.swofty.anticheat.loader.minestom.MinestomLoader;
import net.swofty.commons.ServerType;
import net.swofty.commons.TestFlow;
import net.swofty.commons.config.ConfigProvider;
import net.swofty.commons.config.ConfigurationOverlay;
import net.swofty.commons.data.SwoftyData;
import net.swofty.commons.proxy.ProxyLinkSupervisor;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.proxy.to.*;
import net.swofty.commons.redis.ProxyHeartbeat;
import net.swofty.commons.redis.RedisClient;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.proxyapi.ProxyAPI;
import net.swofty.proxyapi.ProxyService;
import net.swofty.spark.Spark;
import net.swofty.type.generic.*;
import net.swofty.type.generic.i18n.HypixelTranslator;
import net.swofty.type.ravengardgeneric.RavengardGenericLoader;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import org.reflections.Reflections;
import org.tinylog.Logger;

import java.net.InetAddress;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


// Server process entry point: parses launch args, bootstraps Minestom, discovers and initializes the matching
// HypixelTypeLoader, registers Redis proxy/service handlers, and manages proxy registration and heartbeat monitoring.
public class Hypixel {
    // Must run before any configuration is read - ConfigProvider loads during static init below.
    static {
        ConfigurationOverlay.apply();
    }

    @Getter
    @Setter
    private static UUID serverUUID;

    private static final boolean ENABLE_SPARK = ConfigProvider.settings().getIntegrations().isSpark();

    @SneakyThrows
    static void main(String[] args) {
        System.setProperty("minestom.automatic-component-translation", "true");
        sun.misc.Signal.handle(new sun.misc.Signal("TERM"), signal -> System.exit(0));
        if (args.length == 0 || !ServerType.isServerType(args[0])) {
            Logger.error("Please specify a server type.");
            Arrays.stream(ServerType.values()).forEach(serverType -> Logger.error(serverType.name()));
            System.exit(0);
            return;
        }

        if (!ConfigProvider.settings().getIntegrations().getSentryDsn().isBlank()) {
            Sentry.init(options -> {
                options.setDsn(ConfigProvider.settings().getIntegrations().getSentryDsn());
                options.setSendDefaultPii(true);
                options.setTracesSampleRate(1.0);
                options.setProfileSessionSampleRate(1.0);
                options.setProfileLifecycle(ProfileLifecycle.TRACE);
            });
        }

        ServerType serverType = ServerType.valueOf(args[0].toUpperCase());
        long startTime = System.currentTimeMillis();

        Map<String, String> options = parseOptionalArgs(args);
        int maxPlayers = options.containsKey("--max-players") ?
                Integer.parseInt(options.get("--max-players")) : 20;

        // Test flow configuration
        String testFlowName = options.get("--test-flow");
        String testFlowHandler = options.get("--test-flow-handler");
        String testFlowPlayers = options.get("--test-flow-players");
        String testFlowIndex = options.get("--test-flow-index");
        String testFlowTotal = options.get("--test-flow-total");
        String testFlowTotalExpected = options.get("--test-flow-total-expected");

        boolean isTestFlow = testFlowName != null;

        if (isTestFlow) {
            Logger.info("Starting server as part of test flow: " + testFlowName);
            Logger.info("Handler: " + testFlowHandler);
            Logger.info("Players: " + testFlowPlayers);
            Logger.info("Server index: " + testFlowIndex + " of " + testFlowTotal);
        }

        // Initialize Minecraft server
		MinecraftServer minecraftServer = MinecraftServer.init(
                new Auth.Velocity(ConfigProvider.settings().getVelocitySecret())
        );
        serverUUID = UUID.randomUUID();

        SwoftyData.bootstrap(ConfigProvider.settings().getRedisUri());

        // Loaders can schedule Redis-backed work immediately (calendar events,
        // elections, etc.), so identify and connect this server before running
        // any loader initialization.
        ProxyAPI proxyAPI = new ProxyAPI(ConfigProvider.settings().getRedisUri(), serverUUID);

        // Initialize GenericLoader
        Reflections reflections = new Reflections("net.swofty.type");
        Set<Class<? extends HypixelTypeLoader>> subTypes = reflections.getSubTypesOf(HypixelTypeLoader.class);
        if (subTypes.isEmpty()) {
            Logger.error("No TypeLoader found!");
            System.exit(0);
            return;
        }

        HypixelTypeLoader typeLoader = subTypes.stream()
            .map(clazz -> {
                try {
                    HypixelTypeLoader instance = clazz.getDeclaredConstructor().newInstance();
                    Logger.info("Found TypeLoader: " + instance.getType().name());
                    return instance;
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .filter(instance -> instance.getType() == serverType)
            .findFirst()
            .orElseThrow(() ->
                new IllegalStateException("No TypeLoader found for server type " + serverType)
            );

        new HypixelGenericLoader(typeLoader).initialize(minecraftServer);

        // Initialize TypeLoader
        SkyBlockGenericLoader skyblockLoader = null;
        if (typeLoader instanceof SkyBlockTypeLoader) {
            skyblockLoader = new SkyBlockGenericLoader(typeLoader);
            skyblockLoader.initialize(minecraftServer);
        }
        if (typeLoader instanceof RavengardTypeLoader) {
            new RavengardGenericLoader(typeLoader).initialize(minecraftServer);
        }

        // Initialize the server
        typeLoader.onInitialize(minecraftServer);

        // Initialize proxy support
        SkyBlockGenericLoader.loopThroughPackage("net.swofty.type.generic.redis", RedisMessageHandler.class)
                .forEach(proxyAPI::registerProxyHandler);
        SkyBlockGenericLoader.loopThroughPackage("net.swofty.type.generic.redis.service", RedisMessageHandler.class)
                .forEach(proxyAPI::registerServiceHandler);
        typeLoader.getProxyHandlers().forEach(proxyAPI::registerProxyHandler);
        typeLoader.getServiceHandlers().forEach(proxyAPI::registerServiceHandler);
        if (typeLoader instanceof SkyBlockTypeLoader) {
            SkyBlockGenericLoader.loopThroughPackage("net.swofty.type.skyblockgeneric.redis", RedisMessageHandler.class)
                    .forEach(proxyAPI::registerProxyHandler);
            SkyBlockGenericLoader.loopThroughPackage("net.swofty.type.skyblockgeneric.redis.service", RedisMessageHandler.class)
                    .forEach(proxyAPI::registerServiceHandler);
        } else if (typeLoader instanceof RavengardTypeLoader) {
            SkyBlockGenericLoader.loopThroughPackage("net.swofty.type.ravengardgeneric.redis", RedisMessageHandler.class)
                    .forEach(proxyAPI::registerProxyHandler);
        }
        RedisProtocol<?, ?>[] toProxyProtocols = {
                new RequestServerNameProtocol(), new PlayerCountProtocol(),
                new PlayerHandlerProtocol(), new ProxyIsOnlineProtocol(),
                new RegisterServerProtocol(), new FinishedWithPlayerProtocol(),
                new RequestServersProtocol(), new RegisterTestFlowProtocol(),
                new TestFlowServerReadyProtocol(), new StaffChatProtocol(),
                new PunishPlayerProtocol()
        };
        for (RedisProtocol<?, ?> protocol : toProxyProtocols) {
            RedisClient.registerResponseProtocol(protocol);
        }
        List<RedisProtocol> protocols = SkyBlockGenericLoader.loopThroughPackage(
                "net.swofty.commons.protocol.objects", RedisProtocol.class)
                .filter(obj -> !obj.getClass().getPackageName().startsWith("net.swofty.commons.protocol.objects.proxy"))
                .toList();
        protocols.forEach(RedisClient::registerResponseProtocol);
        proxyAPI.start();

        // Start spark if enabled
        if (ENABLE_SPARK) {
            Spark.enable(Files.createTempDirectory("spark"));
            Logger.info("Spark has been enabled");
        }

        // Ensure all services are running
        typeLoader.getRequiredServices().forEach(serviceType -> {
            new ProxyService(serviceType).isOnline().thenAccept(online -> {
                if (!online) {
                    Logger.error("Service " + serviceType.name() + " is not online!");
                }
            });
        });

        typeLoader.afterInitialize(minecraftServer);
        if (skyblockLoader != null) {
            skyblockLoader.afterInitialize();
        }

        HypixelTranslator translator = new HypixelTranslator();
        GlobalTranslator.translator().addSource(translator);
        Logger.info("Loaded " + translator.keyCount() + " translation keys for default locale");

        // Start the server
        MinecraftServer.setBrandName("Hypixel");
        ProxyHeartbeat.init(ConfigProvider.settings().getRedisUri());
        ProxyLinkSupervisor proxyLink = createProxyLink(serverType, maxPlayers, options);
        Logger.info("Registering with the proxy...");
        int port = proxyLink.connect().join();

        minecraftServer.start(ConfigProvider.settings().getHostName(), port);

        long endTime = System.currentTimeMillis();
        Logger.info("Started server on port " + port + " in " + (endTime - startTime) + "ms");
        Logger.info("Server Type: " + serverType.name());
        Logger.info("Internal ID: " + serverUUID.toString());
        HypixelConst.setPort(port);
        HypixelConst.setMaxPlayers(maxPlayers);
        HypixelConst.setServerUUID(serverUUID);

        proxyLink.startMonitoring();

        // Initialize anticheat
        if (ConfigProvider.settings().getIntegrations().isAnticheat()) {
            Thread.startVirtualThread(() -> {
                Logger.info("Initializing anticheat...");

                MinestomLoader minestomLoader = new MinestomLoader();
                SwoftyAnticheat.loader(minestomLoader);
                SwoftyAnticheat.values(new SwoftyValues());
                SwoftyAnticheat.punishmentHandler(new PunishmentHandler() {
                    @Override
                    public void onFlag(UUID uuid, FlagType flagType) {
                        Logger.info("Player " + uuid + " flagged for " + flagType.name());
                    }
                });
                SwoftyAnticheat.start();

                Logger.info("Anticheat initialized");
            });
        }
    }

    private static ProxyLinkSupervisor createProxyLink(ServerType serverType, int maxPlayers, Map<String, String> options) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "proxy-link");
            thread.setDaemon(true);
            return thread;
        });

        ProxyLinkSupervisor.ProxyLink link = new ProxyLinkSupervisor.ProxyLink() {
            @Override
            public boolean isProxyAlive() {
                return ProxyHeartbeat.isProxyAlive();
            }

            @Override
            public CompletableFuture<ProxyLinkSupervisor.Registration> register(Integer port) {
                return registerWithProxy(serverType, maxPlayers, options, port)
                        .thenApply(response -> new ProxyLinkSupervisor.Registration(
                                response.host(), response.port(), response.success(), response.error()));
            }

            @Override
            public CompletableFuture<ProxyLinkSupervisor.AssignedName> requestName() {
                return RedisClient.requestProxy(new RequestServerNameProtocol(), new RequestServerNameProtocol.Request())
                        .thenApply(response -> new ProxyLinkSupervisor.AssignedName(
                                response.serverName(), response.shortenedServerName(),
                                response.success(), response.error()));
            }
        };

        ProxyLinkSupervisor.Listener listener = new ProxyLinkSupervisor.Listener() {
            @Override
            public void onNameAssigned(ProxyLinkSupervisor.AssignedName name, boolean initial) {
                adoptServerName(name, initial, serverType, options);
            }

            @Override
            public void onProxyLost() {
                Logger.warn("{} player(s) still connected; their sessions end and save when the proxy connection closes",
                        MinecraftServer.getConnectionManager().getOnlinePlayers().size());
            }

            @Override
            public void onProxyRestored() {
                Logger.info("Routable through the proxy again as {}", HypixelConst.getServerName());
            }
        };

        return new ProxyLinkSupervisor(link,
                (delay, task) -> executor.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS),
                listener);
    }

    private static void adoptServerName(ProxyLinkSupervisor.AssignedName name, boolean initial,
                                        ServerType serverType, Map<String, String> options) {
        if (options.get("--test-flow") != null) {
            String serverNameRaw = name.shortName().substring(1);
            HypixelConst.setServerName("isolated" + serverNameRaw);
            HypixelConst.setShortenedServerName("i" + serverNameRaw);
            if (initial) {
                handleTestFlowRegistration(options.get("--test-flow"), options.get("--test-flow-handler"),
                        options.get("--test-flow-players"), serverType, options.get("--test-flow-index"),
                        options.get("--test-flow-total"), options.get("--test-flow-server-configs"));
            }
        } else {
            HypixelConst.setServerName(name.name());
            HypixelConst.setShortenedServerName(name.shortName());
        }

        Logger.info("Received server name: " + HypixelConst.getServerName());
    }

    @SneakyThrows
    private static CompletableFuture<RegisterServerProtocol.Response> registerWithProxy(ServerType serverType,
                                                                                        int maxPlayers,
                                                                                        Map<String, String> options,
                                                                                        Integer port) {
        String testFlowName = options.get("--test-flow");
        boolean isTestFlow = testFlowName != null;

        return RedisClient.requestProxy(new RegisterServerProtocol(),
                new RegisterServerProtocol.Request(
                        serverType.name(), maxPlayers, InetAddress.getLocalHost().getHostName(), port,
                        isTestFlow ? true : null,
                        isTestFlow ? testFlowName : null,
                        isTestFlow ? options.get("--test-flow-index") : null,
                        isTestFlow ? options.get("--test-flow-total") : null));
    }

    private static void handleTestFlowRegistration(String testFlowName, String handler, String players,
                                                   ServerType serverType, String index, String total, String serverConfigs) {
        // Parse players list and set up local test flow state
        List<String> playerList = Arrays.stream(players.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // Set the current test flow for this server
        TestFlow.setCurrentTestFlow(testFlowName, playerList);

        // Only the first server (global index 0) should register the test flow with the proxy
        if ("0".equals(index)) {
            Logger.info("Registering test flow with proxy: " + testFlowName);

            List<Map<String, Object>> configList = new ArrayList<>();
            String[] configs = serverConfigs.split(",");
            for (String config : configs) {
                String[] parts = config.trim().split(":");
                String type = parts[0];
                int count = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                configList.add(Map.of("type", type, "count", count));
            }

            RedisClient.requestProxy(new RegisterTestFlowProtocol(),
                    new RegisterTestFlowProtocol.Request(testFlowName, handler, playerList, configList))
                    .thenAccept(response -> {
                        Logger.info("Test flow registered successfully with proxy");
                        notifyTestFlowServerReady(testFlowName, serverType, index);
                    });
        } else {
            // For other servers, just notify they're ready
            notifyTestFlowServerReady(testFlowName, serverType, index);
        }
    }

    private static void notifyTestFlowServerReady(String testFlowName, ServerType serverType, String index) {
        RedisClient.requestProxy(new TestFlowServerReadyProtocol(),
                new TestFlowServerReadyProtocol.Request(testFlowName, serverType.name(), Integer.parseInt(index)))
                .thenAccept(response -> {
                    Logger.info("Notified proxy that " + serverType.name() + " server " + index + " is ready for test flow: " + testFlowName);
                });
    }

    private static Map<String, String> parseOptionalArgs(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].startsWith("--")) {
                options.put(args[i], args[i + 1]);
            }
        }
        return options;
    }
}
