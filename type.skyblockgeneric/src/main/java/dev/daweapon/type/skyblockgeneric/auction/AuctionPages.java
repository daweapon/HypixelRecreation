package dev.daweapon.type.skyblockgeneric.auction;

import java.util.List;

public final class AuctionPages<T> {
    public record Request(long generation, long sequence) {}
    public record Page<T>(int number, int count, int total, List<T> items) {
        public boolean hasPrevious() { return number > 1; }
        public boolean hasNext() { return number < count; }
    }

    private final int size;
    private List<T> results = List.of();
    private int page = 1;
    private long generation, sequence, applied;

    public AuctionPages(int size) {
        if (size < 1) throw new IllegalArgumentException("Page size must be positive");
        this.size = size;
    }

    public synchronized Request request() { return new Request(generation, ++sequence); }

    public synchronized boolean accept(Request request, List<T> results) {
        if (request.generation() != generation || request.sequence() <= applied) return false;
        this.results = List.copyOf(results);
        applied = request.sequence();
        page = Math.min(page, pageCount());
        return true;
    }

    public synchronized void reset() {
        invalidate();
        results = List.of();
        page = 1;
    }

    public synchronized void invalidate() { generation++; }

    public synchronized void move(int delta) {
        page = (int) Math.clamp((long) page + delta, 1, pageCount());
    }

    public synchronized Page<T> current() {
        int start = (page - 1) * size;
        return new Page<>(page, pageCount(), results.size(), results.subList(start, (int) Math.min((long) start + size, results.size())));
    }

    private int pageCount() { return results.isEmpty() ? 1 : (results.size() - 1) / size + 1; }
}
