package dev.daweapon.type.skyblockgeneric.century.raffle;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RaffleSnapshot(CenturyEvent.Window window, boolean active, RaffleProfile profile,
                             Map<RaffleType, Current> current, List<Row> history,
                             Map<CenturyTeam, Long> bigOneTeams, boolean mementoClaimed) {

    public record Current(String key, String label, long entered, long total, long drawAt) {
    }

    public record Row(String key, String label, long entered, @Nullable PrizeTier tier, boolean drawn, boolean claimed, long endedAt) {
        public boolean claimable() {
            return drawn && !claimed && (tier != null || entered > 0);
        }
    }

    public @Nullable CenturyTeam leadingTeam() {
        return RaffleDraws.topTeam(bigOneTeams);
    }

    static @Nullable RaffleSnapshot load(UUID uuid) {
        CenturyEvent.Window window = CenturyEvent.latest();
        if (window == null || !RaffleStore.available()) return null;
        long now = System.currentTimeMillis();
        boolean active = window.contains(now);
        RaffleProfile profile = RaffleStore.load(window.id(), uuid);
        Map<String, String> results = RaffleStore.results(window.id(), uuid);
        Map<String, String> claimed = RaffleStore.claimed(window.id(), uuid);
        Map<RaffleType, Current> current = new EnumMap<>(RaffleType.class);
        List<Row> history = new ArrayList<>();
        for (RaffleType type : RaffleType.values()) {
            int count = RaffleClock.count(window, type);
            for (int index = 0; index < count; index++) {
                String key = type.key(index);
                long end = RaffleClock.end(window, type, index);
                String label = type.label(index + 1);
                if (end > now) {
                    if (active && index == RaffleClock.index(window, type, now)) {
                        long total = RaffleStore.teamTotals(window.id(), key).values().stream().mapToLong(Long::longValue).sum();
                        current.put(type, new Current(key, label, RaffleStore.entered(window.id(), key, uuid), total, end));
                    }
                    break;
                }
                long entered = RaffleStore.entered(window.id(), key, uuid);
                if (entered <= 0 && !results.containsKey(key)) continue;
                String rawTier = results.get(key);
                PrizeTier tier = rawTier == null ? null : PrizeTier.valueOf(rawTier);
                boolean drawn = rawTier != null || RaffleStore.drawn(window.id(), key) != null;
                history.add(new Row(key, label, entered, tier, drawn, claimed.containsKey(key), end));
            }
        }
        history.sort(Comparator.comparing((Row row) -> !row.claimable()).thenComparing(Row::endedAt, Comparator.reverseOrder()));
        Map<CenturyTeam, Long> teams = RaffleStore.teamTotals(window.id(), RaffleType.BIG_ONE.key(0));
        return new RaffleSnapshot(window, active, profile, current, history, teams, profile.flagged(RaffleProfile.MEMENTO));
    }
}
