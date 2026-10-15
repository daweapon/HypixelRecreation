package dev.daweapon.type.skyblockgeneric.dungeons.queue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.UnderstandableProxyServer;
import dev.daweapon.commons.party.FullParty;
import dev.daweapon.commons.protocol.objects.orchestrator.ChooseGameProtocol;
import dev.daweapon.commons.protocol.objects.orchestrator.GetServerForMapProtocol;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.proxyapi.ProxyService;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.party.PartyManager;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsAvailability;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsFloorIds;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsRequirements;
import dev.daweapon.type.skyblockgeneric.dungeons.partyfinder.CatacombsPartyFinder;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.queue.KuudraQueue;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.queue.KuudraRoster;
import dev.daweapon.type.skyblockgeneric.user.ChallengeSidebar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sends a party (or a solo player) from any SkyBlock server into a fresh Catacombs run: the leader queues, every
 * member is checked, the orchestrator creates the run on a Catacombs server (waiting in its queue for up to 45 s
 * when every server is busy), each member is registered for it, then everyone transfers.
 */
public final class CatacombsQueue {
    public static final int START_DELAY_SECONDS = KuudraQueue.START_DELAY_SECONDS;
    private static final long REQUEST_TIMEOUT_SECONDS = 75;
    private static final long REGISTRATION_TIMEOUT_SECONDS = 3;
    private static final int BUSY_NOTICE_SECONDS = 6;
    private static final long QUEUE_LIFETIME_MILLIS = 120_000;
    private static final ProxyService ORCHESTRATOR = new ProxyService(ServiceType.ORCHESTRATOR);
    private static final Map<UUID, Entry> QUEUED = new ConcurrentHashMap<>();

    static {
        ChallengeSidebar.register(CatacombsQueue::sidebar);
    }

    private CatacombsQueue() {
    }

    private record Entry(CatacombsFloor floor, long since) {
    }

    public static String entered(String leaderName, CatacombsFloor floor) {
        return "<a>" + leaderName + " <e>entered <c>The Catacombs<e>, <b>" + floor.displayName() + "<e>!";
    }

    public static boolean isQueued(UUID player) {
        Entry entry = QUEUED.get(player);
        if (entry == null) return false;
        if (System.currentTimeMillis() - entry.since() < QUEUE_LIFETIME_MILLIS) return true;
        QUEUED.remove(player);
        return false;
    }

    public static void queue(SkyBlockPlayer leader, CatacombsFloor floor) {
        if (CatacombsAvailability.blockIfUnavailable(leader, floor)) return;
        if (isQueued(leader.getUuid())) {
            leader.sendMessage("<c>You are already queued for The Catacombs!");
            return;
        }
        Thread.startVirtualThread(() -> {
            try {
                prepare(leader, floor);
            } catch (Exception exception) {
                Logger.error(exception, "Catacombs queue failed for {}", leader.getUsername());
                leader.sendMessage("<c>Something went wrong while queueing for The Catacombs. Please try again.");
            }
        });
    }

    private static void prepare(SkyBlockPlayer leader, CatacombsFloor floor) {
        boolean inParty = PartyManager.isInParty(leader);
        FullParty party = inParty ? PartyManager.getPartyFromPlayer(leader) : null;
        if (inParty && party == null) {
            leader.sendMessage("<c>Failed to read your party. Please try again.");
            return;
        }
        List<UUID> roster = party == null ? List.of(leader.getUuid())
                : KuudraQueue.leaderFirst(leader.getUuid(), party.getMembers().stream().map(FullParty.Member::getUuid).toList());
        boolean isLeader = party == null || party.getLeader().getUuid().equals(leader.getUuid());

        List<SkyBlockPlayer> members = new ArrayList<>();
        List<CatacombsPartyCheck.Member> checks = new ArrayList<>();
        for (UUID uuid : roster) {
            SkyBlockPlayer member = online(uuid);
            if (member != null) members.add(member);
            String name = member != null ? member.getUsername() : HypixelDataHandler.getPotentialIGNFromUUID(uuid);
            checks.add(new CatacombsPartyCheck.Member(name, uuid.equals(leader.getUuid()), member != null,
                    member != null && CatacombsRequirements.meets(member, floor)));
        }
        List<String> problems = CatacombsPartyCheck.problems(isLeader, checks, "The Catacombs, " + floor.displayName());
        if (!problems.isEmpty()) {
            problems.forEach(leader::sendMessage);
            return;
        }
        for (SkyBlockPlayer member : members) {
            if (member != leader && isQueued(member.getUuid())) {
                leader.sendMessage("<c>" + member.getUsername() + " is already queued for The Catacombs!");
                return;
            }
        }

        long now = System.currentTimeMillis();
        members.forEach(member -> QUEUED.put(member.getUuid(), new Entry(floor, now)));
        members.forEach(member -> member.sendMessage("<e>Starting in " + START_DELAY_SECONDS + " seconds..."));
        MinecraftServer.getSchedulerManager().buildTask(() -> Thread.startVirtualThread(() -> send(leader, members, roster, floor)))
                .delay(TaskSchedule.seconds(START_DELAY_SECONDS))
                .schedule();
    }

    private static void send(SkyBlockPlayer leader, List<SkyBlockPlayer> members, List<UUID> roster, CatacombsFloor floor) {
        try {
            List<SkyBlockPlayer> present = members.stream().filter(SkyBlockPlayer::isOnline).toList();
            if (present.size() != members.size()) {
                fail(present, "<c>A party member left, so the queue was cancelled.");
                return;
            }
            AtomicBoolean answered = new AtomicBoolean();
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                if (answered.get()) return;
                present.stream().filter(SkyBlockPlayer::isOnline)
                        .forEach(member -> member.sendMessage("<e>All Catacombs servers are busy. You are in the queue and will be sent as soon as one frees up."));
            }).delay(TaskSchedule.seconds(BUSY_NOTICE_SECONDS)).schedule();
            GetServerForMapProtocol.GetServerForMapResponse response;
            try {
                response = requestRun(roster, floor);
            } finally {
                answered.set(true);
            }
            UnderstandableProxyServer server = response == null ? null : response.server();
            if (server == null || response.gameId() == null) {
                Logger.warn("Catacombs run request failed: {}", response == null ? "no response" : response.error());
                fail(present, failureMessage(response));
                return;
            }
            register(present, server, response.gameId());
            delist(roster);
            String entered = entered(leader.getUsername(), floor);
            present.forEach(member -> {
                member.sendMessage(entered);
                member.sendMessage("<7>Sending to server " + server.shortName() + "...");
            });
            for (SkyBlockPlayer member : present) {
                if (!member.isOnline()) continue;
                member.asProxyPlayer().transferToWithIndication(server.uuid())
                        .whenComplete((_, _) -> QUEUED.remove(member.getUuid()));
            }
        } catch (Exception exception) {
            Logger.error(exception, "Catacombs queue send failed");
            fail(members, "<c>Something went wrong while queueing for The Catacombs. Please try again.");
        }
    }

    private static String failureMessage(@Nullable GetServerForMapProtocol.GetServerForMapResponse response) {
        String reason = response == null ? null : response.reason();
        if ("FULL".equals(reason)) return "<c>All Catacombs servers are busy right now. Please try again in a moment.";
        return "<c>Couldn't find a Catacombs server! Please try again later.";
    }

    private static @Nullable GetServerForMapProtocol.GetServerForMapResponse requestRun(List<UUID> roster, CatacombsFloor floor) {
        if (!ORCHESTRATOR.isOnline().join()) return null;
        GetServerForMapProtocol.GetServerForMapMessage message = new GetServerForMapProtocol.GetServerForMapMessage(
                ServerType.SKYBLOCK_CATACOMBS, KuudraRoster.encode(roster), CatacombsFloorIds.of(floor), roster.size());
        CompletableFuture<GetServerForMapProtocol.GetServerForMapResponse> future = ORCHESTRATOR.handleRequest(message);
        return future.orTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).exceptionally(_ -> null).join();
    }

    private static void register(List<SkyBlockPlayer> members, UnderstandableProxyServer server, String gameId) {
        List<CompletableFuture<?>> registrations = new ArrayList<>();
        for (SkyBlockPlayer member : members) {
            registrations.add(ORCHESTRATOR.handleRequest(
                    new ChooseGameProtocol.ChooseGameMessage(member.getUuid(), server, gameId)));
        }
        try {
            CompletableFuture.allOf(registrations.toArray(new CompletableFuture[0]))
                    .orTimeout(REGISTRATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .join();
        } catch (Exception exception) {
            Logger.warn("Some Catacombs run registrations timed out; the run also knows its roster, so continuing");
        }
    }

    private static void delist(List<UUID> roster) {
        try {
            roster.forEach(CatacombsPartyFinder::leave);
        } catch (Exception exception) {
            Logger.warn(exception, "Couldn't take a queued Catacombs party out of the Party Finder");
        }
    }

    private static void fail(List<SkyBlockPlayer> members, String message) {
        members.forEach(member -> {
            QUEUED.remove(member.getUuid());
            if (member.isOnline()) member.sendMessage(message);
        });
    }

    private static @Nullable SkyBlockPlayer online(UUID uuid) {
        return MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid) instanceof SkyBlockPlayer player
                ? player : null;
    }

    private static @Nullable List<String> sidebar(SkyBlockPlayer player) {
        if (!isQueued(player.getUuid())) return null;
        Entry entry = QUEUED.get(player.getUuid());
        if (entry == null) return null;
        long seconds = (System.currentTimeMillis() - entry.since()) / 1000;
        return List.of(
                "<f>Queued: <a>The Catacombs",
                "<f>Floor: <e>" + entry.floor().displayName(),
                "<f>Since: <a>" + String.format("%02d:%02d", seconds / 60, seconds % 60));
    }
}
