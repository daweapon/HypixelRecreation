package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class CorpseLootTable {
    private static CorpseLootTable instance;

    public record Entry(ItemType item, @Nullable EnchantmentType book, int bookLevel, int amount, double weight, @Nullable String meterKey) {
    }

    public record MeterDrop(String id, String name, ItemType item, @Nullable EnchantmentType book, int bookLevel, long requiredXp) {
    }

    public record Rules(int minRolls, int maxRolls, long powder, long hotmXp, double dyeChance, long meterXp,
                        @Nullable ItemType key, String pool, double spawnShare) {
    }

    private final Map<CorpseType, Rules> rules = new EnumMap<>(CorpseType.class);
    private final Map<String, List<Entry>> pools = new LinkedHashMap<>();
    private final Map<String, MeterDrop> meter = new LinkedHashMap<>();

    public static synchronized CorpseLootTable get() {
        if (instance == null) instance = parse(CorpseFiles.read("corpse_loot.yml"));
        return instance;
    }

    @SuppressWarnings("unchecked")
    static CorpseLootTable parse(Map<String, Object> root) {
        CorpseLootTable table = new CorpseLootTable();
        Map<String, Map<String, Object>> types = (Map<String, Map<String, Object>>) root.get("types");
        for (CorpseType type : CorpseType.values()) {
            Map<String, Object> raw = types.get(type.name());
            List<Number> rolls = (List<Number>) raw.get("rolls");
            Object key = raw.get("key");
            table.rules.put(type, new Rules(rolls.get(0).intValue(), rolls.get(1).intValue(), ((Number) raw.get("powder")).longValue(),
                    ((Number) raw.get("hotm_xp")).longValue(), ((Number) raw.get("dye_chance")).doubleValue(), ((Number) raw.get("meter_xp")).longValue(),
                    key == null ? null : ItemType.valueOf(key.toString()), raw.get("pool").toString(), ((Number) raw.get("spawn_share")).doubleValue()));
        }
        for (Map<String, Object> raw : (List<Map<String, Object>>) root.get("meter")) {
            MeterDrop drop = new MeterDrop(raw.get("id").toString(), raw.get("name").toString(), ItemType.valueOf(raw.get("item").toString()),
                    book(raw), raw.containsKey("level") ? ((Number) raw.get("level")).intValue() : 0, ((Number) raw.get("required_xp")).longValue());
            table.meter.put(drop.id(), drop);
        }
        Map<String, List<Map<String, Object>>> pools = (Map<String, List<Map<String, Object>>>) root.get("pools");
        pools.forEach((name, rows) -> {
            List<Entry> entries = new ArrayList<>();
            for (Map<String, Object> raw : rows) {
                Object meterKey = raw.get("meter");
                entries.add(new Entry(ItemType.valueOf(raw.get("item").toString()), book(raw), raw.containsKey("level") ? ((Number) raw.get("level")).intValue() : 0,
                        ((Number) raw.get("amount")).intValue(), ((Number) raw.get("weight")).doubleValue(), meterKey == null ? null : meterKey.toString()));
            }
            table.pools.put(name, List.copyOf(entries));
        });
        return table;
    }

    private static @Nullable EnchantmentType book(Map<String, Object> raw) {
        return raw.get("book") == null ? null : EnchantmentType.valueOf(raw.get("book").toString());
    }

    public Rules rules(CorpseType type) {
        return rules.get(type);
    }

    public List<Entry> pool(CorpseType type) {
        return pools.get(rules(type).pool());
    }

    public double totalWeight(CorpseType type) {
        return pool(type).stream().mapToDouble(Entry::weight).sum();
    }

    public List<MeterDrop> meterDrops() {
        return List.copyOf(meter.values());
    }

    public @Nullable MeterDrop meterDrop(@Nullable String id) {
        return id == null ? null : meter.get(id);
    }

    public boolean poolHas(CorpseType type, String meterKey) {
        return pool(type).stream().anyMatch(entry -> meterKey.equals(entry.meterKey()));
    }

    public List<Entry> variants(CorpseType type, String meterKey) {
        return pool(type).stream().filter(entry -> meterKey.equals(entry.meterKey())).toList();
    }

    public Entry roll(@Nullable UUID player, CorpseType type, CorpseRngMeter.Boost boost, RandomGenerator random) {
        return LootRoll.pick(player, pool(type), Entry::item, boost::weight, random);
    }

    public Entry rollVariant(@Nullable UUID player, CorpseType type, String meterKey, RandomGenerator random) {
        return LootRoll.pick(player, variants(type, meterKey), Entry::item, Entry::weight, random);
    }
}
