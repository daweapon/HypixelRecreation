package dev.daweapon.commons.redis;

import dev.daweapon.redisapi.api.ChannelRegistry;
import dev.daweapon.redisapi.api.RedisAPI;
import dev.daweapon.redisapi.api.RedisChannel;
import dev.daweapon.redisapi.api.requests.DataStreamListener;
import dev.daweapon.redisapi.events.EventRegistry;
import dev.daweapon.redisapi.exceptions.ChannelAlreadyRegisteredException;
import org.tinylog.Logger;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Keeps one pub/sub connection subscribed to every registered channel, reconnecting with exponential backoff whenever Redis is unreachable or the connection drops.
public final class RedisSubscriber {
    static final String HEARTBEAT_CHANNEL = "redis-subscriber-heartbeat";
    private static final String DATA_REQUEST_CHANNEL = "internal-data-request";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);

    private static volatile RedisSubscriber active;

    private final RedisAPI api;
    private final Duration initialDelay;
    private final Duration maxDelay;
    private final Duration heartbeatInterval;
    private final Object writeLock = new Object();
    private final ScheduledExecutorService heartbeat;
    private final Thread thread;

    private volatile boolean running = true;
    private volatile ListenerPubSub pubSub;

    private RedisSubscriber(RedisAPI api, Duration initialDelay, Duration maxDelay, Duration heartbeatInterval) {
        this.api = api;
        this.initialDelay = initialDelay;
        this.maxDelay = maxDelay;
        this.heartbeatInterval = heartbeatInterval;
        this.heartbeat = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread heartbeatThread = new Thread(runnable, "redis-subscriber-heartbeat");
            heartbeatThread.setDaemon(true);
            return heartbeatThread;
        });
        this.thread = new Thread(this::run, "redis-subscriber");
        this.thread.setDaemon(true);
    }

    public static RedisSubscriber start() {
        return start(RedisAPI.getInstance(), Duration.ofSeconds(1), Duration.ofSeconds(30), Duration.ofSeconds(15));
    }

    static synchronized RedisSubscriber start(RedisAPI api, Duration initialDelay, Duration maxDelay, Duration heartbeatInterval) {
        try {
            api.registerChannel(DATA_REQUEST_CHANNEL, DataStreamListener.class);
        } catch (ChannelAlreadyRegisteredException ignored) {
        }

        if (active != null) active.stop();

        RedisSubscriber subscriber = new RedisSubscriber(api, initialDelay, maxDelay, heartbeatInterval);
        active = subscriber;
        subscriber.thread.start();
        long interval = heartbeatInterval.toMillis();
        subscriber.heartbeat.scheduleWithFixedDelay(subscriber::syncChannels, interval, interval, TimeUnit.MILLISECONDS);
        return subscriber;
    }

    public static void subscribe(String channel) {
        RedisSubscriber subscriber = active;
        if (subscriber != null) subscriber.send(channel);
    }

    public boolean isConnected() {
        ListenerPubSub current = pubSub;
        return current != null && current.established;
    }

    public void stop() {
        running = false;
        heartbeat.shutdownNow();
        thread.interrupt();
        ListenerPubSub current = pubSub;
        if (current != null) current.close();
        if (active == this) active = null;
    }

    private void run() {
        long delay = initialDelay.toMillis();
        boolean failedBefore = false;

        while (running) {
            ListenerPubSub current = new ListenerPubSub(failedBefore);
            try {
                current.jedis = new Jedis(hostAndPort(), subscriberConfig());
                pubSub = current;
                Set<String> channels = registeredChannels();
                current.requested.addAll(channels);
                current.jedis.subscribe(current, channels.toArray(String[]::new));
                if (running) throw new IllegalStateException("subscription ended");
            } catch (Exception exception) {
                if (!running) break;
                if (current.established) delay = initialDelay.toMillis();
                Logger.warn("Redis listener {} {} ({}); retrying in {} ms",
                        current.established ? "lost its connection to" : "cannot reach",
                        hostAndPort(), describe(exception), delay);
                failedBefore = true;
            } finally {
                pubSub = null;
                current.close();
            }

            try {
                Thread.sleep(delay);
            } catch (InterruptedException interrupted) {
                break;
            }
            delay = Math.min(delay * 2, maxDelay.toMillis());
        }
    }

    private void syncChannels() {
        ListenerPubSub current = pubSub;
        if (current == null || !current.established) return;

        Set<String> missing = registeredChannels();
        missing.removeAll(current.requested);
        missing.add(HEARTBEAT_CHANNEL);
        send(current, missing.toArray(String[]::new));
    }

    private void send(String channel) {
        ListenerPubSub current = pubSub;
        if (current == null || !current.established) return;
        send(current, channel);
    }

    private void send(ListenerPubSub current, String... channels) {
        try {
            synchronized (writeLock) {
                current.requested.addAll(Arrays.asList(channels));
                current.subscribe(channels);
            }
        } catch (Exception exception) {
            Logger.warn("Failed to subscribe to Redis channels {} ({}); reconnecting",
                    Arrays.toString(channels), describe(exception));
            current.close();
        }
    }

    private HostAndPort hostAndPort() {
        return api.getHostAndPort();
    }

    private DefaultJedisClientConfig subscriberConfig() {
        DefaultJedisClientConfig base = api.getClientConfig();
        int connectTimeout = (int) CONNECT_TIMEOUT.toMillis();
        return DefaultJedisClientConfig.builder()
                .ssl(base.isSsl())
                .user(base.getUser())
                .password(base.getPassword())
                .connectionTimeoutMillis(connectTimeout)
                .socketTimeoutMillis(connectTimeout)
                .blockingSocketTimeoutMillis((int) heartbeatInterval.multipliedBy(3).toMillis())
                .build();
    }

    private static Set<String> registeredChannels() {
        Set<String> channels = new LinkedHashSet<>();
        for (RedisChannel channel : ChannelRegistry.registeredChannels.toArray(RedisChannel[]::new)) {
            if (channel != null) channels.add(channel.channelName);
        }
        channels.add(HEARTBEAT_CHANNEL);
        return channels;
    }

    private static String describe(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) root = root.getCause();
        return root == throwable
                ? throwable.toString()
                : throwable.getClass().getSimpleName() + " caused by " + root;
    }

    private final class ListenerPubSub extends JedisPubSub {
        private final Set<String> requested = ConcurrentHashMap.newKeySet();
        private final boolean reconnect;
        private volatile Jedis jedis;
        private volatile boolean established;

        private ListenerPubSub(boolean reconnect) {
            this.reconnect = reconnect;
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
            if (established) return;
            established = true;
            if (reconnect) Logger.info("Redis listener reconnected to {}", hostAndPort());
            syncChannels();
        }

        private void close() {
            Jedis connection = jedis;
            if (connection == null) return;
            try {
                connection.close();
            } catch (Exception ignored) {
            }
        }

        @Override
        public void onMessage(String channel, String message) {
            if (HEARTBEAT_CHANNEL.equals(channel)) return;
            try {
                EventRegistry.handleAll(channel, message);
            } catch (Exception exception) {
                Logger.error(exception, "Failed to handle Redis message on channel {}", channel);
            }
        }
    }
}
