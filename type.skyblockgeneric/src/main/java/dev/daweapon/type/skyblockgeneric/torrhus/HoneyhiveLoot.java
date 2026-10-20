package dev.daweapon.type.skyblockgeneric.torrhus;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class HoneyhiveLoot {
    public static final int MIN_HONEYCOMB = 16;
    public static final int MAX_HONEYCOMB = 64;

    private HoneyhiveLoot() {
    }

    public enum Kind {
        SMALL_COINS(100), ENCHANTED_HONEYCOMB(200), LARGE_COINS(50), HONEYBUZZ_SHARD(120), QUEEN_BEE_SHARD(10);

        private final int weight;

        Kind(int weight) {
            this.weight = weight;
        }

        public int weight() {
            return weight;
        }

        public @Nullable String shard() {
            return switch (this) {
                case HONEYBUZZ_SHARD -> "Honeybuzz";
                case QUEEN_BEE_SHARD -> "Queen Bee";
                default -> null;
            };
        }

        public @Nullable ItemType item() {
            if (this == ENCHANTED_HONEYCOMB) return ItemType.ENCHANTED_HONEYCOMB;
            String shard = shard();
            if (shard == null) return null;
            return AttributeRegistry.findByShard(shard).map(AttributeShardComponent::itemType).orElse(null);
        }

        public @Nullable AttributeDefinition definition() {
            String shard = shard();
            return shard == null ? null : AttributeRegistry.findByShard(shard).orElse(null);
        }

        public int rollCoins(RandomGenerator random) {
            return switch (this) {
                case SMALL_COINS -> random.nextInt(1000, 2501);
                case LARGE_COINS -> random.nextInt(2500, 5001);
                default -> 0;
            };
        }
    }

    public static Kind pick(@Nullable UUID player, RandomGenerator random) {
        return LootRoll.pick(player, List.of(Kind.values()), Kind::item, Kind::weight, random);
    }

    public static int honeycomb(RandomGenerator random) {
        return random.nextInt(MIN_HONEYCOMB, MAX_HONEYCOMB + 1);
    }
}
