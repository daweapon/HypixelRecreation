package net.swofty.type.skyblockgeneric.crimson.kuudra.queue;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.UnderstandableProxyServer;
import net.swofty.commons.party.FullParty;
import net.swofty.commons.protocol.objects.orchestrator.ChooseGameProtocol;
import net.swofty.commons.protocol.objects.orchestrator.GetServerForMapProtocol;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.party.PartyManager;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.partyfinder.KuudraPartyFinder;
import net.swofty.type.skyblockgeneric.gui.inventories.kuudra.GUIKuudraNeedPartyLeader;
import net.swofty.type.skyblockgeneric.user.ChallengeSidebar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Sends a party (or a solo player) from any SkyBlock server into a fresh Kuudra run: the leader queues, every
 * member is checked, the orchestrator creates the run on a Kuudra server, each member is registered for it,
 * then everyone transfers.
 */
public final class KuudraQueue {
    public static final int START_DELAY_SECONDS = 4;
    private static final long REQUEST_TIMEOUT_SECONDS = 25;
    private static final long REGISTRATION_TIMEOUT_SECONDS = 3;
    private static final long QUEUE_LIFETIME_MILLIS = 60_000;
    private static final ProxyService ORCHESTRATOR = new ProxyService(ServiceType.ORCHESTRATOR);
    private static final Map<UUID, Entry> QUEUED = new ConcurrentHashMap<>();

    static {
        ChallengeSidebar.register(KuudraQueue::sidebar);
    }

    private KuudraQueue() {
    }

    private record Entry(KuudraTier tier, long since) {
    }

    public static boolean isQueued(UUID player) {
        Entry entry = QUEUED.get(player);
        if (entry == null) return false;
        if (System.currentTimeMillis() - entry.since() < QUEUE_LIFETIME_MILLIS) return true;
        QUEUED.remove(player);
        return false;
    }

    public static void queue(SkyBlockPlayer leader, KuudraTier tier) {
        queue(leader, tier, false);
    }

    /** Elle's first trip to Kuudra: a Basic run that doesn't need "Have Elle take you to Kuudra!". */
    public static void queueWithElle(SkyBlockPlayer leader) {
        queue(leader, KuudraTier.BASIC, true);
    }

    private static void queue(SkyBlockPlayer leader, KuudraTier tier, boolean elle) {
        if (!KuudraAvailability.canQueue(leader)) {
            leader.sendMessage(KuudraAvailability.UNAVAILABLE_CHAT);
            return;
        }
        if (isQueued(leader.getUuid())) {
            leader.sendMessage("<c>You are already queued for Kuudra!");
            return;
        }
        Thread.startVirtualThread(() -> {
            try {
                prepare(leader, tier, elle);
            } catch (Exception exception) {
                Logger.error(exception, "Kuudra queue failed for {}", leader.getUsername());
                leader.sendMessage("<c>Something went wrong while queueing for Kuudra. Please try again.");
            }
        });
    }

    private static void prepare(SkyBlockPlayer leader, KuudraTier tier, boolean elle) {
        boolean inParty = PartyManager.isInParty(leader);
        FullParty party = inParty ? PartyManager.getPartyFromPlayer(leader) : null;
        if (inParty && party == null) {
            leader.sendMessage("<c>Failed to read your party. Please try again.");
            return;
        }
        List<UUID> roster = party == null ? List.of(leader.getUuid())
                : party.getMembers().stream().map(FullParty.Member::getUuid).toList();
        boolean isLeader = party == null || party.getLeader().getUuid().equals(leader.getUuid());
        if (!isLeader) {
            MinecraftServer.getSchedulerManager().scheduleNextTick(() -> leader.openView(new GUIKuudraNeedPartyLeader()));
            return;
        }

        List<SkyBlockPlayer> members = new ArrayList<>();
        List<KuudraPartyCheck.Member> checks = new ArrayList<>();
        for (UUID uuid : roster) {
            SkyBlockPlayer member = online(uuid);
            if (member != null) members.add(member);
            String name = member != null ? member.getUsername() : HypixelDataHandler.getPotentialIGNFromUUID(uuid);
            checks.add(new KuudraPartyCheck.Member(name, uuid.equals(leader.getUuid()), member != null,
                    member != null && (elle || KuudraProgress.meetsRequirements(member, tier))));
        }
        List<String> problems = KuudraPartyCheck.problems(isLeader, checks, tier.getDisplayName());
        if (!problems.isEmpty()) {
            problems.forEach(leader::sendMessage);
            return;
        }
        for (SkyBlockPlayer member : members) {
            if (member != leader && isQueued(member.getUuid())) {
                leader.sendMessage("<c>" + member.getUsername() + " is already queued for Kuudra!");
                return;
            }
        }

        long now = System.currentTimeMillis();
        members.forEach(member -> QUEUED.put(member.getUuid(), new Entry(tier, now)));
        members.forEach(member -> member.sendMessage("<e>Starting in " + START_DELAY_SECONDS + " seconds..."));
        MinecraftServer.getSchedulerManager().buildTask(() -> Thread.startVirtualThread(() -> send(members, roster, tier)))
                .delay(TaskSchedule.seconds(START_DELAY_SECONDS))
                .schedule();
    }

    private static void send(List<SkyBlockPlayer> members, List<UUID> roster, KuudraTier tier) {
        try {
            List<SkyBlockPlayer> present = members.stream().filter(SkyBlockPlayer::isOnline).toList();
            if (present.size() != members.size()) {
                fail(present, "<c>A party member left, so the queue was cancelled.");
                return;
            }
            GetServerForMapProtocol.GetServerForMapResponse response = requestRun(roster, tier);
            UnderstandableProxyServer server = response == null ? null : response.server();
            if (server == null || response.gameId() == null) {
                Logger.warn("Kuudra run request failed: {}", response == null ? "no response" : response.error());
                fail(present, "<c>Couldn't find a Kuudra server! Please try again later.");
                return;
            }
            register(present, server, response.gameId());
            delist(roster);
            present.forEach(member -> member.sendMessage("<7>Sending to server " + server.shortName() + "..."));
            for (SkyBlockPlayer member : present) {
                if (!member.isOnline()) continue;
                member.asProxyPlayer().transferToWithIndication(server.uuid())
                        .whenComplete((_, _) -> QUEUED.remove(member.getUuid()));
            }
        } catch (Exception exception) {
            Logger.error(exception, "Kuudra queue send failed");
            fail(members, "<c>Something went wrong while queueing for Kuudra. Please try again.");
        }
    }

    private static @Nullable GetServerForMapProtocol.GetServerForMapResponse requestRun(List<UUID> roster, KuudraTier tier) {
        if (!ORCHESTRATOR.isOnline().join()) return null;
        GetServerForMapProtocol.GetServerForMapMessage message = new GetServerForMapProtocol.GetServerForMapMessage(
                ServerType.SKYBLOCK_KUUDRA, KuudraRoster.encode(roster), tier.name(), roster.size());
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
            Logger.warn("Some Kuudra run registrations timed out; the run also knows its roster, so continuing");
        }
    }

    private static void delist(List<UUID> roster) {
        try {
            roster.forEach(KuudraPartyFinder::leave);
        } catch (Exception exception) {
            Logger.warn(exception, "Couldn't take a queued Kuudra party out of the Party Finder");
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
                "<f>Queued: <a>Kuudra",
                "<f>Tier: <e>" + entry.tier().getDisplayName(),
                "<f>Since: <a>" + String.format("%02d:%02d", seconds / 60, seconds % 60));
    }
}
