package net.swofty.type.skyblockgeneric.item.handlers.pet.definition;

import net.swofty.commons.ServerType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityRegistry;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * The {@code when:} block of a pet ability: every listed condition must hold for the player.
 * Named conditions cover checks that are not a plain server, region or health test.
 */
public final class PetConditions {
    private static final Map<String, Predicate<SkyBlockPlayer>> NAMED = new ConcurrentHashMap<>();

    static {
        register("PUBLIC_ISLAND", player -> currentServer() != ServerType.SKYBLOCK_ISLAND);
        register("PRIVATE_ISLAND", player -> currentServer() == ServerType.SKYBLOCK_ISLAND);
        register("THE_PARK", inRegions(EnumSet.of(
                RegionType.BIRCH_PARK, RegionType.HOWLING_CAVE, RegionType.SPRUCE_WOODS, RegionType.VIKING_LONGHOUSE,
                RegionType.DARK_THICKET, RegionType.TRIALS_OF_FIRE, RegionType.SAVANNA_WOODLAND, RegionType.SOUL_CAVE,
                RegionType.MELODY_PLATEAU, RegionType.JUNGLE_ISLAND, RegionType.SPIRIT_CAVE)));
    }

    private PetConditions() {
    }

    public static void register(String id, Predicate<SkyBlockPlayer> condition) {
        NAMED.put(id.toUpperCase(), condition);
    }

    public static boolean isNamed(String id) {
        return NAMED.containsKey(id.toUpperCase());
    }

    public static Predicate<SkyBlockPlayer> parse(@Nullable Object raw, String where) {
        if (raw == null) return _ -> true;
        if (!(raw instanceof Map<?, ?> map)) throw new IllegalArgumentException(where + ": 'when' must be a map");
        List<Predicate<SkyBlockPlayer>> all = new ArrayList<>();
        map.forEach((key, value) -> all.add(switch (String.valueOf(key)) {
            case "servers" -> inServers(names(value).stream().map(ServerType::getSkyblockServer).toList());
            case "regions" -> inRegions(EnumSet.copyOf(names(value).stream().map(RegionType::valueOf).toList()));
            case "health_above" -> healthAbove(((Number) value).doubleValue());
            case "conditions" -> named(names(value), where);
            default -> throw new IllegalArgumentException(where + ": unknown condition '" + key + "'");
        }));
        return player -> {
            if (player == null) return false;
            for (Predicate<SkyBlockPlayer> condition : all) if (!condition.test(player)) return false;
            return true;
        };
    }

    private static Predicate<SkyBlockPlayer> named(List<String> ids, String where) {
        PetAbilityRegistry.ids();
        List<String> upper = ids.stream().map(String::toUpperCase).toList();
        for (String id : upper) {
            if (!NAMED.containsKey(id)) throw new IllegalArgumentException(where + ": unknown named condition '" + id + "'");
        }
        return player -> upper.stream().allMatch(id -> NAMED.get(id).test(player));
    }

    private static Predicate<SkyBlockPlayer> inServers(Collection<ServerType> servers) {
        Set<ServerType> set = EnumSet.copyOf(servers);
        return _ -> set.contains(currentServer());
    }

    public static Predicate<SkyBlockPlayer> inRegions(Set<RegionType> regions) {
        return player -> player.getRegion() != null && regions.contains(player.getRegion().getType());
    }

    private static Predicate<SkyBlockPlayer> healthAbove(double fraction) {
        return player -> player.getHealth() > player.getMaxHealth() * fraction;
    }

    private static @Nullable ServerType currentServer() {
        return HypixelConst.getTypeLoader() == null ? null : HypixelConst.getTypeLoader().getType();
    }

    private static List<String> names(Object raw) {
        if (raw instanceof Collection<?> list) return list.stream().map(String::valueOf).toList();
        return List.of(String.valueOf(raw));
    }
}
