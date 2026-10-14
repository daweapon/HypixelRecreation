package net.swofty.service.generic;

import org.tinylog.Logger;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MongoReadiness {
    private static final Pattern HOSTS = Pattern.compile("^mongodb(?:\\+srv)?://(?:[^@/]*@)?([^/?]+)");
    private static final Duration TIMEOUT = Duration.ofMinutes(5);
    private static final int CONNECT_MILLIS = 2000;
    private static final long RETRY_MILLIS = 2000;

    private MongoReadiness() {}

    public static void await(String connectionString) {
        List<InetSocketAddress> hosts = parseHosts(connectionString);
        if (hosts.isEmpty()) return;

        long deadline = System.nanoTime() + TIMEOUT.toNanos();
        boolean logged = false;
        while (hosts.stream().noneMatch(MongoReadiness::reachable)) {
            if (System.nanoTime() > deadline) {
                Logger.warn("MongoDB still unreachable after {} minutes, continuing anyway", TIMEOUT.toMinutes());
                return;
            }
            if (!logged) {
                Logger.info("Waiting for MongoDB at {}", hosts);
                logged = true;
            }
            try {
                Thread.sleep(RETRY_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    static List<InetSocketAddress> parseHosts(String connectionString) {
        List<InetSocketAddress> hosts = new ArrayList<>();
        if (connectionString == null) return hosts;
        Matcher matcher = HOSTS.matcher(connectionString);
        if (!matcher.find() || connectionString.startsWith("mongodb+srv")) return hosts;
        for (String hostPort : matcher.group(1).split(",")) {
            int colon = hostPort.lastIndexOf(':');
            String host = colon < 0 ? hostPort : hostPort.substring(0, colon);
            int port = colon < 0 ? 27017 : Integer.parseInt(hostPort.substring(colon + 1));
            hosts.add(InetSocketAddress.createUnresolved(host, port));
        }
        return hosts;
    }

    private static boolean reachable(InetSocketAddress unresolved) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(unresolved.getHostString(), unresolved.getPort()), CONNECT_MILLIS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
