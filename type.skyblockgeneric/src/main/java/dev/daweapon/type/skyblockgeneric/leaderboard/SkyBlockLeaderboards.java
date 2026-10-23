package dev.daweapon.type.skyblockgeneric.leaderboard;

import dev.daweapon.type.generic.data.datapoints.DatapointString;
import dev.daweapon.type.generic.leaderboard.LeaderboardService;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class SkyBlockLeaderboards {
    public static final int TOP_SIZE = 10;
    private static final long RANK_CACHE_MILLIS = 30_000;
    private static final Map<String, CachedRank> RANKS = new ConcurrentHashMap<>();

    public record Row(int rank, UUID profileId, String owner, String profileName, double score) {
    }

    public record Page(SkyBlockBoard board, List<Row> top, Row own, long total) {
    }

    private record CachedRank(int rank, long loadedAt) {
    }

    private SkyBlockLeaderboards() {
    }

    public static boolean hidden(SkyBlockPlayer player) {
        return player.getRank().isStaff();
    }

    public static void sync(SkyBlockPlayer player, SkyBlockDataHandler handler) {
        if (!LeaderboardService.isInitialized()) return;
        UUID profileId = handler.getCurrentProfileId();
        if (profileId == null) return;
        if (hidden(player)) {
            forget(profileId);
            return;
        }
        String profileName = handler.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class).getValue();
        LeaderboardService.putMemberMeta(profileId, Map.of("profileName", String.valueOf(profileName)),
                Map.of("owner", player.getUsername()));
        LeaderboardService.updateScores(profileId, SkyBlockBoards.scoresOf(player));
    }

    public static void forget(UUID profileId) {
        LeaderboardService.removeFromAllLeaderboards(profileId);
        RANKS.keySet().removeIf(key -> key.startsWith(profileId.toString()));
    }

    public static Page load(SkyBlockBoard board, UUID ownProfile) {
        List<Row> top = new ArrayList<>();
        for (LeaderboardService.LeaderboardEntry entry : LeaderboardService.getTopN(board.key(), TOP_SIZE)) {
            top.add(row(entry.rank(), entry.playerUuid(), entry.score()));
        }
        Row own = null;
        if (ownProfile != null) {
            LeaderboardService.LeaderboardEntry entry = LeaderboardService.getPlayerRankEntry(board.key(), ownProfile);
            if (entry != null) own = row(entry.rank(), ownProfile, entry.score());
        }
        return new Page(board, top, own, LeaderboardService.getTotalPlayers(board.key()));
    }

    public static void loadAsync(SkyBlockPlayer player, SkyBlockBoard board, Consumer<Page> then) {
        UUID profile = currentProfile(player);
        Thread.startVirtualThread(() -> {
            Page page = load(board, profile);
            player.scheduler().scheduleNextTick(() -> then.accept(page));
        });
    }

    public static List<String> collectionBoardIds() {
        List<String> ids = new ArrayList<>();
        for (String key : LeaderboardService.listKeys(SkyBlockBoard.KEY_PREFIX + SkyBlockBoards.COLLECTION_PREFIX)) {
            ids.add(key.substring(SkyBlockBoard.KEY_PREFIX.length()));
        }
        ids.sort(String::compareTo);
        return ids;
    }

    public static int cachedRank(SkyBlockPlayer player, SkyBlockBoard board, Runnable onLoaded) {
        UUID profile = currentProfile(player);
        if (profile == null) return -1;
        String key = profile + "|" + board.id();
        CachedRank cached = RANKS.get(key);
        long now = System.currentTimeMillis();
        if (cached == null || now - cached.loadedAt() > RANK_CACHE_MILLIS) {
            Thread.startVirtualThread(() -> {
                int rank = LeaderboardService.getPlayerRank(board.key(), profile);
                boolean changed = cached == null || cached.rank() != rank;
                RANKS.put(key, new CachedRank(rank, System.currentTimeMillis()));
                if (changed) player.scheduler().scheduleNextTick(onLoaded);
            });
        }
        return cached == null ? -1 : cached.rank();
    }

    public static UUID currentProfile(SkyBlockPlayer player) {
        SkyBlockDataHandler handler = player.getSkyblockDataHandler();
        return handler == null ? null : handler.getCurrentProfileId();
    }

    private static Row row(int rank, UUID profileId, double score) {
        Map<String, String> meta = LeaderboardService.getMemberMeta(profileId);
        return new Row(rank, profileId, meta.getOrDefault("owner", "Unknown"), meta.getOrDefault("profileName", "?"), score);
    }
}
