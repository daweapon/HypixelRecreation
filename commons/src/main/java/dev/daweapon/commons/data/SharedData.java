package dev.daweapon.commons.data;

import dev.daweapon.DataAPI;
import dev.daweapon.api.DataAPIImpl;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.commons.redis.RedisConnectionPool;
import dev.daweapon.data.format.JsonFormat;
import dev.daweapon.event.RedisPubSubHandler;
import dev.daweapon.lock.RedisDistributedLock;
import dev.daweapon.storage.RedisDataStorage;
import dev.daweapon.storage.StorageOwnership;
import redis.clients.jedis.JedisPool;

import java.time.Duration;

// Lazily-bootstrapped access point for the shared Redis-backed account/profile DataAPI instances, lock, and connection pool.
public final class SharedData {
    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(5);

    private static JedisPool pool;
    private static RedisDistributedLock lock;
    private static DataAPI account;
    private static DataAPI profile;

    private SharedData() {}

    public static synchronized void bootstrap(String redisUri) {
        if (account != null) return;
        pool = RedisConnectionPool.connect(redisUri, RedisConnectionPool.Settings.standard());
        lock = new RedisDistributedLock(pool);
        account = new DataAPIImpl(new RedisDataStorage(pool, "hsb:acct"), new JsonFormat(),
                new RedisPubSubHandler(pool, "hsb-acct"), true, lock, StorageOwnership.BORROWED, LOCK_TIMEOUT);
        profile = new DataAPIImpl(new RedisDataStorage(pool, "hsb:prof"), new JsonFormat(),
                new RedisPubSubHandler(pool, "hsb-prof"), true, lock, StorageOwnership.BORROWED, LOCK_TIMEOUT);
    }

    public static synchronized void shutdown() {
        if (account instanceof DataAPIImpl accountImpl) accountImpl.shutdown();
        if (profile instanceof DataAPIImpl profileImpl) profileImpl.shutdown();
        if (lock != null) lock.close();
        if (pool != null) pool.close();
    }

    private static void ensureBootstrapped() {
        if (account == null) bootstrap(ConfigProvider.settings().getRedisUri());
    }

    public static DataAPI account() {
        ensureBootstrapped();
        return account;
    }

    public static DataAPI profile() {
        ensureBootstrapped();
        return profile;
    }

    public static RedisDistributedLock lock() {
        ensureBootstrapped();
        return lock;
    }

    public static JedisPool jedisPool() {
        ensureBootstrapped();
        return pool;
    }
}
