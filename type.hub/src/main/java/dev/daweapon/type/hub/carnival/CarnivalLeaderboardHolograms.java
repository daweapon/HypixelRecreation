package dev.daweapon.type.hub.carnival;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.entity.hologram.PlayerHolograms;
import dev.daweapon.type.skyblockgeneric.carnival.Carnival;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGame;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalLeaderboards;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The three game leaderboards standing beside the stalls while the Carnival is open, at the captured spots: the Top 10
 * of this Carnival's best single-game scores, then the viewer's own line. Each viewer gets their own copy.
 */
public final class CarnivalLeaderboardHolograms {
    private static final double SPACING = 0.37;
    private static final double NAME_HEIGHT = 1.975;
    private static final int REFRESH_SECONDS = 10;
    private static final Map<CarnivalGame, Pos> BOTTOM_LINE = Map.of(
            CarnivalGame.FRUIT_DIGGING, new Pos(-110.5, 72.65 + NAME_HEIGHT, 29.5),
            CarnivalGame.CATCH_A_FISH, new Pos(-77.5, 71.15 + NAME_HEIGHT, 27.5),
            CarnivalGame.ZOMBIE_SHOOTOUT, new Pos(-100.5, 69.65 + NAME_HEIGHT, 37.5));

    private static final Map<UUID, Map<CarnivalGame, PlayerHolograms.ExternalPlayerHologram>> SHOWN = new ConcurrentHashMap<>();

    private CarnivalLeaderboardHolograms() {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(CarnivalLeaderboardHolograms::refresh)
                .delay(TaskSchedule.seconds(5)).repeat(TaskSchedule.seconds(REFRESH_SECONDS)).schedule();
    }

    private static void refresh() {
        List<SkyBlockPlayer> viewers = MinecraftServer.getConnectionManager().getOnlinePlayers().stream()
                .filter(SkyBlockPlayer.class::isInstance).map(SkyBlockPlayer.class::cast)
                .filter(Player::isOnline).toList();
        SHOWN.keySet().removeIf(uuid -> viewers.stream().noneMatch(viewer -> viewer.getUuid().equals(uuid)));
        if (!Carnival.isOpen()) {
            viewers.forEach(CarnivalLeaderboardHolograms::hide);
            return;
        }
        Thread.startVirtualThread(() -> {
            Map<CarnivalGame, CarnivalLeaderboards.Board> boards = new EnumMap<>(CarnivalGame.class);
            for (CarnivalGame game : CarnivalGame.values()) boards.put(game, CarnivalLeaderboards.loadBoard(game));
            Map<UUID, Map<CarnivalGame, String[]>> text = new HashMap<>();
            for (SkyBlockPlayer viewer : viewers) {
                if (viewer.getSkyblockDataHandler() == null) continue;
                Map<CarnivalGame, String[]> perGame = new EnumMap<>(CarnivalGame.class);
                String colour = NamedTextColor.NAMES.key(viewer.getNameColor());
                for (CarnivalGame game : CarnivalGame.values()) {
                    perGame.put(game, CarnivalLeaderboards.boardLines(game, boards.get(game),
                            CarnivalLeaderboards.loadOwnRow(viewer, game), viewer.getUsername(), colour).toArray(String[]::new));
                }
                text.put(viewer.getUuid(), perGame);
            }
            MinecraftServer.getSchedulerManager().scheduleNextTick(() -> viewers.forEach(viewer -> {
                Map<CarnivalGame, String[]> lines = text.get(viewer.getUuid());
                if (lines != null && viewer.isOnline()) show(viewer, lines);
            }));
        });
    }

    private static void show(SkyBlockPlayer viewer, Map<CarnivalGame, String[]> lines) {
        Map<CarnivalGame, PlayerHolograms.ExternalPlayerHologram> shown = SHOWN.computeIfAbsent(viewer.getUuid(), _ -> new EnumMap<>(CarnivalGame.class));
        lines.forEach((game, text) -> {
            PlayerHolograms.ExternalPlayerHologram hologram = shown.get(game);
            if (hologram != null && hologram.getInstance() == viewer.getInstance()) {
                PlayerHolograms.updateExternalPlayerHologramText(hologram, text);
                return;
            }
            if (hologram != null) PlayerHolograms.removeExternalPlayerHologram(hologram);
            hologram = PlayerHolograms.ExternalPlayerHologram.builder()
                    .player(viewer)
                    .instance(viewer.getInstance())
                    .pos(BOTTOM_LINE.get(game))
                    .text(text)
                    .spacing(SPACING)
                    .build();
            PlayerHolograms.addExternalPlayerHologram(hologram);
            shown.put(game, hologram);
        });
    }

    private static void hide(SkyBlockPlayer viewer) {
        Map<CarnivalGame, PlayerHolograms.ExternalPlayerHologram> shown = SHOWN.remove(viewer.getUuid());
        if (shown != null) shown.values().forEach(PlayerHolograms::removeExternalPlayerHologram);
    }
}
