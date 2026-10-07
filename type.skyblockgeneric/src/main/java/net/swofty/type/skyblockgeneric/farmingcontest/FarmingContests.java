package net.swofty.type.skyblockgeneric.farmingcontest;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointFarmingContests;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContestProfile.Entry;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runs Jacob's Farming Contests for players on this server. Scores live in a Redis sorted set
 * per contest crop, so every server ranks against the same participants; only players who have
 * collected the 100-crop minimum are entered, which makes the set's size the participant count.
 */
public final class FarmingContests {
    public static final int REQUIRED_FARMING_LEVEL = 10;
    public static final String JACOB = "<e>[NPC] Jacob<f>: ";
    private static final int GOLD_CROP_XP = 10;
    private static final long LEADERBOARD_TTL_SECONDS = Duration.ofDays(90).toSeconds();
    private static final Map<UUID, ContestBracket> liveBrackets = new ConcurrentHashMap<>();
    private static final Set<UUID> resolving = ConcurrentHashMap.newKeySet();

    private FarmingContests() {}

    public static void start() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                if (player.getSkyblockDataHandler() == null) continue;
                finaliseEnded(player);
                refreshLiveBracket(player);
            }
            return TaskSchedule.seconds(5);
        });
    }

    public static FarmingContestProfile profile(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    public static void save(SkyBlockPlayer player, FarmingContestProfile profile) {
        datapoint(player).setValue(profile);
    }

    private static DatapointFarmingContests datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.FARMING_CONTESTS, DatapointFarmingContests.class);
    }

    public static int farmingLevelCapTier(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler() == null ? 0 : profile(player).getLevelCapTier();
    }

    public static boolean meetsFarmingRequirement(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.FARMING) >= REQUIRED_FARMING_LEVEL;
    }

    public static boolean canParticipate(SkyBlockPlayer player) {
        return meetsFarmingRequirement(player) && profile(player).isMetJacob();
    }

    public static boolean goatedActive() {
        return ElectionPerks.isActive(SkyBlockMayor.Perk.GOATED);
    }

    public static Optional<Entry> activeEntry(SkyBlockPlayer player) {
        return FarmingContest.active().flatMap(contest -> profile(player).entryFor(contest));
    }

    public static Optional<ContestBracket> liveBracket(SkyBlockPlayer player) {
        return Optional.ofNullable(liveBrackets.get(player.getUuid()));
    }

    public static void announceStart() {
        Optional<FarmingContest> active = FarmingContest.active();
        if (active.isEmpty()) return;
        ContestCrop anitaCrop = active.get().anitaCrop();
        boolean goated = goatedActive();
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (player.getSkyblockDataHandler() == null || !meetsFarmingRequirement(player)) continue;
            player.sendMessage(JACOB + "My contest has started!");
            if (goated) player.sendMessage(JACOB + "<e>Finnegan<f> is forcing me to be more lenient with rankings...");
            double accessoryFortune = FarmingContestStatistics.anitaAccessoryFortune(player);
            if (accessoryFortune > 0 && anitaCrop.getFortuneStatistic() != null) {
                player.sendMessage(JACOB + "Your " + anitaAccessoryName(accessoryFortune) + " <f>is giving you <6>+"
                        + (int) accessoryFortune + " " + anitaCrop.getDisplayName() + " Fortune <f>during the contest!");
            }
        }
    }

    private static String anitaAccessoryName(double fortune) {
        if (fortune >= 25) return "<9>Anita's Artifact";
        if (fortune >= 15) return "<a>Anita's Ring";
        return "<f>Anita's Talisman";
    }

    public static void recordHarvest(SkyBlockPlayer player, ItemType type, int amount) {
        Optional<ContestCrop> harvested = ContestCrop.fromDrop(type);
        Optional<FarmingContest> active = FarmingContest.active();
        if (harvested.isEmpty() || active.isEmpty() || amount <= 0) return;
        ContestCrop crop = harvested.get();
        FarmingContest contest = active.get();
        if (!contest.features(crop) || !canParticipate(player)) return;

        FarmingContestProfile profile = profile(player);
        Entry entry = profile.entryFor(contest).orElse(null);
        if (entry == null) entry = profile.participate(contest, crop, goatedActive());
        if (entry.getCrop() != crop) return;

        entry.setCollected(entry.getCollected() + amount);
        if (entry.qualifies()) {
            LeaderboardService.updateExpiringScoreAsync(contest.leaderboardKey(crop), player.getUuid(),
                    entry.getCollected(), LEADERBOARD_TTL_SECONDS);
        }

        long best = profile.personalBest(crop);
        if (profile.isPersonalBestsUnlocked() && !entry.isPersonalBestAnnounced() && best > 0 && entry.getCollected() > best) {
            entry.setPersonalBestAnnounced(true);
            player.sendMessage(JACOB + "<d><l>PERSONAL BEST<r><f>! You've surpassed your previous record of <e>"
                    + StringUtility.commaify(best) + " <f>items collected in the <a>" + crop.getDisplayName() + " Contest<f>!");
            player.sendMessage(JACOB + "Keep it up!");
        }
        save(player, profile);
    }

    private static void refreshLiveBracket(SkyBlockPlayer player) {
        Optional<FarmingContest> contest = FarmingContest.active();
        Optional<Entry> entry = contest.flatMap(active -> profile(player).entryFor(active));
        if (entry.isEmpty() || !entry.get().qualifies() || !LeaderboardService.isInitialized()) {
            liveBrackets.remove(player.getUuid());
            return;
        }
        String key = contest.get().leaderboardKey(entry.get().getCrop());
        boolean goated = entry.get().isGoated();
        UUID uuid = player.getUuid();
        Thread.startVirtualThread(() -> liveBrackets.put(uuid, ContestBracket.forPlacement(
                LeaderboardService.getPlayerRank(key, uuid), LeaderboardService.getTotalPlayers(key), goated)));
    }

    public static void finaliseEnded(SkyBlockPlayer player) {
        List<Entry> pending = profile(player).getEntries().stream()
                .filter(entry -> !entry.isFinalised() && entry.contest().hasEnded())
                .toList();
        if (pending.isEmpty()) return;
        boolean needsRanking = pending.stream().anyMatch(Entry::qualifies);
        if (needsRanking && !LeaderboardService.isInitialized()) return;
        UUID uuid = player.getUuid();
        if (!resolving.add(uuid)) return;

        Thread.startVirtualThread(() -> {
            Map<String, ContestBracket> results = new HashMap<>();
            try {
                for (Entry entry : pending) {
                    if (!entry.qualifies()) continue;
                    String key = entry.contest().leaderboardKey(entry.getCrop());
                    results.put(entry.getContestId(), ContestBracket.forPlacement(
                            LeaderboardService.getPlayerRank(key, uuid),
                            LeaderboardService.getTotalPlayers(key),
                            entry.isGoated()));
                }
            } finally {
                MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                    try {
                        if (player.isOnline() && player.getSkyblockDataHandler() != null) applyResults(player, results);
                    } finally {
                        resolving.remove(uuid);
                    }
                });
            }
        });
    }

    private static void applyResults(SkyBlockPlayer player, Map<String, ContestBracket> results) {
        FarmingContestProfile profile = profile(player);
        for (Entry entry : List.copyOf(profile.getEntries())) {
            if (entry.isFinalised() || !entry.contest().hasEnded()) continue;
            if (!entry.qualifies()) {
                profile.getEntries().remove(entry);
                continue;
            }
            ContestBracket bracket = results.get(entry.getContestId());
            if (bracket == null) continue;
            entry.setBracket(bracket);
            entry.setFinalised(true);
            profile.setContestsParticipated(profile.getContestsParticipated() + 1);
            announceResult(player, profile, entry, bracket);
        }
        profile.trimHistory();
        save(player, profile);
    }

    private static void announceResult(SkyBlockPlayer player, FarmingContestProfile profile, Entry entry, ContestBracket bracket) {
        ContestCrop crop = entry.getCrop();
        String collected = StringUtility.commaify(entry.getCollected());
        long previousBest = profile.personalBest(crop);
        boolean personalBest = profile.isPersonalBestsUnlocked() && entry.getCollected() > previousBest;

        player.sendMessage(JACOB + "The Farming Contest is over!");
        player.sendMessage(JACOB + "Let me count the final results eh?");
        if (bracket.earnsMedal()) {
            player.sendMessage(JACOB + "You placed in the " + bracket.formatted() + " <r><f>bracket in the <a>"
                    + crop.getDisplayName() + " Contest<f>!");
        } else {
            player.sendMessage(JACOB + "You didn't earn a medal, but you may claim a participation reward in the <a>"
                    + crop.getDisplayName() + " Contest<f>!");
        }
        player.sendMessage(JACOB + "You collected <e>" + collected + "<f> items!" + (personalBest ? " <d><l>PERSONAL BEST<r><f>!" : ""));

        if (personalBest) {
            profile.getPersonalBests().put(crop, entry.getCollected());
            player.sendMessage(JACOB + "Your previous personal best was <e>" + StringUtility.commaify(previousBest) + "<f>.");
            if (crop.getFortuneStatistic() != null) {
                player.sendMessage(JACOB + "Your <6>Personal Bests <f>perk is now granting you <6>+"
                        + String.format("%.2f", crop.personalBestFortune(entry.getCollected())) + "☘ "
                        + crop.getDisplayName() + " Fortune<f>!");
            }
        }
        if (HypixelConst.getTypeLoader().getType() != ServerType.SKYBLOCK_HUB) {
            player.sendMessage(JACOB + "Come see me in the <a>Hub <f>to claim your reward!");
        }
    }

    public static int claim(SkyBlockPlayer player, List<Entry> toClaim) {
        FarmingContestProfile profile = profile(player);
        int claimed = 0;
        for (Entry requested : toClaim) {
            Entry entry = profile.getEntries().stream()
                    .filter(candidate -> candidate.getContestId().equals(requested.getContestId()))
                    .findFirst().orElse(null);
            if (entry == null || !entry.isClaimable()) continue;
            grant(player, profile, entry);
            claimed++;
        }
        if (claimed > 0) save(player, profile);
        return claimed;
    }

    private static void grant(SkyBlockPlayer player, FarmingContestProfile profile, Entry entry) {
        ContestBracket bracket = entry.getBracket();
        ContestRewards.grant(player, bracket);
        profile.addMedals(ContestBracket.BRONZE, bracket.getBronzeMedals());
        profile.addMedals(ContestBracket.SILVER, bracket.getSilverMedals());
        profile.addMedals(ContestBracket.GOLD, bracket.getGoldMedals());

        if (bracket.earnsMedal()) {
            ContestBracket previous = profile.uniqueBracket(entry.getCrop());
            if (previous == null || bracket.ordinal() > previous.ordinal()) {
                profile.getUniqueBrackets().put(entry.getCrop(), bracket);
                if (bracket.atLeast(ContestBracket.GOLD) && (previous == null || !previous.atLeast(ContestBracket.GOLD))) {
                    player.getSkyBlockExperience().addSourceXP(SkyBlockXPSource.JACOBS_FARMING_CONTEST, GOLD_CROP_XP);
                }
            }
        }
        entry.setClaimed(true);
        player.sendMessage("<a>You claimed the " + bracket.formatted() + " <r><a>rewards from the <e>"
                + entry.getCrop().getDisplayName() + " Contest <a>on <e>" + entry.contest().dateLabel() + "<a>!");
    }
}
