package dev.daweapon.type.skyblockgeneric.hunting.npc;

import dev.daweapon.commons.YamlFileUtils;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class HuntingShardShops {
    public static final File FILE = new File("./configuration/skyblock/hunting/shard_shops.yml");
    private static final Map<String, Shop> CACHE = new ConcurrentHashMap<>();

    private HuntingShardShops() {
    }

    public record Offer(String shardId, long coins, int stock) {
    }

    public record Shop(String id, String title, int[] slots, int contactBonus, List<Offer> offers) {
        public int stockFor(Offer offer, boolean hasContact) {
            return offer.stock() + (hasContact ? contactBonus : 0);
        }
    }

    public static Shop get(String id) {
        return CACHE.computeIfAbsent(id, key -> load(FILE, key));
    }

    @SuppressWarnings("unchecked")
    public static Shop load(File file, String id) {
        try {
            Map<String, Object> shops = (Map<String, Object>) YamlFileUtils.loadYaml(file).get("shops");
            Map<String, Object> raw = (Map<String, Object>) shops.get(id);
            if (raw == null) throw new IllegalArgumentException("Unknown hunting shard shop " + id);
            List<Offer> offers = ((List<Map<String, Object>>) raw.get("offers")).stream()
                    .map(entry -> new Offer(String.valueOf(entry.get("shard")), ((Number) entry.get("coins")).longValue(),
                            ((Number) entry.get("stock")).intValue()))
                    .toList();
            int[] slots = raw.get("slots") instanceof List<?> list
                    ? list.stream().mapToInt(value -> ((Number) value).intValue()).toArray()
                    : ShopView.DEFAULT;
            int bonus = raw.get("contactBonus") instanceof Number number ? number.intValue() : 0;
            return new Shop(id, String.valueOf(raw.getOrDefault("title", id)), slots, bonus, offers);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
