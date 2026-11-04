package dev.daweapon.type.hub;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.entity.hologram.PlayerHolograms;
import dev.daweapon.type.generic.leaderboard.LeaderboardService;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.leaderboard.SkyBlockBoards;
import dev.daweapon.type.skyblockgeneric.leaderboard.SkyBlockLeaderboards;
import net.minestom.server.coordinate.Pos;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class SkyBlockLevelLeaderboardDisplay {
    private static final Pos DISPLAY_POS = new Pos(6.5, 77, -0.5);
    private static final long REFRESH_MILLIS = 5 * 60_000L;

    private static final Map<HypixelPlayer, Applied> holos = new ConcurrentHashMap<>();
    private static final String STALE_COLLECTION_KEYS = "skyblock:collection:";
    private static final AtomicBoolean loading = new AtomicBoolean();
    private static final AtomicBoolean staleKeysRetired = new AtomicBoolean();
    private static volatile String[] lines = loadingLines();
    private static volatile long loadedAt;

    private record Applied(PlayerHolograms.ExternalPlayerHologram hologram, String[] lines) {
    }

    public static void addAndUpdate() {
        Set<UUID> loadedUuids = HypixelGenericLoader.getLoadedPlayers().stream()
                .map(HypixelPlayer::getUuid)
                .collect(Collectors.toSet());
        holos.keySet().removeIf(p -> !loadedUuids.contains(p.getUuid()));

        if (System.currentTimeMillis() - loadedAt > REFRESH_MILLIS) reload();
        String[] current = lines;

        HypixelGenericLoader.forEachLoadedPlayer(player -> {
            Applied applied = holos.get(player);
            if (applied != null && applied.lines() == current) return;
            if (applied != null) PlayerHolograms.removeExternalPlayerHologram(applied.hologram());
            PlayerHolograms.ExternalPlayerHologram holo = PlayerHolograms.ExternalPlayerHologram.builder()
                    .player(player)
                    .text(current)
                    .pos(DISPLAY_POS)
                    .spacing(0.3d)
                    .instance(player.getInstance())
                    .build();
            PlayerHolograms.addExternalPlayerHologram(holo);
            holos.put(player, new Applied(holo, current));
        });
    }

    private static void reload() {
        if (!loading.compareAndSet(false, true)) return;
        Thread.startVirtualThread(() -> {
            try {
                if (staleKeysRetired.compareAndSet(false, true)) {
                    int deleted = LeaderboardService.deleteKeys(STALE_COLLECTION_KEYS);
                    if (deleted > 0) Logger.info("Deleted {} stale per-player collection leaderboard keys", deleted);
                }
                SkyBlockLeaderboards.Page page = SkyBlockLeaderboards.load(SkyBlockBoards.fixed(SkyBlockBoards.LEVEL).orElseThrow(), null);
                lines = renderLines(page);
                loadedAt = System.currentTimeMillis();
            } finally {
                loading.set(false);
            }
        });
    }

    private static String[] renderLines(SkyBlockLeaderboards.Page page) {
        List<String> message = new ArrayList<>(List.of("<6><l>SKYBLOCK LEVEL", "<f>Top Profiles", " "));
        if (page.top().isEmpty()) message.add("<7>No ranked profiles yet.");
        for (SkyBlockLeaderboards.Row row : page.top()) {
            String color = switch (row.rank()) {
                case 1 -> "<6>";
                case 2 -> "<7>";
                case 3 -> "<c>";
                default -> "<f>";
            };
            message.add(row.rank() + ". " + color + row.owner() + " <7>- <f>" + StringUtility.commaify((long) row.score()) + " XP");
        }
        message.add(" ");
        message.add("<7>Use <e>/leaderboard <7>for more.");
        return message.toArray(new String[0]);
    }

    private static String[] loadingLines() {
        return new String[]{"<6><l>SKYBLOCK LEVEL", "<7>Loading..."};
    }
}
