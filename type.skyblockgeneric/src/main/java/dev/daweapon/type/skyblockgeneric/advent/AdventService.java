package dev.daweapon.type.skyblockgeneric.advent;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointString;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AdventService {
    private static final Map<UUID, Integer> PREVIEW = new ConcurrentHashMap<>();

    private AdventService() {
    }

    public static int openDay(SkyBlockPlayer player) {
        Integer preview = PREVIEW.get(player.getUuid());
        if (preview != null) return preview;
        return dayOf(LocalDate.now());
    }

    public static int dayOf(LocalDate date) {
        return date.getMonthValue() == 12 ? Math.min(date.getDayOfMonth(), AdventCalendar.DAYS) : 0;
    }

    public static int year(SkyBlockPlayer player) {
        return LocalDate.now().getYear();
    }

    public static void preview(SkyBlockPlayer player, Integer day) {
        if (day == null) PREVIEW.remove(player.getUuid());
        else PREVIEW.put(player.getUuid(), day);
    }

    public static void forget(SkyBlockPlayer player) {
        PREVIEW.remove(player.getUuid());
    }

    public static Set<Integer> claimed(SkyBlockPlayer player) {
        String raw = player.getDataHandler().get(HypixelDataHandler.Data.ADVENT_CLAIMS, DatapointString.class).getValue();
        return parse(raw, year(player));
    }

    static Set<Integer> parse(String raw, int year) {
        Set<Integer> days = new TreeSet<>();
        if (raw == null || !raw.startsWith(year + ":")) return days;
        for (String day : raw.substring(String.valueOf(year).length() + 1).split(",")) {
            if (!day.isBlank()) days.add(Integer.parseInt(day.trim()));
        }
        return days;
    }

    static String serialize(int year, Set<Integer> days) {
        return year + ":" + String.join(",", days.stream().map(String::valueOf).toList());
    }

    public static void claim(SkyBlockPlayer player, int number) {
        int open = openDay(player);
        if (open == 0) {
            player.sendMessage("<c>The Advent Calendar is only open in December!");
            return;
        }
        if (number > open) {
            player.sendMessage("<c>You can't claim this reward yet!");
            return;
        }
        Set<Integer> claimed = claimed(player);
        if (claimed.contains(number)) {
            player.sendMessage("<c>You have already claimed this reward!");
            return;
        }
        AdventCalendar.Day day = AdventCalendar.day(number);
        claimed.add(number);
        player.getDataHandler().get(HypixelDataHandler.Data.ADVENT_CLAIMS, DatapointString.class).setValue(serialize(year(player), claimed));

        day.gifts().forEach((gift, amount) -> player.addAndUpdateItem(ItemType.get(gift), amount));
        if (day.coins() > 0) player.addCoins(day.coins());
        StringBuilder summary = new StringBuilder();
        day.gifts().forEach((gift, amount) -> summary.append(amount).append("x ").append(StringUtility.toNormalCase(gift)).append(", "));
        summary.append(StringUtility.commaify(day.coins())).append(" coins");
        for (AdventCalendar.RankedItem reward : day.items()) {
            if (player.getRank().isEqualOrHigherThan(reward.rank())) {
                player.addAndUpdateItem(ItemType.get(reward.item()));
                summary.append(", 1x ").append(StringUtility.toNormalCase(reward.item()));
            } else {
                player.sendMessage("<c>{} requires " + reward.rank().prefixMarkup() + "<c>!", StringUtility.toNormalCase(reward.item()));
            }
        }
        player.sendMessage("<a>You claimed the day {} Advent Calendar reward: <f>{}<a>!", number, summary.toString());
        if (!day.unbuilt().isEmpty()) {
            player.sendMessage("<7>The {} from this day can't be given yet.", String.join(" and ", day.unbuilt()));
        }
    }
}
