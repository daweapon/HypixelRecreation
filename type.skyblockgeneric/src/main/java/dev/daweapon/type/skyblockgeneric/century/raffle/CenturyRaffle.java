package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockEmblems;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelCause;
import dev.daweapon.type.skyblockgeneric.levels.causes.CenturyEmblemCause;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class CenturyRaffle {
    private static final long REFRESH_MILLIS = 2_000;
    private static final long MINUTE_MILLIS = 60_000;
    private static final Map<UUID, RaffleProfile> PROFILES = new ConcurrentHashMap<>();
    private static final Set<UUID> LOADING = ConcurrentHashMap.newKeySet();
    private static volatile @Nullable String cachedEvent;
    private static volatile @Nullable Boolean announcedActive;

    private CenturyRaffle() {
    }

    public static void initialize() {
        Thread.startVirtualThread(() -> {
            long lastMinute = System.currentTimeMillis();
            while (true) {
                try {
                    Thread.sleep(REFRESH_MILLIS);
                } catch (InterruptedException interrupted) {
                    return;
                }
                try {
                    if (!RaffleStore.available()) continue;
                    CenturyEvent.refresh();
                    syncCache();
                    announceStateChange();
                    long now = System.currentTimeMillis();
                    if (now - lastMinute >= MINUTE_MILLIS) {
                        lastMinute = now;
                        tickOnline();
                        RaffleDraws.run();
                    }
                } catch (Exception exception) {
                    Logger.error(exception, "Raffle of the Century loop failed");
                }
            }
        });
    }

    public static @Nullable RaffleProfile profile(SkyBlockPlayer player) {
        RaffleProfile profile = PROFILES.get(player.getUuid());
        CenturyEvent.Window latest = CenturyEvent.latest();
        if (profile == null || latest == null || !profile.eventId().equals(latest.id())) return null;
        return profile;
    }

    public static @Nullable CenturyTeam team(SkyBlockPlayer player) {
        RaffleProfile profile = profile(player);
        return profile == null ? null : profile.team();
    }

    public static CompletableFuture<@Nullable RaffleProfile> load(SkyBlockPlayer player) {
        CenturyEvent.Window latest = CenturyEvent.latest();
        if (latest == null || !RaffleStore.available()) return CompletableFuture.completedFuture(null);
        return CompletableFuture.supplyAsync(() -> {
            RaffleProfile profile = RaffleStore.load(latest.id(), player.getUuid());
            PROFILES.put(player.getUuid(), profile);
            return profile;
        }, CenturyRaffle::async);
    }

    public static CompletableFuture<@Nullable CenturyTeam> joinTeam(SkyBlockPlayer player) {
        CenturyEvent.Window window = CenturyEvent.active();
        if (window == null || !RaffleStore.available()) return CompletableFuture.completedFuture(null);
        return CompletableFuture.supplyAsync(() -> {
            CenturyTeam picked = CenturyTeam.values()[ThreadLocalRandom.current().nextInt(CenturyTeam.values().length)];
            boolean fresh = RaffleStore.setTeam(window.id(), player.getUuid(), picked);
            RaffleProfile profile = RaffleStore.load(window.id(), player.getUuid());
            PROFILES.put(player.getUuid(), profile);
            CenturyTeam team = profile.team();
            if (fresh && team != null) {
                sync(() -> applyTeamEmblem(player, team, true));
                addTickets(player, profile, window, CenturyRaffleConfig.get().joinTickets(),
                        "<6><l>REZZUS GIFT! <e>You gained <b>+" + CenturyRaffleConfig.get().joinTickets() + " Raffle Tickets<e>!");
            }
            return team;
        }, CenturyRaffle::async);
    }

    public static void grantTickets(SkyBlockPlayer player, int amount, String message) {
        CenturyEvent.Window window = CenturyEvent.active();
        RaffleProfile profile = profile(player);
        if (window == null || profile == null || profile.team() == null || amount <= 0) return;
        async(() -> addTickets(player, profile, window, amount, message));
    }

    private static void addTickets(SkyBlockPlayer player, RaffleProfile profile, CenturyEvent.Window window, int amount, String message) {
        CenturyTeam team = profile.team();
        if (team == null) return;
        long before = profile.tickets();
        long total = RaffleStore.addTickets(window.id(), player.getUuid(), team, amount,
                RaffleClock.currentKeys(window, System.currentTimeMillis()));
        profile.tickets(total);
        sync(() -> {
            if (!player.isOnline()) return;
            player.sendMessage(message);
            for (RaffleMilestone milestone : RaffleMilestone.values()) {
                if (before < milestone.tickets() && total >= milestone.tickets()) {
                    player.sendMessage("<a><l>MILESTONE! <r><a>You reached <6>Raffle Milestone " + milestone.numeral()
                            + "<a>! <e>Open the Raffle Box to claim it.");
                }
            }
            progress(player, RaffleAction.of(RaffleAction.Trigger.TICKET));
        });
    }

    public static void progress(SkyBlockPlayer player, RaffleAction action) {
        CenturyEvent.Window window = CenturyEvent.active();
        RaffleProfile profile = profile(player);
        if (window == null || profile == null || profile.team() == null) return;
        int period = RaffleClock.taskPeriod(window, System.currentTimeMillis());
        for (RaffleTask task : RaffleTasks.forPeriod(window.id(), period)) {
            if (profile.taskDone(period, task) || !task.matches(action)) continue;
            profile.completeTask(period, task);
            async(() -> {
                if (!RaffleStore.claimOnce(window.id(), player.getUuid(), RaffleProfile.TASK_PREFIX + RaffleProfile.taskKey(period, task))) return;
                sync(() -> {
                    if (!player.isOnline()) return;
                    player.addAndUpdateItem(randomSlice(player.getUuid()));
                    grantTickets(player, 1, "<6><l>RAFFLE TASK! <e>You completed the <a>" + task.description()
                            + " <e>raffle task and earned <b>+1 Raffle Ticket <e>and a slice of cake!");
                });
            });
        }
    }

    public static ItemType randomSlice(@Nullable UUID player) {
        return LootRoll.pick(player, List.of(CenturyTeam.values()), CenturyTeam::slice, _ -> 1D, ThreadLocalRandom.current()).slice();
    }

    static void applyTeamEmblem(SkyBlockPlayer player, CenturyTeam team, boolean select) {
        unlockEmblem(player, CenturyEmblemCause.teamKey(team.name()));
        if (!select) return;
        for (SkyBlockEmblems.SkyBlockEmblem emblem : SkyBlockEmblems.SPECIAL.getEmblems()) {
            if (emblem.displayName().equals(team.emblemName())) {
                player.getSkyBlockExperience().setEmblem(SkyBlockEmblems.SPECIAL, emblem);
            }
        }
    }

    static void unlockEmblem(SkyBlockPlayer player, String key) {
        CenturyEmblemCause cause = SkyBlockLevelCause.getCenturyEmblemCause(key);
        if (cause == null || player.hasUnlockedXPCause(cause)) return;
        player.getSkyBlockExperience().addExperience(cause);
    }

    private static void syncCache() {
        CenturyEvent.Window latest = CenturyEvent.latest();
        String id = latest == null ? null : latest.id();
        if (id == null ? cachedEvent != null : !id.equals(cachedEvent)) {
            PROFILES.clear();
            cachedEvent = id;
        }
        if (id == null) return;
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            UUID uuid = player.getUuid();
            if (PROFILES.containsKey(uuid) || !LOADING.add(uuid)) continue;
            try {
                RaffleProfile profile = RaffleStore.load(id, uuid);
                PROFILES.put(uuid, profile);
                CenturyTeam team = profile.team();
                if (team != null) sync(() -> applyTeamEmblem(player, team, false));
            } finally {
                LOADING.remove(uuid);
            }
        }
        PROFILES.keySet().removeIf(uuid -> MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid) == null);
    }

    private static void tickOnline() {
        CenturyEvent.Window window = CenturyEvent.active();
        if (window == null) return;
        int every = Math.max(1, CenturyRaffleConfig.get().onlineTicketMinutes());
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            RaffleProfile profile = profile(player);
            if (profile == null || profile.team() == null) continue;
            long minutes = RaffleStore.addOnlineMinute(window.id(), player.getUuid());
            profile.onlineMinutes(minutes);
            if (minutes % every == 0) {
                addTickets(player, profile, window, 1, "<6><l>PLAYTIME! <e>You gained <b>+1 Raffle Ticket<e>!");
            }
        }
    }

    private static void announceStateChange() {
        boolean active = CenturyEvent.isActive();
        Boolean previous = announcedActive;
        announcedActive = active;
        if (previous == null || previous == active) return;
        String message = active
                ? "<d><l>CENTURY CELEBRATION! <r><e>The <6>Raffle of the Century <e>has begun! Talk to <a>Rezzus <e>in the Hub to join a team!"
                : "<d><l>CENTURY CELEBRATION! <r><e>The <6>Raffle of the Century <e>has ended! Visit the <6>Raffle Box <e>to claim your prizes.";
        sync(() -> SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> player.sendMessage(message)));
    }

    public static void sync(Runnable task) {
        MinecraftServer.getSchedulerManager().scheduleNextTick(task);
    }

    public static void async(Runnable task) {
        Thread.startVirtualThread(() -> {
            try {
                task.run();
            } catch (Exception exception) {
                Logger.error(exception, "Raffle of the Century task failed");
            }
        });
    }
}
