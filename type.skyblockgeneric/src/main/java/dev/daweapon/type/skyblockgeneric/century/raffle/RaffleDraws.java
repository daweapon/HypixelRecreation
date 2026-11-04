package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class RaffleDraws {
    private static final Set<String> KNOWN_DRAWN = ConcurrentHashMap.newKeySet();
    private static volatile boolean primed;

    private RaffleDraws() {
    }

    static void run() {
        CenturyEvent.Window window = CenturyEvent.latest();
        if (window == null) return;
        long now = System.currentTimeMillis();
        for (RaffleType type : RaffleType.values()) {
            int count = RaffleClock.count(window, type);
            for (int index = 0; index < count; index++) {
                if (RaffleClock.end(window, type, index) > now) break;
                settle(window.id(), type.key(index), primed);
            }
        }
        primed = true;
    }

    public static boolean forceDraw(RaffleType type) {
        CenturyEvent.Window window = CenturyEvent.active();
        if (window == null || !RaffleStore.available()) return false;
        String raffleKey = type.key(RaffleClock.index(window, type, System.currentTimeMillis()));
        CenturyRaffle.async(() -> settle(window.id(), raffleKey, true));
        return true;
    }

    private static void settle(String eventId, String raffleKey, boolean announce) {
        String memo = eventId + "/" + raffleKey;
        if (KNOWN_DRAWN.contains(memo)) return;
        String drawn = RaffleStore.drawn(eventId, raffleKey);
        if (drawn == null && RaffleStore.lockDraw(eventId, raffleKey)) {
            draw(eventId, raffleKey);
            drawn = RaffleStore.drawn(eventId, raffleKey);
        }
        if (drawn == null) return;
        KNOWN_DRAWN.add(memo);
        if (announce) announce(raffleKey, drawn);
    }

    static void draw(String eventId, String raffleKey) {
        RaffleType type = RaffleClock.typeOf(raffleKey);
        Map<UUID, Long> entries = RaffleStore.entries(eventId, raffleKey);
        Map<CenturyTeam, Long> teams = RaffleStore.teamTotals(eventId, raffleKey);
        long total = entries.values().stream().mapToLong(Long::longValue).sum();
        Map<UUID, PrizeTier> winners = pickWinners(type, entries, CenturyRaffleConfig.get());
        RaffleStore.finishDraw(eventId, raffleKey, winners, total, topTeam(teams));
    }

    static Map<UUID, PrizeTier> pickWinners(RaffleType type, Map<UUID, Long> entries, CenturyRaffleConfig config) {
        Map<UUID, PrizeTier> winners = new HashMap<>();
        List<UUID> remaining = new ArrayList<>(entries.keySet());
        for (PrizeTier tier : PrizeTier.values()) {
            double expected = config.prizeCount(type, tier) * (double) entries.size() / config.referenceParticipants(type);
            int count = (int) expected;
            if (LootRoll.roll(null, null, (expected - count) * 100)) count++;
            for (int i = 0; i < count && !remaining.isEmpty(); i++) {
                UUID winner = LootRoll.pick(null, remaining, _ -> null, uuid -> entries.get(uuid), ThreadLocalRandom.current());
                remaining.remove(winner);
                winners.put(winner, tier);
            }
        }
        return winners;
    }

    static @Nullable CenturyTeam topTeam(Map<CenturyTeam, Long> teams) {
        return teams.entrySet().stream().filter(entry -> entry.getValue() > 0)
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    private static void announce(String raffleKey, String drawn) {
        String[] parts = drawn.split("\\|", -1);
        long total = RaffleProfile.parse(parts[0]);
        CenturyTeam top = parts.length > 1 ? CenturyTeam.parse(parts[1]) : null;
        String label = RaffleClock.label(raffleKey);
        CenturyRaffle.sync(() -> {
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                if (CenturyRaffle.team(player) == null) continue;
                player.sendMessage("<6><l>RAFFLE! <8>" + label + " <e>has been drawn! A total of <b>" + total
                        + " <e>tickets were entered" + (top == null ? "" : ", with " + top.display() + " <e>having the most entries") + "!");
                player.sendMessage("<6><l>RAFFLE! <e>Check the <6>Raffle Box <e>or run <a>/century <e>to see if you won!");
            }
        });
    }
}
