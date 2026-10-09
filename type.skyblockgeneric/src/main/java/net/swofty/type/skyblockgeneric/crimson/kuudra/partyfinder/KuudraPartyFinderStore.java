package net.swofty.type.skyblockgeneric.crimson.kuudra.partyfinder;

import net.swofty.api.DataAPIImpl;
import net.swofty.commons.data.SwoftyData;
import net.swofty.dungeons.partyfinder.PartyFinderBoard;
import net.swofty.lock.DistributedLock;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.util.function.Function;

/** Where the Kuudra Party Finder board lives: Redis on a live network, memory in tests. */
public interface KuudraPartyFinderStore {
    PartyFinderBoard<KuudraTier, Void> read();

    /** Reads the board, applies the change and writes it back, with no other server changing it in between. */
    <T> T update(Function<PartyFinderBoard<KuudraTier, Void>, T> change);

    final class Redis implements KuudraPartyFinderStore {
        private static final String KEY = "hsb:partyfinder:kuudra";
        private static final String LOCK = "partyfinder:kuudra";
        private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(5);

        @Override
        public PartyFinderBoard<KuudraTier, Void> read() {
            try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
                return KuudraPartyFinderCodec.decode(jedis.get(KEY));
            }
        }

        @Override
        public <T> T update(Function<PartyFinderBoard<KuudraTier, Void>, T> change) {
            try (DistributedLock.Handle ignored = ((DataAPIImpl) SwoftyData.profile()).lock(LOCK, LOCK_TIMEOUT);
                 Jedis jedis = SwoftyData.jedisPool().getResource()) {
                PartyFinderBoard<KuudraTier, Void> board = KuudraPartyFinderCodec.decode(jedis.get(KEY));
                T result = change.apply(board);
                jedis.set(KEY, KuudraPartyFinderCodec.encode(board));
                return result;
            }
        }
    }

    final class Memory implements KuudraPartyFinderStore {
        private String json = "";

        @Override
        public synchronized PartyFinderBoard<KuudraTier, Void> read() {
            return KuudraPartyFinderCodec.decode(json);
        }

        @Override
        public synchronized <T> T update(Function<PartyFinderBoard<KuudraTier, Void>, T> change) {
            PartyFinderBoard<KuudraTier, Void> board = KuudraPartyFinderCodec.decode(json);
            T result = change.apply(board);
            json = KuudraPartyFinderCodec.encode(board);
            return result;
        }
    }
}
