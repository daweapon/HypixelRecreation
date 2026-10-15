package dev.daweapon.type.skyblockgeneric.dungeons.partyfinder;

import dev.daweapon.api.DataAPIImpl;
import dev.daweapon.commons.data.SharedData;
import dev.daweapon.dungeons.partyfinder.PartyFinderBoard;
import dev.daweapon.lock.DistributedLock;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.util.function.Function;

/** Where the Catacombs Party Finder board lives: Redis on a live network, memory in tests. */
public interface CatacombsPartyFinderStore {
    PartyFinderBoard<CatacombsFloor, Void> read();

    /** Reads the board, applies the change and writes it back, with no other server changing it in between. */
    <T> T update(Function<PartyFinderBoard<CatacombsFloor, Void>, T> change);

    final class Redis implements CatacombsPartyFinderStore {
        private static final String KEY = "hsb:partyfinder:catacombs";
        private static final String LOCK = "partyfinder:catacombs";
        private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(5);

        @Override
        public PartyFinderBoard<CatacombsFloor, Void> read() {
            try (Jedis jedis = SharedData.jedisPool().getResource()) {
                return CatacombsPartyFinderCodec.decode(jedis.get(KEY));
            }
        }

        @Override
        public <T> T update(Function<PartyFinderBoard<CatacombsFloor, Void>, T> change) {
            try (DistributedLock.Handle ignored = ((DataAPIImpl) SharedData.profile()).lock(LOCK, LOCK_TIMEOUT);
                 Jedis jedis = SharedData.jedisPool().getResource()) {
                PartyFinderBoard<CatacombsFloor, Void> board = CatacombsPartyFinderCodec.decode(jedis.get(KEY));
                T result = change.apply(board);
                jedis.set(KEY, CatacombsPartyFinderCodec.encode(board));
                return result;
            }
        }
    }

    final class Memory implements CatacombsPartyFinderStore {
        private String json = "";

        @Override
        public synchronized PartyFinderBoard<CatacombsFloor, Void> read() {
            return CatacombsPartyFinderCodec.decode(json);
        }

        @Override
        public synchronized <T> T update(Function<PartyFinderBoard<CatacombsFloor, Void>, T> change) {
            PartyFinderBoard<CatacombsFloor, Void> board = CatacombsPartyFinderCodec.decode(json);
            T result = change.apply(board);
            json = CatacombsPartyFinderCodec.encode(board);
            return result;
        }
    }
}
