package net.swofty.type.skyblockgeneric.elections;

import net.swofty.type.skyblockgeneric.accessory.AccessoryOwnership;
import com.google.gson.Gson;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.election.CastVoteProtocol;
import net.swofty.commons.protocol.objects.election.ForceElectMayorProtocol;
import net.swofty.commons.protocol.objects.election.GetCandidatesProtocol;
import net.swofty.commons.protocol.objects.election.GetElectionDataProtocol;
import net.swofty.commons.protocol.objects.election.GetPlayerVoteProtocol;
import net.swofty.commons.protocol.objects.election.ResolveElectionProtocol;
import net.swofty.commons.protocol.objects.election.StartElectionProtocol;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.elections.perks.PerkpocalypseRotation;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

// Coordinates mayoral elections with the remote election service: loading/syncing election
// state, starting/resolving election cycles, casting and caching votes, and checking active perks.
public class ElectionManager {

    private static final Gson GSON = new Gson();
    private static final java.util.List<net.swofty.commons.skyblock.item.ItemType> VOTER_BADGES = java.util.List.of(
            net.swofty.commons.skyblock.item.ItemType.VOTER_BADGE, net.swofty.commons.skyblock.item.ItemType.VOTER_BADGE_VIP,
            net.swofty.commons.skyblock.item.ItemType.VOTER_BADGE_ELITE, net.swofty.commons.skyblock.item.ItemType.VOTER_BADGE_SUPREME);
    private static ProxyService SERVICE;
    private static final ConcurrentHashMap<UUID, String> playerVoteCache = new ConcurrentHashMap<>();
    private static final AtomicBoolean tallyRefreshStarted = new AtomicBoolean();

    @Getter
    private static ElectionData electionData = new ElectionData();

    public static void loadFromService() {
        SERVICE = new ProxyService(ServiceType.ELECTION);
        if (!SERVICE.isOnline().join()) {
            Logger.error("Service ELECTION is not online!");
            electionData = new ElectionData();
            initializeFirstElection();
            startTallyRefreshTask();
            return;
        }

        try {
            GetElectionDataProtocol.GetElectionDataResponse response =
                SERVICE.<GetElectionDataProtocol.GetElectionDataMessage,
                    GetElectionDataProtocol.GetElectionDataResponse>handleRequest(
                    new GetElectionDataProtocol.GetElectionDataMessage()
                ).join();

            if (response.found() && response.serializedData() != null) {
                electionData = GSON.fromJson(response.serializedData(), ElectionData.class);
                if (electionData.getCurrentMayor() == null) {
                    electionData = new ElectionData();
                    initializeFirstElection();
                    Logger.info("Election data was missing mayor info. Reinitialized first election.");
                }
            } else {
                electionData = new ElectionData();
                initializeFirstElection();
                Logger.info("No election data found. Initialized first election.");
            }
        } catch (Exception e) {
            Logger.error("Failed to load election data from service: {}", e.getMessage());
            electionData = new ElectionData();
            initializeFirstElection();
        }

        startTallyRefreshTask();
    }

    private static void initializeFirstElection() {
        int currentYear = SkyBlockCalendar.getYear();
        electionData.setCurrentMayor(SkyBlockMayor.COLE.name());
        electionData.setCurrentMayorColor(ElectionData.wireColorForIndex(4));
        electionData.setCurrentMayorPerks(Arrays.stream(SkyBlockMayor.COLE.getAllPerks()).map(Enum::name).toList());
        electionData.setMayorElectedYear(currentYear);
        electionData.setCurrentMinister(SkyBlockMayor.DIAZ.name());
        electionData.setMinisterPerk(SkyBlockMayor.Perk.STOCK_EXCHANGE.name());
        electionData.setCurrentMinisterColor(ElectionData.wireColorForIndex(0));
    }

    public static void onElectionStart() {
        int currentYear = SkyBlockCalendar.getYear();
        if (electionData.isElectionOpen()) return;

        electionData.startNewElection(currentYear);
        String serialized = GSON.toJson(electionData);

        try {
            StartElectionProtocol.StartElectionResponse response =
                SERVICE.<StartElectionProtocol.StartElectionMessage,
                    StartElectionProtocol.StartElectionResponse>handleRequest(
                    new StartElectionProtocol.StartElectionMessage(currentYear, serialized)
                ).join();

            if (response.serializedData() != null) {
                electionData = GSON.fromJson(response.serializedData(), ElectionData.class);
            }

            if (response.started()) {
                Logger.info("Election started for Year {}", currentYear);
            } else {
                Logger.info("Election for Year {} was already started by another server", currentYear);
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to start election via service");
        }
    }

    public static void onElectionEnd() {
        if (!electionData.isElectionOpen()) return;

        // The voting window now spans the SkyBlock year boundary (opens Late Summer, closes Late
        // Spring the following year), so SkyBlockCalendar.getYear() at close time is one year ahead
        // of the year votes were actually tallied under. electionData.getElectionYear() still holds
        // the year the election opened under - the same value CastVoteEndpoint/ElectionDatabase
        // tallied votes against - so it must be used here to look up the right tally record.
        int electionYear = electionData.getElectionYear();

        try {
            ResolveElectionProtocol.ResolveElectionResponse response =
                SERVICE.<ResolveElectionProtocol.ResolveElectionMessage,
                    ResolveElectionProtocol.ResolveElectionResponse>handleRequest(
                    new ResolveElectionProtocol.ResolveElectionMessage(electionYear)
                ).join();

            if (response.serializedData() != null) {
                electionData = GSON.fromJson(response.serializedData(), ElectionData.class);
            }

            if (response.resolved()) {
                Logger.info("Election resolved for Year {}. New Mayor: {}", electionYear, electionData.getCurrentMayor());
            } else {
                Logger.info("Election for Year {} was already resolved by another server", electionYear);
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to resolve election via service");
        }

        playerVoteCache.clear();
    }

    // Admin-only testing helper backing /adminbypass skiptospecialmayor: directly force-elects the
    // next special mayor in rotation (same specialCandidateIndex real special-year elections use),
    // bypassing the normal vote/resolve cycle. Not a real Hypixel mechanic - a dev convenience for
    // testing special-mayor-only perks (Perkpocalypse/Statspocalypse/Jerrypocalypse, Bribe, etc.)
    // without waiting for a real special year (every 8th SkyBlock year) to land on the right mayor.
    public static SkyBlockMayor forceElectSpecialMayor() {
        List<SkyBlockMayor> specials = SkyBlockMayor.getSpecialMayors();
        if (specials.isEmpty()) return null;

        SkyBlockMayor chosen = specials.get(electionData.getSpecialCandidateIndex() % specials.size());
        electionData.setSpecialCandidateIndex(electionData.getSpecialCandidateIndex() + 1);
        return forceElectMayor(chosen);
    }

    // Admin-only testing helper backing /adminbypass forcemayor: installs the given mayor with all of
    // their perks, bypassing the vote, so a perk such as Diana's Mythological Ritual can be tested.
    public static SkyBlockMayor forceElectMayor(SkyBlockMayor chosen) {
        electionData.setElectionOpen(false);
        electionData.getCandidates().clear();
        electionData.setCurrentMayor(chosen.name());
        electionData.setCurrentMayorColor(ElectionData.wireColorForIndex(0));
        electionData.setCurrentMayorPerks(Arrays.stream(chosen.getAllPerks()).map(Enum::name).toList());
        electionData.setMayorElectedYear(SkyBlockCalendar.getYear());
        electionData.getLastElectedYear().put(chosen.name(), SkyBlockCalendar.getYear());

        String serialized = GSON.toJson(electionData);
        try {
            ForceElectMayorProtocol.ForceElectMayorResponse response =
                SERVICE.<ForceElectMayorProtocol.ForceElectMayorMessage,
                    ForceElectMayorProtocol.ForceElectMayorResponse>handleRequest(
                    new ForceElectMayorProtocol.ForceElectMayorMessage(serialized)
                ).join();

            if (response.serializedData() != null) {
                electionData = GSON.fromJson(response.serializedData(), ElectionData.class);
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to force-elect special mayor via service");
        }

        playerVoteCache.clear();
        return chosen;
    }

    // Vote weight: Fame Rank tier + highest owned Voter's Badge tier, doubled if holding an
    // Artifact of Control. Matches the SkyBlock Wiki's Mayor Election voting formula.
    public static long computeVoteWeight(net.swofty.type.skyblockgeneric.user.SkyBlockPlayer player) {
        long weight = FameRank.forFame(player.getFame()).getVotes();

        int badgeTier = -1;
        boolean hasArtifactOfControl = false;
        for (net.swofty.type.skyblockgeneric.item.SkyBlockItem accessory : AccessoryOwnership.activeItems(player)) {
            net.swofty.commons.skyblock.item.ItemType type = accessory.getItemType();
            if (type == net.swofty.commons.skyblock.item.ItemType.ARTIFACT_OF_CONTROL) {
                hasArtifactOfControl = true;
            } else {
                badgeTier = Math.max(badgeTier, VOTER_BADGES.indexOf(type));
            }
        }
        if (badgeTier >= 0) weight += (badgeTier + 1) * 3L;
        if (hasArtifactOfControl) weight *= 2;

        return weight;
    }

    public static CompletableFuture<Void> castVote(net.swofty.type.skyblockgeneric.user.SkyBlockPlayer player, String candidateName) {
        if (!electionData.isElectionOpen()) return CompletableFuture.completedFuture(null);

        boolean validCandidate = electionData.getCandidates().stream()
            .anyMatch(c -> c.getMayorName().equals(candidateName));
        if (!validCandidate) return CompletableFuture.completedFuture(null);

        UUID accountId = player.getUuid();
        long voteWeight = computeVoteWeight(player);

        return SERVICE.<CastVoteProtocol.CastVoteMessage,
            CastVoteProtocol.CastVoteResponse>handleRequest(
            new CastVoteProtocol.CastVoteMessage(accountId, candidateName, voteWeight)
        ).thenAccept(response -> {
            if (response.success()) {
                playerVoteCache.put(accountId, candidateName);
                if ("Marina".equals(candidateName)) player.getAchievementHandler().completeAchievement("skyblock.cool_off");
                if (response.tallies() != null) {
                    electionData.updateTallies(response.tallies());
                }
            }
        }).exceptionally(e -> {
            Logger.error(e, "Failed to cast vote via service");
            return null;
        });
    }

    public static String getPlayerVote(UUID accountId) {
        return playerVoteCache.get(accountId);
    }

    public static CompletableFuture<String> fetchPlayerVote(UUID accountId) {
        String cached = playerVoteCache.get(accountId);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        return SERVICE.<GetPlayerVoteProtocol.GetPlayerVoteMessage,
            GetPlayerVoteProtocol.GetPlayerVoteResponse>handleRequest(
            new GetPlayerVoteProtocol.GetPlayerVoteMessage(accountId)
        ).thenApply(response -> {
            if (response.candidateName() != null) {
                playerVoteCache.put(accountId, response.candidateName());
            }
            return response.candidateName();
        });
    }

    public static void clearPlayerVoteCache(UUID accountId) {
        playerVoteCache.remove(accountId);
    }

    public static SkyBlockMayor getCurrentMayor() {
        return electionData.getMayorEnum();
    }

    public static SkyBlockMayor getCurrentMinister() {
        return electionData.getMinisterEnum();
    }

    public static boolean isMayorPerkActive(SkyBlockMayor.Perk perk) {
        return ElectionPerks.current().isMayorPerk(perk);
    }

    public static boolean isMinisterPerkActive(SkyBlockMayor.Perk perk) {
        return ElectionPerks.current().isMinisterPerk(perk);
    }

    public static boolean isPerkActive(SkyBlockMayor.Perk perk) {
        return ElectionPerks.isActive(perk);
    }

    public static Set<SkyBlockMayor.Perk> getPerkpocalypseActivePerks() {
        SkyBlockMayor source = getPerkpocalypseSourceMayor();
        return source == null ? Set.of() : Set.of(source.getAllPerks());
    }

    public static SkyBlockMayor getPerkpocalypseSourceMayor() {
        ElectionData data = electionData;
        if (data.getMayorEnum() != SkyBlockMayor.JERRY) return null;
        if (!data.getCurrentMayorPerks().contains(SkyBlockMayor.Perk.PERKPOCALYPSE.name())) return null;
        return PerkpocalypseRotation.mayorAt(data.getMayorElectedYear(), SkyBlockCalendar.getElapsed());
    }

    public static void triggerPerkpocalypseIfEligible() {
        SkyBlockMayor chosen = getPerkpocalypseSourceMayor();
        if (chosen == null) return;
        Logger.info("Perkpocalypse activated {}'s perks", chosen.getDisplayName());
        Text announcement = Text.parseLenient(
                "<c><l>PERKPOCALYPSE! <7>Mayor <a>" + chosen.getDisplayName() + "<7>'s perks have been activated!");
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            player.sendMessage(announcement.asComponent());
        }
    }

    public static CompletableFuture<List<GetCandidatesProtocol.CandidateInfo>> fetchCandidates() {
        return SERVICE.<GetCandidatesProtocol.GetCandidatesMessage,
            GetCandidatesProtocol.GetCandidatesResponse>handleRequest(
            new GetCandidatesProtocol.GetCandidatesMessage()
        ).thenApply(GetCandidatesProtocol.GetCandidatesResponse::candidates);
    }

    // Real SkyBlock Wiki-documented cycle: voting opens Late Summer 27th and stays open until Late
    // Spring 27th of the FOLLOWING year - a ~9 month window that spans the SkyBlock year boundary,
    // not a short same-year window. Mirrored in CalendarEvent.ELECTION_OPEN/ELECTION_CLOSE for the
    // player-facing Calendar GUI.
    private static final int ELECTION_OPEN_MONTH = 6;
    private static final int ELECTION_OPEN_DAY = 27;
    private static final int ELECTION_CLOSE_MONTH = 3;
    private static final int ELECTION_CLOSE_DAY = 27;

    public static void checkElectionCycle() {
        int currentMonth = SkyBlockCalendar.getMonth();
        int currentDay = SkyBlockCalendar.getDay();

        if (!electionData.isElectionOpen()
                && currentMonth == ELECTION_OPEN_MONTH && currentDay == ELECTION_OPEN_DAY) {
            onElectionStart();
        }

        if (electionData.isElectionOpen()
                && currentMonth == ELECTION_CLOSE_MONTH && currentDay == ELECTION_CLOSE_DAY) {
            onElectionEnd();
        }
    }

    private static void startTallyRefreshTask() {
        if (!tallyRefreshStarted.compareAndSet(false, true)) return;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (electionData.isElectionOpen()) {
                refreshTalliesAsync();
            }
            // Each game server type runs its own JVM with its own in-memory electionData, loaded
            // once at boot. A mayor change made on one server (a real election resolving, or an
            // admin force-electing a special mayor) is only persisted to the remote service and
            // this server's own local copy - other servers never hear about it until they poll,
            // so this periodically re-syncs the current mayor/minister state from the service.
            refreshElectionDataAsync();
            return TaskSchedule.seconds(10);
        });
    }

    private static void refreshElectionDataAsync() {
        SERVICE.<GetElectionDataProtocol.GetElectionDataMessage,
            GetElectionDataProtocol.GetElectionDataResponse>handleRequest(
            new GetElectionDataProtocol.GetElectionDataMessage()
        ).thenAccept(response -> {
            if (response.found() && response.serializedData() != null) {
                ElectionData fresh = GSON.fromJson(response.serializedData(), ElectionData.class);
                if (fresh.getCurrentMayor() != null) {
                    electionData = fresh;
                }
            }
        }).exceptionally(e -> {
            Logger.warn("Failed to refresh election data: {}", e.getMessage());
            return null;
        });
    }

    private static void refreshTalliesAsync() {
        SERVICE.<GetCandidatesProtocol.GetCandidatesMessage,
            GetCandidatesProtocol.GetCandidatesResponse>handleRequest(
            new GetCandidatesProtocol.GetCandidatesMessage()
        ).thenAccept(response -> {
            if (response.electionOpen()) {
                Map<String, Long> tallies = new HashMap<>();
                for (GetCandidatesProtocol.CandidateInfo info : response.candidates()) {
                    tallies.put(info.mayorName(), info.votes());
                }
                electionData.updateTallies(tallies);
            }
        }).exceptionally(e -> {
            Logger.warn("Failed to refresh election tallies: {}", e.getMessage());
            return null;
        });
    }
}
