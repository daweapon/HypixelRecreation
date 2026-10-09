package net.swofty.commons.redis;

import net.swofty.redisapi.api.ChannelRegistry;
import net.swofty.redisapi.api.RedisAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisSubscriberRecoveryTest {
    private RedisSubscriber subscriber;
    private FakeRedisServer server;

    @AfterEach
    void tearDown() throws IOException {
        if (subscriber != null) subscriber.stop();
        if (server != null) server.close();
    }

    @Test
    void resubscribesAfterRedisComesUpLateAndAfterARestart() throws Exception {
        int port = freePort();
        RedisAPI api = RedisAPI.generateInstance("redis://127.0.0.1:" + port);

        String earlyChannel = "subscriber-test-early-" + UUID.randomUUID();
        BlockingQueue<String> earlyMessages = new LinkedBlockingQueue<>();
        api.registerChannel(earlyChannel, event -> earlyMessages.add(event.message));

        subscriber = RedisSubscriber.start(api, Duration.ofMillis(50), Duration.ofMillis(400), Duration.ofMillis(200));

        String lateChannel = "subscriber-test-late-" + UUID.randomUUID();
        BlockingQueue<String> lateMessages = new LinkedBlockingQueue<>();
        api.registerChannel(lateChannel, event -> lateMessages.add(event.message));
        RedisSubscriber.subscribe(lateChannel);

        Thread.sleep(500);
        assertFalse(subscriber.isConnected());

        server = new FakeRedisServer(port);
        assertTrue(await(subscriber::isConnected, Duration.ofSeconds(10)));
        assertTrue(publishUntilReceived(api, earlyChannel, "after-startup", earlyMessages));
        assertTrue(publishUntilReceived(api, lateChannel, "late-after-startup", lateMessages));

        server.close();
        assertTrue(await(() -> !subscriber.isConnected(), Duration.ofSeconds(10)));
        server = new FakeRedisServer(port);

        assertTrue(await(subscriber::isConnected, Duration.ofSeconds(10)));
        assertTrue(publishUntilReceived(api, earlyChannel, "after-restart", earlyMessages));
        assertTrue(publishUntilReceived(api, lateChannel, "late-after-restart", lateMessages));
        assertTrue(server.subscribedChannels().containsAll(
                Set.of(earlyChannel, lateChannel, RedisSubscriber.HEARTBEAT_CHANNEL, "internal-data-request")));
    }

    @Test
    void reconnectsWhenTheConnectionGoesSilent() throws Exception {
        int port = freePort();
        RedisAPI api = RedisAPI.generateInstance("redis://127.0.0.1:" + port);
        String channel = "subscriber-test-silent-" + UUID.randomUUID();
        BlockingQueue<String> messages = new LinkedBlockingQueue<>();
        api.registerChannel(channel, event -> messages.add(event.message));

        server = new FakeRedisServer(port);
        subscriber = RedisSubscriber.start(api, Duration.ofMillis(50), Duration.ofMillis(400), Duration.ofMillis(200));
        assertTrue(await(subscriber::isConnected, Duration.ofSeconds(10)));

        server.goSilent();
        assertTrue(await(() -> !subscriber.isConnected(), Duration.ofSeconds(10)));
        server.close();
        server = new FakeRedisServer(port);

        assertTrue(await(subscriber::isConnected, Duration.ofSeconds(10)));
        assertTrue(publishUntilReceived(api, channel, "after-silence", messages));
    }

    private static boolean publishUntilReceived(RedisAPI api, String channel, String payload,
                                                BlockingQueue<String> messages) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            try {
                api.publishMessage("all", ChannelRegistry.getFromName(channel), payload).join();
            } catch (Exception ignored) {
            }
            String received = messages.poll(200, TimeUnit.MILLISECONDS);
            if (received != null && received.endsWith(";" + payload)) return true;
        }
        return false;
    }

    private static boolean await(BooleanSupplier condition, Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) return true;
            Thread.sleep(20);
        }
        return condition.getAsBoolean();
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static final class FakeRedisServer implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final List<Socket> clients = new ArrayList<>();
        private final Map<String, Set<Client>> subscribers = new ConcurrentHashMap<>();
        private volatile boolean silent;
        private boolean closed;

        private FakeRedisServer(int port) throws IOException, InterruptedException {
            serverSocket = bind(port);
            Thread.ofVirtual().start(this::accept);
        }

        private static ServerSocket bind(int port) throws IOException, InterruptedException {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (true) {
                ServerSocket socket = new ServerSocket();
                socket.setReuseAddress(true);
                try {
                    socket.bind(new InetSocketAddress("127.0.0.1", port));
                    return socket;
                } catch (IOException previousOwnerStillClosing) {
                    socket.close();
                    if (System.nanoTime() > deadline) throw previousOwnerStillClosing;
                    Thread.sleep(20);
                }
            }
        }

        private void goSilent() {
            silent = true;
        }

        private Set<String> subscribedChannels() {
            return Set.copyOf(subscribers.keySet());
        }

        private void accept() {
            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    synchronized (clients) {
                        if (closed) {
                            socket.close();
                            return;
                        }
                        clients.add(socket);
                    }
                    Thread.ofVirtual().start(() -> serve(new Client(socket)));
                } catch (IOException closed) {
                    return;
                }
            }
        }

        private void serve(Client client) {
            try (client.socket) {
                InputStream input = new BufferedInputStream(client.socket.getInputStream());
                while (true) {
                    List<String> command = readCommand(input);
                    if (command == null) return;
                    if (!silent) handle(client, command);
                }
            } catch (IOException ignored) {
            }
        }

        private void handle(Client client, List<String> command) throws IOException {
            switch (command.getFirst().toUpperCase()) {
                case "HELLO" -> client.write("-ERR unknown command 'HELLO'\r\n");
                case "PING" -> client.write("+PONG\r\n");
                case "SUBSCRIBE" -> {
                    for (String channel : command.subList(1, command.size())) {
                        subscribers.computeIfAbsent(channel, ignored -> ConcurrentHashMap.newKeySet()).add(client);
                        client.channels.add(channel);
                        client.write("*3\r\n" + bulk("subscribe") + bulk(channel) + ":" + client.channels.size() + "\r\n");
                    }
                }
                case "PUBLISH" -> {
                    Set<Client> receivers = subscribers.getOrDefault(command.get(1), Set.of());
                    for (Client receiver : receivers) {
                        receiver.write("*3\r\n" + bulk("message") + bulk(command.get(1)) + bulk(command.get(2)));
                    }
                    client.write(":" + receivers.size() + "\r\n");
                }
                default -> client.write("+OK\r\n");
            }
        }

        private static String bulk(String value) {
            return "$" + value.getBytes(StandardCharsets.UTF_8).length + "\r\n" + value + "\r\n";
        }

        private static List<String> readCommand(InputStream input) throws IOException {
            String header = readLine(input);
            if (header == null) return null;
            int count = Integer.parseInt(header.substring(1));
            List<String> parts = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                int length = Integer.parseInt(readLine(input).substring(1));
                byte[] data = input.readNBytes(length);
                input.readNBytes(2);
                parts.add(new String(data, StandardCharsets.UTF_8));
            }
            return parts;
        }

        private static String readLine(InputStream input) throws IOException {
            StringBuilder line = new StringBuilder();
            int value;
            while ((value = input.read()) != -1) {
                if (value == '\r') {
                    input.read();
                    return line.toString();
                }
                line.append((char) value);
            }
            return null;
        }

        @Override
        public void close() throws IOException {
            synchronized (clients) {
                closed = true;
                serverSocket.close();
                for (Socket socket : clients) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }

        private static final class Client {
            private final Socket socket;
            private final Set<String> channels = ConcurrentHashMap.newKeySet();

            private Client(Socket socket) {
                this.socket = socket;
            }

            private synchronized void write(String reply) throws IOException {
                OutputStream output = socket.getOutputStream();
                output.write(reply.getBytes(StandardCharsets.UTF_8));
                output.flush();
            }
        }
    }
}
