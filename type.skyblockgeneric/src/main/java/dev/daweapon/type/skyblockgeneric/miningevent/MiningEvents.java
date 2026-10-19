package dev.daweapon.type.skyblockgeneric.miningevent;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import dev.daweapon.type.skyblockgeneric.commission.CommissionProgress;
import dev.daweapon.type.skyblockgeneric.commission.EventType;
import dev.daweapon.type.skyblockgeneric.commission.Objective;
import dev.daweapon.type.skyblockgeneric.commission.ObjectiveType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public final class MiningEvents {
    private static volatile MiningEvent current;

    private MiningEvents() {
    }

    public static Optional<MiningEvent> current() {
        return Optional.ofNullable(current);
    }

    static void setCurrent(@Nullable MiningEvent event) {
        current = event;
    }

    public static boolean isActive(MiningEventType type) {
        MiningEvent event = current;
        return event != null && event.type() == type;
    }

    public static boolean isActive(EventType type) {
        MiningEvent event = current;
        return event != null && event.type().commissionEvent() == type;
    }

    public static boolean doublePowder() {
        return isActive(MiningEventType.DOUBLE_POWDER);
    }

    public static List<SkyBlockPlayer> players() {
        return MinecraftServer.getConnectionManager().getOnlinePlayers().stream()
                .filter(SkyBlockPlayer.class::isInstance)
                .map(SkyBlockPlayer.class::cast)
                .toList();
    }

    public static void broadcast(String message) {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (player instanceof SkyBlockPlayer skyBlockPlayer) skyBlockPlayer.sendMessage(message);
        }
    }

    public static dev.daweapon.commons.skyblock.statistics.ItemStatistics statistics(SkyBlockPlayer player) {
        return isActive(MiningEventType.BETTER_TOGETHER)
                ? BetterTogetherEvent.statistics(player)
                : dev.daweapon.commons.skyblock.statistics.ItemStatistics.empty();
    }

    public static void participate(SkyBlockPlayer player) {
        CommissionProgress.update(player, ObjectiveType.PARTICIPATE, Objective.BlockTarget.NONE, 1, null);
    }
}
