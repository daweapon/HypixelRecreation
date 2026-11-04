package dev.daweapon.service.control.integrity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Memory budget for the fleet (capability 20): what running containers may claim, plus what a
 * start would add, against the host's RAM less a reserve for the NAS itself.
 *
 * A container without a mem_limit cannot be summed exactly, so it is counted at its JVM heap
 * plus overhead when the heap is known, otherwise at a flat estimate, and flagged as estimated.
 */
public final class CapacityBudget {
    /** JVM resident size runs well above -Xmx once metaspace, threads and buffers are counted. */
    static final double HEAP_OVERHEAD = 1.25;

    public record Container(String name, boolean running, long limitBytes, long heapBytes) {
    }

    public record Entry(String name, boolean running, boolean candidate, long bytes, boolean estimated) {
    }

    public record Budget(long hostBytes, long reserveBytes, long budgetBytes, long committedBytes,
                         long candidateBytes, long projectedBytes, boolean overBudget,
                         int estimatedCount, List<String> candidates, List<Entry> entries, String summary) {
    }

    private CapacityBudget() {
    }

    public static Budget evaluate(long hostBytes, long reserveBytes, long flatEstimateBytes,
                                  List<Container> containers, Set<String> candidates) {
        long budget = Math.max(0, hostBytes - reserveBytes);
        long committed = 0;
        long added = 0;
        int estimated = 0;
        List<Entry> entries = new ArrayList<>();
        List<String> starting = new ArrayList<>();

        for (Container container : containers) {
            boolean candidate = !container.running() && candidates.contains(container.name());
            long bytes;
            boolean isEstimate = container.limitBytes() <= 0;
            if (!isEstimate) {
                bytes = container.limitBytes();
            } else if (container.heapBytes() > 0) {
                bytes = (long) (container.heapBytes() * HEAP_OVERHEAD);
            } else {
                bytes = flatEstimateBytes;
            }

            if (container.running()) {
                committed += bytes;
            } else if (candidate) {
                added += bytes;
                starting.add(container.name());
            } else {
                entries.add(new Entry(container.name(), false, false, bytes, isEstimate));
                continue;
            }
            if (isEstimate) {
                estimated++;
            }
            entries.add(new Entry(container.name(), container.running(), candidate, bytes, isEstimate));
        }

        entries.sort(Comparator.comparing(Entry::running).reversed()
                .thenComparing(Comparator.comparingLong(Entry::bytes).reversed()));

        long projected = committed + added;
        boolean over = projected > budget;
        return new Budget(hostBytes, reserveBytes, budget, committed, added, projected, over,
                estimated, starting, entries, summarise(budget, committed, added, projected, over, starting));
    }

    private static String summarise(long budget, long committed, long added, long projected,
                                    boolean over, List<String> starting) {
        if (starting.isEmpty()) {
            return gib(committed) + " of " + gib(budget) + " committed"
                    + (committed > budget ? " - already over budget" : "");
        }
        String head = "Starting " + String.join(", ", starting) + " adds " + gib(added)
                + ": " + gib(projected) + " of " + gib(budget);
        return over ? head + " - over budget by " + gib(projected - budget) : head;
    }

    static String gib(long bytes) {
        return String.format(java.util.Locale.ROOT, "%.1f GiB", bytes / (1024.0 * 1024 * 1024));
    }
}
