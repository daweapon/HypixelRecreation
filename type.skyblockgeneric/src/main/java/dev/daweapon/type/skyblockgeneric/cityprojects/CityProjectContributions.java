package dev.daweapon.type.skyblockgeneric.cityprojects;

import dev.daweapon.commons.data.SharedData;
import dev.daweapon.type.generic.data.datapoints.DatapointMapStringLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import redis.clients.jedis.Jedis;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * Records City Project contributions. The community total is kept in Redis so every server sees
 * the same count; each player's own contributions live on their profile. Each component accepts
 * one contribution per player per day, up to 24, following the wiki's daily-contribution rule.
 */
public final class CityProjectContributions {
    public static final int MAX_PER_COMPONENT = 24;
    private static final String GLOBAL_KEY = "city_project:";

    public enum Result {CONTRIBUTED, ALREADY_TODAY, CAPPED, CANNOT_AFFORD}

    private CityProjectContributions() {
    }

    public static long global(CityProject project, int component) {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            String value = jedis.get(GLOBAL_KEY + project.name() + ":" + component);
            return value == null ? 0 : Long.parseLong(value);
        }
    }

    public static long globalTotal(CityProject project) {
        long total = 0;
        for (int i = 0; i < project.getComponents().size(); i++) total += global(project, i);
        return total;
    }

    public static long personal(SkyBlockPlayer player, CityProject project, int component) {
        return read(player).getOrDefault(project.name() + ":" + component, 0L);
    }

    public static long personalTotal(SkyBlockPlayer player, CityProject project) {
        long total = 0;
        for (int i = 0; i < project.getComponents().size(); i++) total += personal(player, project, i);
        return total;
    }

    public static boolean contributedToday(SkyBlockPlayer player, CityProject project, int component) {
        return read(player).getOrDefault(project.name() + ":" + component + ":day", -1L) == today();
    }

    public static boolean canAfford(SkyBlockPlayer player, CityProject.Component component) {
        if (player.getBits() < component.bits()) return false;
        return component.costs().stream().allMatch(cost ->
                player.getAmountInInventory(cost.item()) + player.getSackItems().getAmount(cost.item()) >= cost.amount());
    }

    public static Result contribute(SkyBlockPlayer player, CityProject project, int index) {
        CityProject.Component component = project.getComponents().get(index);
        if (personal(player, project, index) >= MAX_PER_COMPONENT) return Result.CAPPED;
        if (contributedToday(player, project, index)) return Result.ALREADY_TODAY;
        if (!canAfford(player, component)) return Result.CANNOT_AFFORD;

        component.costs().forEach(cost -> player.removeItemFromPlayer(cost.item(), cost.amount()));
        if (component.bits() > 0) player.setBits(player.getBits() - component.bits());
        player.addFame(component.fame());

        Map<String, Long> data = new HashMap<>(read(player));
        long before = personalTotal(player, project);
        data.merge(project.name() + ":" + index, 1L, Long::sum);
        data.put(project.name() + ":" + index + ":day", today());
        player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.CITY_PROJECT_CONTRIBUTIONS, DatapointMapStringLong.class)
                .setValue(data);

        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            jedis.incr(GLOBAL_KEY + project.name() + ":" + index);
        }

        if (before < project.getMementoContributions() && before + 1 >= project.getMementoContributions()) {
            player.addAndUpdateItem(project.getMemento());
        }
        return Result.CONTRIBUTED;
    }

    private static Map<String, Long> read(SkyBlockPlayer player) {
        Map<String, Long> raw = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.CITY_PROJECT_CONTRIBUTIONS, DatapointMapStringLong.class).getValue();
        Map<String, Long> normalised = new HashMap<>();
        // Jackson reads small numbers back as Integer despite the declared Long values.
        raw.forEach((key, value) -> normalised.put(key, ((Number) value).longValue()));
        return normalised;
    }

    private static long today() {
        return LocalDate.now(ZoneOffset.UTC).toEpochDay();
    }
}
