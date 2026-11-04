package dev.daweapon.type.skyblockgeneric.carnival;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.leaderboard.LeaderboardService;
import dev.daweapon.type.skyblockgeneric.leaderboard.SkyBlockLeaderboards;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Each Carnival's per-game leaderboards of best single-game scores (wiki: Carnival, Jar of Sand, Archery Cube,
 * Aquarium Diorama, Plastic Shovel, Singing Fish, Lantern of the Dead). Once the Carnival has closed, the next join
 * shows the results with each game's Top 10 on hover, and the claim gives a game's Top 500 trophy and, for a best
 * score of 1,000 or more, its high-score trophy. Ranks are per profile and staff profiles are never ranked.
 */
public final class CarnivalLeaderboards {
    public static final int REWARD_PLACES = 500;
    public static final long HIGH_SCORE = 1_000;
    public static final String CLAIM_COMMAND = "/carnivalrewards";
    private static final Set<UUID> CLAIMING = ConcurrentHashMap.newKeySet();

    public enum Reward {
        FRUIT_DIGGING(CarnivalGame.FRUIT_DIGGING, ItemType.JAR_OF_SAND, ItemType.PLASTIC_SHOVEL),
        CATCH_A_FISH(CarnivalGame.CATCH_A_FISH, ItemType.MINI_FISH_BOWL, ItemType.SINGING_FISH),
        ZOMBIE_SHOOTOUT(CarnivalGame.ZOMBIE_SHOOTOUT, ItemType.ARCHERY_CUBE, ItemType.LANTERN_OF_THE_DEAD);

        private final CarnivalGame game;
        private final ItemType placementTrophy;
        private final ItemType highScoreTrophy;

        Reward(CarnivalGame game, ItemType placementTrophy, ItemType highScoreTrophy) {
            this.game = game;
            this.placementTrophy = placementTrophy;
            this.highScoreTrophy = highScoreTrophy;
        }

        static Reward of(CarnivalGame game) {
            for (Reward reward : values()) if (reward.game == game) return reward;
            throw new IllegalArgumentException(game.name());
        }
    }

    public record Standing(CarnivalGame game, long best, int rank, List<SkyBlockLeaderboards.Row> top) {
    }

    private CarnivalLeaderboards() {
    }

    public static String key(int year, CarnivalGame game) {
        return "carnival:" + year + ":" + game.name();
    }

    public static void submit(SkyBlockPlayer player, CarnivalGame game, long best) {
        if (SkyBlockLeaderboards.hidden(player)) return;
        UUID profile = SkyBlockLeaderboards.currentProfile(player);
        if (profile == null) return;
        String owner = player.getUsername();
        String key = key(Carnival.year(), game);
        Thread.startVirtualThread(() -> {
            LeaderboardService.putMemberMeta(profile, Map.of(), Map.of("owner", owner));
            LeaderboardService.updateScore(key, profile, best);
        });
    }

    public static boolean hasPendingResults(CarnivalProfile profile) {
        return !Carnival.isOpen() && profile.getYear() >= 0 && profile.getClaimedYear() != profile.getYear()
                && profile.playedThisYear();
    }

    public static void announceIfPending(SkyBlockPlayer player) {
        CarnivalProfile profile = Carnivals.profile(player);
        if (!hasPendingResults(profile)) return;
        loadStandings(player, profile, standings -> announce(player, profile.getYear(), standings));
    }

    public static void claim(SkyBlockPlayer player) {
        CarnivalProfile profile = Carnivals.profile(player);
        if (!hasPendingResults(profile)) {
            player.sendMessage("<c>You don't have any Carnival rewards to claim!");
            return;
        }
        if (!CLAIMING.add(player.getUuid())) return;
        loadStandings(player, profile, standings -> {
            CLAIMING.remove(player.getUuid());
            CarnivalProfile current = Carnivals.profile(player);
            if (!hasPendingResults(current)) return;
            current.setClaimedYear(current.getYear());
            Carnivals.save(player, current);
            List<ItemType> rewards = rewardsFor(standings);
            rewards.forEach(player::addAndUpdateItem);
            if (rewards.isEmpty()) {
                player.sendMessage("<e>You didn't earn any Carnival trophies this time. Better luck next Carnival!");
                return;
            }
            player.sendMessage("<a><l>CARNIVAL REWARDS CLAIMED");
            for (ItemType reward : rewards) player.sendMessage("   <8>+<7>" + StringUtility.toNormalCase(reward.name()));
        });
    }

    static List<ItemType> rewardsFor(List<Standing> standings) {
        List<ItemType> rewards = new ArrayList<>();
        for (Standing standing : standings) {
            Reward reward = Reward.of(standing.game());
            if (standing.rank() >= 1 && standing.rank() <= REWARD_PLACES) rewards.add(reward.placementTrophy);
            if (standing.best() >= HIGH_SCORE) rewards.add(reward.highScoreTrophy);
        }
        return rewards;
    }

    private static void loadStandings(SkyBlockPlayer player, CarnivalProfile profile,
                                      java.util.function.Consumer<List<Standing>> then) {
        UUID profileId = SkyBlockLeaderboards.currentProfile(player);
        int year = profile.getYear();
        Map<CarnivalGame, Long> bests = new EnumMap<>(CarnivalGame.class);
        for (CarnivalGame game : CarnivalGame.values()) {
            CarnivalProfile.GameStats stats = profile.getStats().get(game);
            if (stats != null && stats.getYearAttempts() > 0) bests.put(game, stats.getYearHighScore());
        }
        Thread.startVirtualThread(() -> {
            List<Standing> standings = new ArrayList<>();
            bests.forEach((game, best) -> {
                String key = key(year, game);
                int rank = profileId == null ? -1 : LeaderboardService.getPlayerRank(key, profileId);
                List<SkyBlockLeaderboards.Row> top = new ArrayList<>();
                for (LeaderboardService.LeaderboardEntry entry : LeaderboardService.getTopN(key, SkyBlockLeaderboards.TOP_SIZE)) {
                    String owner = LeaderboardService.getMemberMeta(entry.playerUuid()).getOrDefault("owner", "Unknown");
                    top.add(new SkyBlockLeaderboards.Row(entry.rank(), entry.playerUuid(), owner, "", entry.score()));
                }
                standings.add(new Standing(game, best, rank, top));
            });
            player.scheduler().scheduleNextTick(() -> then.accept(standings));
        });
    }

    private static void announce(SkyBlockPlayer player, int year, List<Standing> standings) {
        player.sendMessage("<6><l>CARNIVAL RESULTS <8>(Year " + year + ")");
        for (Standing standing : standings) {
            String place = standing.rank() > 0 ? "<e>#" + StringUtility.commaify(standing.rank()) : "<8>Unranked";
            player.sendMessage(Text.of(" " + standing.game().colored() + "<7>: Best score <a>"
                            + StringUtility.commaify(standing.best()) + " <7>- " + place).asComponent()
                    .hoverEvent(HoverEvent.showText(Text.of(topTen(standing)))));
        }
        player.sendMessage(Component.text("CLICK HERE", NamedTextColor.YELLOW)
                .append(Component.text(" to claim your rewards!", NamedTextColor.GOLD))
                .clickEvent(ClickEvent.runCommand(CLAIM_COMMAND))
                .hoverEvent(HoverEvent.showText(Text.of("<e>Click to claim!"))));
    }

    private static String topTen(Standing standing) {
        StringBuilder lines = new StringBuilder(standing.game().colored() + " <7>Top 10");
        for (SkyBlockLeaderboards.Row row : standing.top()) {
            lines.append("\n<7>").append(row.rank()).append(". <f>").append(row.owner())
                    .append(" <7>- <a>").append(StringUtility.commaify((long) row.score()));
        }
        return lines.toString();
    }
}
