package dev.daweapon.service.api.control.admin;

import dev.daweapon.commons.economy.CoinFlowStore;
import dev.daweapon.commons.economy.CoinFlowStore.Day;
import dev.daweapon.commons.economy.CoinFlowStore.Delta;
import dev.daweapon.commons.economy.CoinFlowStore.Kind;
import dev.daweapon.commons.economy.CoinFlowStore.Supply;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Money created (sources) against money destroyed (sinks). Transfers between players move coins
// without changing the supply, and staff tools are reported apart so testing does not read as inflation.
public final class InflationReport {
    public record DayRow(String date, double minted, double burned, double net, double staff,
                         @Nullable Double supply) {}

    public record Line(String category, double amount, long count, double share) {}

    public record Channel(String channel, double received, double paid, double net, long count) {}

    public record Report(int days, double minted, double burned, double net, double staffNet, double averageDailyNet,
                         @Nullable Double supplyNow, @Nullable Double supplyAtStart, @Nullable Double supplyChangePercent,
                         @Nullable Double netAsPercentOfSupply, double sinkToSourceRatio, List<DayRow> daily,
                         List<Line> sources, List<Line> sinks, List<Channel> transfers, List<Line> staff,
                         @Nullable Supply latestSupply) {}

    private InflationReport() {}

    public static Report build(List<Day> days, Map<LocalDate, Supply> supply) {
        Map<String, double[]> sources = new LinkedHashMap<>();
        Map<String, double[]> sinks = new LinkedHashMap<>();
        Map<String, double[]> staff = new LinkedHashMap<>();
        Map<String, double[]> channels = new LinkedHashMap<>();
        List<DayRow> daily = new ArrayList<>();
        double minted = 0;
        double burned = 0;
        double staffNet = 0;

        for (Day day : days) {
            double dayMinted = 0;
            double dayBurned = 0;
            double dayStaff = 0;
            for (Map.Entry<String, Delta> entry : day.flows().entrySet()) {
                String[] parts = entry.getKey().split("\\|", 2);
                if (parts.length != 2) continue;
                Kind kind;
                try {
                    kind = Kind.valueOf(parts[0]);
                } catch (IllegalArgumentException unknown) {
                    continue;
                }
                String category = parts[1];
                Delta delta = entry.getValue();
                switch (kind) {
                    case SOURCE -> {
                        dayMinted += delta.amount();
                        accumulate(sources, category, delta.amount(), delta.count());
                    }
                    case SINK -> {
                        dayBurned -= delta.amount();
                        accumulate(sinks, category, -delta.amount(), delta.count());
                    }
                    case STAFF -> {
                        dayStaff += delta.amount();
                        accumulate(staff, category, delta.amount(), delta.count());
                    }
                    case TRANSFER -> {
                        boolean received = category.endsWith(CoinFlowStore.RECEIVED);
                        String channel = received ? strip(category, CoinFlowStore.RECEIVED)
                                : strip(category, CoinFlowStore.PAID);
                        double[] totals = channels.computeIfAbsent(channel, _ -> new double[3]);
                        if (received) totals[0] += delta.amount();
                        else totals[1] -= delta.amount();
                        totals[2] += delta.count();
                    }
                }
            }
            Supply daySupply = supply.get(day.date());
            daily.add(new DayRow(day.date().toString(), dayMinted, dayBurned, dayMinted - dayBurned, dayStaff,
                    daySupply == null ? null : daySupply.total()));
            minted += dayMinted;
            burned += dayBurned;
            staffNet += dayStaff;
        }

        double net = minted - burned;
        Supply latest = latest(supply, days.isEmpty() ? null : days.getLast().date());
        Supply earliest = earliest(supply, days.isEmpty() ? null : days.getFirst().date());
        Double supplyNow = latest == null ? null : latest.total();
        Double supplyAtStart = earliest == null ? null : earliest.total();
        Double change = supplyNow != null && supplyAtStart != null && supplyAtStart > 0
                ? (supplyNow - supplyAtStart) / supplyAtStart * 100 : null;
        Double netShare = null;
        if (supplyAtStart != null && supplyAtStart > 0) netShare = net / supplyAtStart * 100;
        else if (supplyNow != null && supplyNow > 0) netShare = net / supplyNow * 100;

        List<Channel> transferList = new ArrayList<>();
        channels.forEach((channel, totals) -> transferList.add(
                new Channel(channel, totals[0], totals[1], totals[0] - totals[1], (long) totals[2])));
        transferList.sort(Comparator.comparingDouble((Channel channel) -> channel.received() + channel.paid()).reversed());

        return new Report(days.size(), minted, burned, net, staffNet, days.isEmpty() ? 0 : net / days.size(),
                supplyNow, supplyAtStart, change, netShare, minted <= 0 ? 0 : burned / minted, daily,
                lines(sources, minted), lines(sinks, burned), transferList, lines(staff, 0), latest);
    }

    private static void accumulate(Map<String, double[]> totals, String category, double amount, long count) {
        double[] values = totals.computeIfAbsent(category, _ -> new double[2]);
        values[0] += amount;
        values[1] += count;
    }

    private static List<Line> lines(Map<String, double[]> totals, double whole) {
        List<Line> lines = new ArrayList<>();
        totals.forEach((category, values) -> lines.add(new Line(category, values[0], (long) values[1],
                whole > 0 ? values[0] / whole * 100 : 0)));
        lines.sort(Comparator.comparingDouble(Line::amount).reversed());
        return lines;
    }

    private static String strip(String category, String suffix) {
        return category.endsWith(suffix) ? category.substring(0, category.length() - suffix.length()) : category;
    }

    private static @Nullable Supply latest(Map<LocalDate, Supply> supply, @Nullable LocalDate upTo) {
        return supply.values().stream()
                .filter(entry -> upTo == null || !entry.date().isAfter(upTo))
                .max(Comparator.comparing(Supply::date)).orElse(null);
    }

    private static @Nullable Supply earliest(Map<LocalDate, Supply> supply, @Nullable LocalDate from) {
        return supply.values().stream()
                .filter(entry -> from == null || !entry.date().isBefore(from))
                .min(Comparator.comparing(Supply::date)).orElse(null);
    }
}
