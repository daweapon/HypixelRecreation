package net.swofty.type.skyblockgeneric.accessory.effect;

import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.accessory.AccessoryHandlers;
import net.swofty.type.skyblockgeneric.accessory.AccessoryPowerRule;
import net.swofty.type.skyblockgeneric.accessory.AccessoryRealm;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.item.components.AccessoryComponent;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Builds an {@link AccessoryComponent} from the {@code ACCESSORY} (or {@code TALISMAN}) block of an
 * item YAML. Every error names the item and field so a typo fails loudly in the guard test.
 */
public final class AccessoryComponentParser {
    private AccessoryComponentParser() {
    }

    public static AccessoryComponent parse(String itemId, Map<String, Object> raw) {
        String where = "ACCESSORY[" + itemId + "]";
        boolean recombobulate = !Boolean.FALSE.equals(raw.get("recombobulate"));
        boolean dungeon = Boolean.TRUE.equals(raw.get("dungeon"));
        AccessoryRealm realm = enumValue(AccessoryRealm.class, raw.getOrDefault("only_in", "ANYWHERE"), where + ".only_in");
        AccessoryPowerRule power = enumValue(AccessoryPowerRule.class, raw.getOrDefault("power", "NORMAL"), where + ".power");
        Rarity dynamicMax = raw.get("dynamic_max_rarity") == null ? null
                : enumValue(Rarity.class, raw.get("dynamic_max_rarity"), where + ".dynamic_max_rarity");

        List<AccessoryEffect> effects = new ArrayList<>();
        if (raw.get("effects") instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                effects.add(effect(asMap(list.get(i), where + ".effects[" + i + "]"), where + ".effects[" + i + "]"));
            }
        } else if (raw.containsKey("effects")) {
            throw new IllegalArgumentException(where + ".effects: must be a list");
        }
        return new AccessoryComponent(recombobulate, dungeon, realm, power, dynamicMax, List.copyOf(effects));
    }

    private static AccessoryEffect effect(Map<String, Object> raw, String where) {
        AccessoryEffectKind kind = enumValue(AccessoryEffectKind.class, raw.get("type"), where + ".type");
        List<String> text = strings(raw.get("text"), where + ".text");
        ItemStatistics statistics = statistics(raw.get("statistics"), where + ".statistics");
        AccessoryConditions conditions = conditions(raw, where);
        Set<MobType> mobTypes = enumSet(MobType.class, raw.get("mob_types"), where + ".mob_types");
        double percent = raw.get("percent") instanceof Number n ? n.doubleValue() : 0;
        String handler = raw.get("handler") == null ? null : String.valueOf(raw.get("handler"));
        String reason = raw.get("reason") == null ? null : String.valueOf(raw.get("reason"));

        switch (kind) {
            case STAT -> {
                if (statistics.getOverall().isEmpty()) throw new IllegalArgumentException(where + ": STAT needs statistics");
            }
            case DAMAGE_REDUCTION, DAMAGE_BONUS -> {
                if (percent <= 0) throw new IllegalArgumentException(where + ": " + kind + " needs a positive percent");
            }
            case HANDLER -> {
                if (handler == null) throw new IllegalArgumentException(where + ": HANDLER needs handler");
                if (!AccessoryHandlers.isKnown(handler))
                    throw new IllegalArgumentException(where + ": unknown handler " + handler + ", known " + AccessoryHandlers.ids());
            }
            case TEXT, INACTIVE -> {
                if (text.isEmpty() && kind == AccessoryEffectKind.TEXT)
                    throw new IllegalArgumentException(where + ": TEXT needs text");
            }
        }
        return new AccessoryEffect(kind, text, statistics, conditions, mobTypes, percent, handler, reason);
    }

    private static AccessoryConditions conditions(Map<String, Object> raw, String where) {
        Set<ServerType> servers = enumSet(ServerType.class, raw.get("servers"), where + ".servers");
        Set<RegionType> regions = enumSet(RegionType.class, raw.get("regions"), where + ".regions");
        AccessoryConditions.TimeOfDay time = raw.get("time") == null ? null
                : enumValue(AccessoryConditions.TimeOfDay.class, raw.get("time"), where + ".time");
        if (servers.isEmpty() && regions.isEmpty() && time == null) return AccessoryConditions.ALWAYS;
        return new AccessoryConditions(Set.copyOf(servers), Set.copyOf(regions), time);
    }

    private static ItemStatistics statistics(Object raw, String where) {
        if (raw == null) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (Map.Entry<String, Object> entry : asMap(raw, where).entrySet()) {
            ItemStatistic statistic = enumValue(ItemStatistic.class, entry.getKey(), where + "." + entry.getKey());
            if (!(entry.getValue() instanceof Number number))
                throw new IllegalArgumentException(where + "." + entry.getKey() + ": must be a number");
            builder.withBase(statistic, number.doubleValue());
        }
        return builder.build();
    }

    private static <E extends Enum<E>> Set<E> enumSet(Class<E> type, Object raw, String where) {
        Set<E> values = EnumSet.noneOf(type);
        if (raw == null) return values;
        if (!(raw instanceof List<?> list)) throw new IllegalArgumentException(where + ": must be a list");
        for (int i = 0; i < list.size(); i++) values.add(enumValue(type, list.get(i), where + "[" + i + "]"));
        return values;
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, Object raw, String where) {
        if (raw == null) throw new IllegalArgumentException(where + ": is required");
        try {
            return Enum.valueOf(type, String.valueOf(raw).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(where + ": unknown value " + raw);
        }
    }

    private static List<String> strings(Object raw, String where) {
        if (raw == null) return List.of();
        if (raw instanceof String single) return List.of(single);
        if (!(raw instanceof List<?> list)) throw new IllegalArgumentException(where + ": must be a list of lines");
        return list.stream().map(String::valueOf).toList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object raw, String where) {
        if (raw instanceof Map<?, ?> map) return (Map<String, Object>) map;
        throw new IllegalArgumentException(where + ": must be a map");
    }
}
