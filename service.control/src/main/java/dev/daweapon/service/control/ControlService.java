package dev.daweapon.service.control;

import dev.daweapon.service.control.docker.DockerApi;
import dev.daweapon.service.control.deploy.DeployService;
import dev.daweapon.service.control.docker.DockerSocketClient;
import dev.daweapon.service.control.fleet.FleetService;
import dev.daweapon.service.control.fleet.LifecycleController;
import dev.daweapon.service.control.http.ControlRoutes;
import dev.daweapon.service.control.logs.LogTailer;
import dev.daweapon.service.control.watchdog.AlertDispatcher;
import dev.daweapon.service.control.watchdog.Watchdog;
import org.tinylog.Logger;
import spark.Spark;

/**
 * Control-plane agent for the stack.
 *
 * Runs as its own container holding the Docker socket, reachable only on the internal compose
 * network; service.api is the authenticated front door in front of it. It is deliberately kept
 * free of any Redis or Mongo dependency: its job is to report on the stack when the stack is
 * broken, so it must not need the broken parts in order to start.
 */
public final class ControlService {
    private static final int DEFAULT_PORT = 9090;

    public static void main(String[] args) {
        int port = resolvePort(args);

        DockerSocketClient socketClient = new DockerSocketClient();
        DockerApi docker = new DockerApi(socketClient);
        FleetService fleetService = new FleetService(docker);
        LifecycleController lifecycle = new LifecycleController(docker);
        LogTailer logTailer = new LogTailer(docker);
        DeployService deployService = new DeployService(docker, socketClient, fleetService, lifecycle);

        AlertDispatcher alertDispatcher = AlertDispatcher.fromEnvironment();
        Watchdog watchdog = new Watchdog(fleetService, alertDispatcher);

        Logger.info("Control agent starting on port {}", port);
        Logger.info("Docker socket: {} (available: {})",
                socketClient.getSocketPath(), socketClient.isAvailable());

        if (!socketClient.isAvailable()) {
            Logger.warn("Docker socket is NOT available - fleet endpoints will report unreachable. "
                    + "Mount /var/run/docker.sock into this container.");
        }

        Spark.port(port);
        new ControlRoutes(fleetService, lifecycle, logTailer, watchdog, socketClient, deployService).register();
        Spark.awaitInitialization();

        watchdog.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.info("Control agent shutting down");
            watchdog.stop();
            Spark.stop();
        }));

        Logger.info("Deploy drop folder: {} (available: {})",
                deployService.dropDirectory(), deployService.isDropAvailable());
        Logger.info("Control agent ready on port {}", port);
    }

    private static int resolvePort(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--port=")) {
                return Integer.parseInt(arg.substring("--port=".length()));
            }
        }
        String fromEnv = System.getenv("CONTROL_PORT");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return Integer.parseInt(fromEnv.trim());
        }
        return DEFAULT_PORT;
    }
}
