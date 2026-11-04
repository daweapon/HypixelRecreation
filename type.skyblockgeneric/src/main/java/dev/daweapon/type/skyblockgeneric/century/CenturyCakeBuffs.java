package dev.daweapon.type.skyblockgeneric.century;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.data.datapoints.DatapointMapStringLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.playtime.ProfilePlaytime;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class CenturyCakeBuffs {
    public static final long DURATION_MS = 48L * 60 * 60 * 1000;
    public static final long SOUL_MIN_PLAYTIME_MS = 8L * 60 * 60 * 1000;
    public static final double SOUL_CHANCE_PERCENT = 0.1;
    static final String EATEN = "eaten";
    static final String SOULS = "souls";
    static final String BUFF_PREFIX = "buff:";

    private CenturyCakeBuffs() {
    }

    public record Outcome(boolean refreshed, @Nullable SkyBlockItem soul) {
    }

    public static Outcome eat(SkyBlockPlayer player, CenturyCake cake, @Nullable String cakeOwner) {
        long now = System.currentTimeMillis();
        Map<String, Long> state = new HashMap<>(state(player));
        boolean active = isActive(state, cake, now);
        state.put(BUFF_PREFIX + cake.name(), now + DURATION_MS);
        state.merge(EATEN, 1L, Long::sum);
        SkyBlockItem soul = null;
        if (soulEligible(active, ProfilePlaytime.millis(player))
                && LootRoll.roll(player.getUuid(), ItemType.CAKE_SOUL, SOUL_CHANCE_PERCENT)) {
            soul = CakeSoul.create(cake.soulColour(), cakeOwner);
            state.merge(SOULS, 1L, Long::sum);
        }
        datapoint(player).setValue(state);
        apply(player, cake, DURATION_MS);
        player.sendMessage((active ? "<d><l>Big Yum! <r><e>You refresh " : "<d><l>Yum! <r><e>You gain ")
                + cake.bonus() + " <e>for <a>48 <e>hours!");
        if (soul != null) {
            player.addAndUpdateItem(soul);
            player.sendMessage("<d><l>RARE DROP! <r>" + cake.soulColour().tag() + "Cake Soul ("
                    + cake.soulColour().displayName() + ")");
        }
        dev.daweapon.type.skyblockgeneric.century.raffle.CenturyRaffle.progress(player,
                dev.daweapon.type.skyblockgeneric.century.raffle.RaffleAction.of(
                        dev.daweapon.type.skyblockgeneric.century.raffle.RaffleAction.Trigger.EAT_CAKE));
        return new Outcome(active, soul);
    }

    public static void restore(SkyBlockPlayer player) {
        Map<String, Long> state = state(player);
        long now = System.currentTimeMillis();
        for (CenturyCake cake : CenturyCake.values()) {
            long remaining = expiration(state, cake) - now;
            if (remaining > 0) apply(player, cake, remaining);
        }
    }

    public static boolean isActive(Map<String, Long> state, CenturyCake cake, long now) {
        return expiration(state, cake) > now;
    }

    public static boolean soulEligible(boolean buffAlreadyActive, long playtimeMs) {
        return !buffAlreadyActive && playtimeMs > SOUL_MIN_PLAYTIME_MS;
    }

    public static long eaten(SkyBlockPlayer player) {
        return state(player).getOrDefault(EATEN, 0L);
    }

    public static long souls(SkyBlockPlayer player) {
        return state(player).getOrDefault(SOULS, 0L);
    }

    public static long expiration(SkyBlockPlayer player, CenturyCake cake) {
        return expiration(state(player), cake);
    }

    private static long expiration(Map<String, Long> state, CenturyCake cake) {
        return state.getOrDefault(BUFF_PREFIX + cake.name(), 0L);
    }

    private static void apply(SkyBlockPlayer player, CenturyCake cake, long millis) {
        player.getStatistics().removeTemporaryStatistics(statistic -> cake.displayName().equals(statistic.getDisplayName()));
        if (cake.statistic() == null) return;
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(ItemStatistics.builder().withBase(cake.statistic(), (double) cake.amount()).build())
                .withExpirationInMs(millis)
                .withDisplayName(cake.displayName())
                .withDisplayColor("<d>")
                .build());
    }

    private static DatapointMapStringLong datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CENTURY_CAKES, DatapointMapStringLong.class);
    }

    private static Map<String, Long> state(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return Map.of();
        return datapoint(player).getValue();
    }
}
