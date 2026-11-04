package dev.daweapon.type.skyblockgeneric.potion.mixin;

import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.data.datapoints.DatapointMapStringLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.potion.GodPotionService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Mixins consumed straight from the inventory (wiki: Mixins, Consuming): needs an active Booster Cookie and God
 * Potion, lasts three times a God Potion's duration, stacks, and only counts down while a God Potion is running.
 * Each Mixin's remaining time is stored in milliseconds of God Potion time.
 */
public final class Mixins {
    public static final int GOD_POTIONS_PER_MIXIN = 3;

    private Mixins() {
    }

    public static void consume(SkyBlockPlayer player, SkyBlockItem item) {
        MixinType type = MixinType.byItemId(item.getAttributeHandler().getPotentialType().name());
        if (type == null) return;
        if (!type.isAvailable()) {
            player.sendMessage("<c>This Mixin can't be consumed yet: " + type.getUnavailableReason() + ".");
            return;
        }
        if (!type.meetsRequirement(player)) {
            player.sendMessage("<c>You need " + type.getRequirementName() + " to consume this Mixin!");
            return;
        }
        if (!player.isBoosterCookieActive()) {
            player.sendMessage("<c>You need an active Booster Cookie to consume Mixins!");
            return;
        }
        if (!GodPotionService.isActive(player)) {
            player.sendMessage("<c>You need an active God Potion to consume Mixins!");
            return;
        }
        long added = GOD_POTIONS_PER_MIXIN * GodPotionService.durationFor(player);
        Map<String, Long> updated = new HashMap<>(stored(player));
        long total = updated.getOrDefault(type.name(), 0L) + added;
        updated.put(type.name(), total);
        save(player, updated);
        player.setItemInMainHand(player.getItemInMainHand().withAmount(player.getItemInMainHand().amount() - 1));
        player.sendMessage("<a><l>SIP! <e>The <9>" + type.getGodPotionName() + " <e>Mixin is active for <b>"
                + total / 3_600_000 + "h " + (total / 60_000) % 60 + "m <e>of God Potion time!");
    }

    public static void elapse(SkyBlockPlayer player, long godPotionMillis) {
        if (godPotionMillis <= 0 || player.getSkyblockDataHandler() == null) return;
        Map<String, Long> current = stored(player);
        if (current.isEmpty()) return;
        Map<String, Long> updated = new HashMap<>();
        current.forEach((id, remaining) -> {
            long left = remaining - godPotionMillis;
            if (left > 0) updated.put(id, left);
        });
        save(player, updated);
    }

    public static long remaining(SkyBlockPlayer player, MixinType type) {
        if (player.getSkyblockDataHandler() == null) return 0;
        long stored = stored(player).getOrDefault(type.name(), 0L);
        if (stored <= 0) return 0;
        long godRemaining = GodPotionService.peekRemaining(player);
        if (godRemaining <= 0) return 0;
        return Math.max(0, stored - GodPotionService.peekElapsed(player));
    }

    public static boolean isActive(SkyBlockPlayer player, MixinType type) {
        return remaining(player, type) > 0;
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null || stored(player).isEmpty()) return ItemStatistics.empty();
        ItemStatistics total = ItemStatistics.empty();
        for (MixinType type : MixinType.values()) {
            if (isActive(player, type)) total = ItemStatistics.add(total, type.getStatistics().apply(player));
        }
        return total;
    }

    public static double chocolateMultiplier(SkyBlockPlayer player) {
        return isActive(player, MixinType.HOT_CHOCOLATE_MIXIN) ? MixinType.HOT_CHOCOLATE_MULTIPLIER : 0;
    }

    public static double hotspotChance(SkyBlockPlayer player) {
        return isActive(player, MixinType.HOTSPOT_TONIC_MIXIN) ? MixinType.HOTSPOT_TONIC_CHANCE : 0;
    }

    private static Map<String, Long> stored(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.GOD_POTION_MIXINS, DatapointMapStringLong.class).getValue();
    }

    private static void save(SkyBlockPlayer player, Map<String, Long> values) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.GOD_POTION_MIXINS, DatapointMapStringLong.class).setValue(values);
    }
}
