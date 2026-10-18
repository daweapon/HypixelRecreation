package dev.daweapon.type.skyblockgeneric.slayer.inferno;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Blazetekk Ham Radio: a radio in the inventory tuned to one of nine channels. Exactly two players
 * within range on the same channel each get the full buff, a lone player gets half and three or more
 * lose the signal.
 */
public final class BlazetekkHamRadio {
    public static final List<String> CHANNELS = List.of("610 AM", "S&R 40.5 FM", "SETI.930Mhz", "1512 AM",
        "Bluetooth 2.48 GHz", "2325 XM", "Infrared", "GPS.1227Mhz", "103.5 FM");
    public static final double RANGE = 110;
    public static final double DEFENSE = 60;
    public static final double TRUE_DEFENSE = 40;
    public static final double COMBAT_WISDOM = 5;
    public static final double VITALITY = 10;
    private static final String ITEM_ID = "BLAZETEKK_HAM_RADIO";
    private static final String BUFF_NAME = "Ham Radio";
    private static final long BUFF_LINGER_MS = 1_500;

    public enum Signal { WEAK, STRONG, LOST }

    private static final Map<UUID, Signal> SIGNALS = new ConcurrentHashMap<>();

    private BlazetekkHamRadio() {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            SkyBlockGenericLoader.forEachLoadedPlayer(BlazetekkHamRadio::tick);
            return TaskSchedule.seconds(1);
        }, ExecutionType.TICK_END);
    }

    public static Signal signal(int otherPlayersOnChannel) {
        if (otherPlayersOnChannel <= 0) return Signal.WEAK;
        return otherPlayersOnChannel == 1 ? Signal.STRONG : Signal.LOST;
    }

    public static double statMultiplier(Signal signal) {
        return switch (signal) {
            case WEAK -> 0.5;
            case STRONG -> 1;
            case LOST -> 0;
        };
    }

    public static int nextChannel(int channel) {
        return (Math.floorMod(channel, CHANNELS.size()) + 1) % CHANNELS.size();
    }

    public static String channelName(int channel) {
        return CHANNELS.get(Math.floorMod(channel, CHANNELS.size()));
    }

    public static ItemStatistics statistics(Signal signal) {
        double multiplier = statMultiplier(signal);
        return ItemStatistics.builder()
            .withBase(ItemStatistic.DEFENSE, DEFENSE * multiplier)
            .withBase(ItemStatistic.TRUE_DEFENSE, TRUE_DEFENSE * multiplier)
            .withBase(ItemStatistic.COMBAT_WISDOM, COMBAT_WISDOM * multiplier)
            .withBase(ItemStatistic.VITALITY, VITALITY * multiplier)
            .build();
    }

    public static @Nullable SkyBlockItem radioOf(SkyBlockPlayer player) {
        for (ItemStack stack : player.getInventory().getItemStacks()) {
            if (stack.isAir()) continue;
            SkyBlockItem item = new SkyBlockItem(stack);
            if (ITEM_ID.equals(item.getAttributeHandler().getTypeAsString())) return item;
        }
        return null;
    }

    private static void tick(SkyBlockPlayer player) {
        SkyBlockItem radio = radioOf(player);
        if (radio == null) {
            if (SIGNALS.remove(player.getUuid()) != null) clearBuff(player);
            return;
        }
        int channel = radio.getAttributeHandler().getRadioChannel();
        Signal signal = signal(othersOnChannel(player, channel));
        clearBuff(player);
        if (statMultiplier(signal) > 0) {
            player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(statistics(signal))
                .withExpirationInMs(BUFF_LINGER_MS)
                .withDisplayName(BUFF_NAME)
                .build());
        }
        if (SIGNALS.put(player.getUuid(), signal) != signal) announce(player, signal);
    }

    private static int othersOnChannel(SkyBlockPlayer player, int channel) {
        if (player.getInstance() == null) return 0;
        int others = 0;
        for (var other : player.getInstance().getPlayers()) {
            if (other == player || !(other instanceof SkyBlockPlayer neighbour)) continue;
            if (neighbour.getPosition().distance(player.getPosition()) > RANGE) continue;
            SkyBlockItem radio = radioOf(neighbour);
            if (radio != null && Math.floorMod(radio.getAttributeHandler().getRadioChannel(), CHANNELS.size())
                == Math.floorMod(channel, CHANNELS.size())) others++;
        }
        return others;
    }

    private static void clearBuff(SkyBlockPlayer player) {
        player.getStatistics().removeTemporaryStatistics(stat -> BUFF_NAME.equals(stat.getDisplayName()));
    }

    private static void announce(SkyBlockPlayer player, Signal signal) {
        player.sendMessage(switch (signal) {
            case WEAK -> "<c>Your radio is weak. Find another enjoyer to boost it.";
            case STRONG -> "<a>Your radio signal is strong!";
            case LOST -> "<c>Your radio lost signal. There's too many enjoyers on this channel.";
        });
    }
}
