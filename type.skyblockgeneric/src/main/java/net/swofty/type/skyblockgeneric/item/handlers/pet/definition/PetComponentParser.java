package net.swofty.type.skyblockgeneric.item.handlers.pet.definition;

import net.minestom.server.entity.EntityType;
import net.minestom.server.particle.Particle;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.KatUpgrade;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityRegistry;
import net.swofty.type.skyblockgeneric.minion.MinionRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Builds a {@link PetComponent} from the {@code PET} component block of an item YAML.
 */
public final class PetComponentParser {
    private static final int DEFAULT_PARTICLE = 3;

    private PetComponentParser() {
    }

    public static PetComponent parse(Map<String, Object> raw) {
        String petName = string(raw, "pet_name", null);
        if (petName == null) throw new IllegalArgumentException("PET: pet_name is required");
        String where = "PET[" + petName + "]";

        PetType type = PetType.parse(string(raw, "pet_type", string(raw, "skill_category", "COMBAT")));
        List<Rarity> rarities = rarities(raw, where);
        int maxLevel = integer(raw, "max_level", 100);
        int statsBaseLevel = integer(raw, "stats_base_level", 0);
        Integer xpOffset = raw.get("xp_offset") instanceof Number n ? n.intValue() : null;
        boolean fixedLevel = Boolean.TRUE.equals(raw.get("fixed_level"));
        boolean petScore = !Boolean.FALSE.equals(raw.get("pet_score"));
        boolean passive = Boolean.TRUE.equals(raw.get("passive"));

        PetStatTable statistics = raw.containsKey("statistics")
                ? PetStatTable.parse(raw.get("statistics"), where + ".statistics")
                : legacyStatistics(raw, where);

        List<ConfiguredPetAbility> abilities = new ArrayList<>();
        Object abilityList = raw.get("abilities");
        if (abilityList instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                abilities.add(ability(asMap(list.get(i), where + ".abilities[" + i + "]"), rarities.getFirst(),
                        statsBaseLevel, where + ".abilities[" + i + "]"));
            }
        }

        return new PetComponent(petName, type, rarities, maxLevel, statsBaseLevel, xpOffset, fixedLevel, petScore, passive,
                RarityNumbers.parse(upperKeys(raw.get("george_price")), where + ".george_price"),
                katUpgrades(raw.get("kat_upgrades"), where),
                statistics, abilities, particle(raw.get("particle")),
                string(raw, "skull_texture", ""), texturesByRarity(raw.get("skull_texture_by_rarity"), where))
                .withSource(PetSource.parse(raw.get("source"), where + ".source"));
    }

    private static List<Rarity> rarities(Map<String, Object> raw, String where) {
        List<Rarity> rarities = new ArrayList<>();
        if (raw.get("rarities") instanceof Collection<?> list) {
            for (Object name : list) rarities.add(rarity(name, where + ".rarities"));
        } else if (raw.get("per_level_statistics") instanceof Map<?, ?> legacy) {
            for (Object key : legacy.keySet()) {
                Rarity rarity = Rarity.getRarity(String.valueOf(key));
                if (rarity != null && rarity.ordinal() <= Rarity.MYTHIC.ordinal()) rarities.add(rarity);
            }
        }
        if (rarities.isEmpty()) throw new IllegalArgumentException(where + ": rarities is required");
        rarities.sort(null);
        return rarities;
    }

    private static PetStatTable legacyStatistics(Map<String, Object> raw, String where) {
        Map<String, Object> base = raw.get("base_statistics") instanceof Map<?, ?> m ? castMap(m) : Map.of();
        if (!(raw.get("per_level_statistics") instanceof Map<?, ?> perLevel)) return PetStatTable.EMPTY;
        EnumMap<Rarity, Map<ItemStatistic, PetValue>> blocks = new EnumMap<>(Rarity.class);
        perLevel.forEach((rarityKey, stats) -> {
            Rarity rarity = Rarity.getRarity(String.valueOf(rarityKey));
            if (rarity == null || !(stats instanceof Map<?, ?> statMap)) return;
            Map<ItemStatistic, PetValue> block = new LinkedHashMap<>();
            Set<Object> keys = new java.util.LinkedHashSet<>(statMap.keySet());
            keys.addAll(base.keySet());
            for (Object key : keys) {
                double flat = base.get(String.valueOf(key)) instanceof Number n ? n.doubleValue() : 0;
                double step = statMap.get(key) instanceof Number n ? n.doubleValue() : 0;
                block.put(ItemStatistic.valueOf(String.valueOf(key).toUpperCase()),
                        new PetValue(RarityNumbers.constant(flat), RarityNumbers.constant(step), null, null));
            }
            blocks.put(rarity, block);
        });
        return new PetStatTable(blocks);
    }

    private static ConfiguredPetAbility ability(Map<String, Object> raw, Rarity petMin, int baseLevel, String where) {
        String name = string(raw, "name", null);
        if (name == null) throw new IllegalArgumentException(where + ": name is required");
        where = where + "(" + name + ")";

        Rarity minRarity = raw.containsKey("rarity") ? rarity(raw.get("rarity"), where + ".rarity") : petMin;
        Rarity maxRarity = raw.containsKey("max_rarity") ? rarity(raw.get("max_rarity"), where + ".max_rarity") : null;
        PetAbilityKind kind = raw.containsKey("kind")
                ? PetAbilityKind.valueOf(String.valueOf(raw.get("kind")).trim().toUpperCase())
                : PetAbilityKind.INACTIVE;
        String unavailable = string(raw, "unavailable", null);
        if (unavailable != null) kind = PetAbilityKind.INACTIVE;

        List<PetValue> values = new ArrayList<>();
        if (raw.get("values") instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) values.add(PetValue.parse(list.get(i), where + ".values[" + i + "]"));
        }
        int valueCount = values.size();

        List<String> text = lines(raw.get("text"));
        Map<Rarity, List<String>> textByRarity = new EnumMap<>(Rarity.class);
        if (raw.get("text_by_rarity") instanceof Map<?, ?> byRarity) {
            String finalWhere = where;
            byRarity.forEach((key, lines) -> textByRarity.put(rarity(key, finalWhere + ".text_by_rarity"), lines(lines)));
        }

        Map<ItemStatistic, ValueRef> statistics = new LinkedHashMap<>();
        Object statsRaw = raw.get("statistics");
        if (statsRaw instanceof Map<?, ?> map) {
            String finalWhere = where;
            map.forEach((stat, ref) -> statistics.put(ItemStatistic.valueOf(String.valueOf(stat).toUpperCase()),
                    ValueRef.parse(ref, valueCount, finalWhere + ".statistics." + stat)));
        } else if (statsRaw instanceof Collection<?> list) {
            for (Object stat : list) {
                statistics.put(ItemStatistic.valueOf(String.valueOf(stat).toUpperCase()), new ValueRef(0, 0));
            }
        }

        ConfiguredPetAbility.PerStat perStat = null;
        if (raw.get("per_stat") instanceof Map<?, ?> map) {
            Map<String, Object> p = castMap(map);
            perStat = new ConfiguredPetAbility.PerStat(
                    ItemStatistic.valueOf(string(p, "grants", "").toUpperCase()),
                    ValueRef.parse(p.getOrDefault("amount", "{0}"), valueCount, where + ".per_stat.amount"),
                    ItemStatistic.valueOf(string(p, "per", "").toUpperCase()),
                    ValueRef.parse(p.getOrDefault("every", 1), valueCount, where + ".per_stat.every"),
                    p.get("above") instanceof Number n ? n.doubleValue() : 0,
                    p.get("max") instanceof Number n ? n.doubleValue() : null);
        }

        if (((Map<?, ?>) raw).containsKey(Boolean.TRUE)) {
            throw new IllegalArgumentException(where + ": YAML reads the key 'on' as true; use 'triggers:'");
        }
        Set<PetTrigger> triggers = PetTrigger.parse(raw.get("triggers"), where + ".triggers");
        List<ItemType> items = new ArrayList<>();
        for (String item : lines(raw.get("items"))) items.add(ItemType.valueOf(item.trim().toUpperCase()));
        ValueRef chance = raw.containsKey("chance") ? ValueRef.parse(raw.get("chance"), valueCount, where + ".chance") : null;

        Set<EntityType> mobs = new java.util.HashSet<>();
        for (String mob : lines(raw.get("mobs"))) {
            EntityType entityType = EntityType.fromKey(mob.contains(":") ? mob : "minecraft:" + mob.toLowerCase());
            if (entityType == null) throw new IllegalArgumentException(where + ".mobs: unknown entity '" + mob + "'");
            mobs.add(entityType);
        }

        Set<String> minions = new java.util.HashSet<>();
        for (String minion : lines(raw.get("minions"))) {
            String minionName = minion.trim().toUpperCase();
            try {
                MinionRegistry.valueOf(minionName);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(where + ".minions: unknown minion '" + minion + "'");
            }
            minions.add(minionName);
        }

        Predicate<SkyBlockPlayer> condition = PetConditions.parse(raw.get("when"), where + ".when");

        String handlerId = string(raw, "handler", null);
        PetAbilityHandler handler = null;
        if (kind == PetAbilityKind.HANDLER) {
            handler = handlerId == null ? null : PetAbilityRegistry.get(handlerId);
            if (handler == null) {
                Logger.warn("{}: pet ability handler '{}' is not registered; showing it as not active", where, handlerId);
            }
        }

        validate(kind, statistics, perStat, triggers, items, chance, valueCount, where);
        if (kind == PetAbilityKind.MINION_SPEED) require(!minions.isEmpty() && valueCount > 0, where, "needs minions and a value");

        return new ConfiguredPetAbility(name, minRarity, maxRarity, text, textByRarity, values, kind, statistics,
                perStat, triggers, items, chance, mobs, minions, condition, handlerId, handler, baseLevel, unavailable);
    }

    private static void validate(PetAbilityKind kind, Map<ItemStatistic, ValueRef> statistics,
                                 ConfiguredPetAbility.PerStat perStat, Set<PetTrigger> triggers, List<ItemType> items,
                                 ValueRef chance, int valueCount, String where) {
        switch (kind) {
            case STAT, PERCENT_STAT -> require(!statistics.isEmpty(), where, "needs statistics");
            case STAT_PER_STAT -> require(perStat != null, where, "needs per_stat");
            case DROP -> {
                require(!items.isEmpty() && chance != null, where, "needs items and chance");
                require(!triggers.isEmpty(), where, "needs triggers");
            }
            case DAMAGE_REDUCTION -> {
                require(valueCount > 0, where, "needs a value (percent) at index 0");
                require(!triggers.isEmpty() && EnumSet.of(PetTrigger.DAMAGED_BY_MOB, PetTrigger.FALL_DAMAGE)
                        .containsAll(triggers), where, "triggers must be DAMAGED_BY_MOB and/or FALL_DAMAGE");
            }
            default -> {
            }
        }
    }

    private static void require(boolean ok, String where, String message) {
        if (!ok) throw new IllegalArgumentException(where + ": " + message);
    }

    private static EnumMap<Rarity, KatUpgrade> katUpgrades(Object raw, String where) {
        EnumMap<Rarity, KatUpgrade> upgrades = new EnumMap<>(Rarity.class);
        if (!(raw instanceof Map<?, ?> map)) return upgrades;
        map.forEach((key, value) -> {
            Rarity target = Rarity.getRarity(String.valueOf(key));
            if (target == null || !(value instanceof Map<?, ?> entry)) return;
            Map<String, Object> e = castMap(entry);
            long time = e.get("time_seconds") instanceof Number s ? s.longValue() * 1000
                    : e.get("time") instanceof Number ms ? ms.longValue() : 0;
            long coins = e.get("coins") instanceof Number c ? c.longValue() : 0;
            List<KatUpgrade.ItemCost> items = new ArrayList<>();
            if (e.get("items") instanceof Map<?, ?> itemMap) {
                itemMap.forEach((item, amount) -> items.add(new KatUpgrade.ItemCost(
                        ItemType.valueOf(String.valueOf(item).toUpperCase()), ((Number) amount).intValue())));
            } else if (e.get("item") != null) {
                items.add(new KatUpgrade.ItemCost(ItemType.valueOf(String.valueOf(e.get("item")).toUpperCase()),
                        e.get("amount") instanceof Number a ? a.intValue() : 1));
            }
            upgrades.put(target, new KatUpgrade(time, coins, List.copyOf(items)));
        });
        return upgrades;
    }

    private static EnumMap<Rarity, String> texturesByRarity(Object raw, String where) {
        EnumMap<Rarity, String> textures = new EnumMap<>(Rarity.class);
        if (raw instanceof Map<?, ?> map) {
            map.forEach((key, value) -> textures.put(rarity(key, where + ".skull_texture_by_rarity"), String.valueOf(value)));
        }
        return textures;
    }

    private static Particle particle(Object raw) {
        if (raw instanceof Number id) return Particle.fromId(id.intValue());
        if (raw instanceof String key) {
            Particle particle = Particle.fromKey(key.contains(":") ? key : "minecraft:" + key.toLowerCase());
            if (particle != null) return particle;
        }
        return Particle.fromId(DEFAULT_PARTICLE);
    }

    private static Rarity rarity(Object raw, String where) {
        Rarity rarity = Rarity.getRarity(String.valueOf(raw).trim());
        if (rarity == null) throw new IllegalArgumentException(where + ": unknown rarity '" + raw + "'");
        return rarity;
    }

    private static Object upperKeys(Object raw) {
        if (!(raw instanceof Map<?, ?> map)) return raw;
        Map<String, Object> upper = new LinkedHashMap<>();
        map.forEach((key, value) -> {
            String name = String.valueOf(key).toUpperCase();
            if (!name.equals("REST")) upper.put(name, value);
        });
        return upper;
    }

    private static List<String> lines(Object raw) {
        if (raw == null) return List.of();
        if (raw instanceof Collection<?> list) return list.stream().map(String::valueOf).toList();
        return List.of(String.valueOf(raw));
    }

    private static String string(Map<String, Object> raw, String key, String fallback) {
        Object value = raw.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private static int integer(Map<String, Object> raw, String key, int fallback) {
        return raw.get(key) instanceof Number n ? n.intValue() : fallback;
    }

    private static Map<String, Object> asMap(Object raw, String where) {
        if (!(raw instanceof Map<?, ?> map)) throw new IllegalArgumentException(where + ": expected a map");
        return castMap(map);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }
}
