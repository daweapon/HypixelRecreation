package net.swofty.type.crystalhollows.lifecycle;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.text.Text;
import net.swofty.type.crystalhollows.layout.CrystalHollowsLayout;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.crystalhollows.FairyGrottoFishing;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CrystalHollowsLobby {
    private static final List<Long> WARNING_MINUTES = List.of(5L, 1L);

    private static final Set<UUID> ADMITTED = ConcurrentHashMap.newKeySet();
    private static volatile long openedAt = System.currentTimeMillis();
    private static volatile long lastWarnedMinutes = Long.MAX_VALUE;
    private static boolean started;

    private CrystalHollowsLobby() {
    }

    public static synchronized void start() {
        if (started) return;
        started = true;
        openedAt = System.currentTimeMillis();
        MinecraftServer.getSchedulerManager().buildTask(CrystalHollowsLobby::tick).repeat(TaskSchedule.seconds(5)).schedule();
    }

    public static long elapsedMillis() {
        return System.currentTimeMillis() - openedAt;
    }

    public static int day() {
        return LobbySchedule.dayAt(elapsedMillis());
    }

    public static boolean acceptsNewPlayers() {
        return LobbySchedule.acceptsNewPlayers(elapsedMillis());
    }

    public static boolean mayEnter(SkyBlockPlayer player) {
        return ADMITTED.contains(player.getUuid()) || acceptsNewPlayers();
    }

    public static void admit(SkyBlockPlayer player) {
        ADMITTED.add(player.getUuid());
    }

    private static void tick() {
        try {
            List<SkyBlockPlayer> players = SkyBlockGenericLoader.getLoadedPlayers();
            if (LobbySchedule.phaseAt(elapsedMillis(), players.size()) == LobbySchedule.Phase.CLOSED) {
                shutDown(players);
                return;
            }
            warn(players);
        } catch (RuntimeException exception) {
            org.tinylog.Logger.error(exception, "Crystal Hollows lobby tick failed");
        }
    }

    private static void warn(List<SkyBlockPlayer> players) {
        long minutesLeft = LobbySchedule.millisUntilShutdown(elapsedMillis()) / 60_000;
        for (long mark : WARNING_MINUTES) {
            if (minutesLeft < mark && lastWarnedMinutes >= mark) {
                lastWarnedMinutes = mark;
                players.forEach(player -> player.sendMessage(
                        Text.of("<c>This Crystal Hollows lobby is shutting down in {} minute{}!", mark, mark == 1 ? "" : "s")));
                return;
            }
        }
    }

    private static void shutDown(List<SkyBlockPlayer> players) {
        for (SkyBlockPlayer player : players) {
            player.sendMessage(Text.of("<c>This Crystal Hollows lobby has shut down. You have been sent to the Dwarven Mines!"));
            player.sendTo(ServerType.SKYBLOCK_DWARVEN_MINES);
        }
        ADMITTED.clear();
        CrystalHollowsLayout.generate();
        FairyGrottoFishing.reset();
        lastWarnedMinutes = Long.MAX_VALUE;
        openedAt = System.currentTimeMillis();
    }
}
