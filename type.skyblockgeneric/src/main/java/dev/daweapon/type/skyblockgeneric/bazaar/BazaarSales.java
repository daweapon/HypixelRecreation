package dev.daweapon.type.skyblockgeneric.bazaar;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// Bulk instant sells: everything in the inventory, or everything in the sacks, that the Bazaar has a buyer for.
public final class BazaarSales {
    public static final int LISTED_LINES = 10;
    private static final int INVENTORY_SLOTS = 36;

    private BazaarSales() {
    }

    public enum Source {
        INVENTORY, SACKS
    }

    public record Line(BazaarProduct product, int amount, double coins) {
        public Line(ItemType type, int amount, double coins) {
            this(BazaarProducts.of(type), amount, coins);
        }
    }

    public record Summary(List<Line> sellable, int unsellableProducts, double total) {
        public boolean isEmpty() {
            return sellable.isEmpty() && unsellableProducts == 0;
        }

        public boolean hasUnsellable() {
            return unsellableProducts > 0;
        }

        public List<Line> listed() {
            return sellable.subList(0, Math.min(LISTED_LINES, sellable.size()));
        }

        public int otherAmount() {
            return sellable.stream().skip(LISTED_LINES).mapToInt(Line::amount).sum();
        }

        public double otherCoins() {
            return sellable.stream().skip(LISTED_LINES).mapToDouble(Line::coins).sum();
        }

        public boolean hasOthers() {
            return sellable.size() > LISTED_LINES;
        }
    }

    public record Sold(int products, int items, double coins) {
    }

    public static Map<BazaarProduct, Integer> holdings(SkyBlockPlayer player, Source source) {
        Map<BazaarProduct, Integer> held = new LinkedHashMap<>();
        if (source == Source.SACKS) {
            player.getSackItems().getItems().forEach((type, amount) -> {
                if (amount != null && amount > 0 && isListed(player, BazaarProducts.of(type))) {
                    held.put(BazaarProducts.of(type), amount);
                }
            });
            return held;
        }
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            BazaarProduct product = BazaarProducts.fromStack(player.getInventory().getItemStack(slot));
            if (product != null && !held.containsKey(product) && isListed(player, product)) {
                held.put(product, product.held(player));
            }
        }
        return held;
    }

    private static boolean isListed(SkyBlockPlayer player, BazaarProduct product) {
        ItemType type = product.itemType();
        if (type == null) {
            return true;
        }
        return BazaarCategories.getFromItem(type) != null && !BazaarSettings.isIgnored(player.getUuid(), type);
    }

    public static CompletableFuture<Summary> summarize(SkyBlockPlayer player, Source source) {
        Map<BazaarProduct, Integer> held = holdings(player, source);
        List<CompletableFuture<Line>> lookups = new ArrayList<>();
        for (Map.Entry<BazaarProduct, Integer> entry : held.entrySet()) {
            lookups.add(player.getBazaarConnector().getItemStatistics(entry.getKey())
                    .thenApply(stats -> new Line(entry.getKey(), entry.getValue(), stats.bestBid() * entry.getValue()))
                    .exceptionally(throwable -> new Line(entry.getKey(), entry.getValue(), 0)));
        }
        return CompletableFuture.allOf(lookups.toArray(new CompletableFuture[0])).thenApply(ignored -> {
            List<Line> sellable = new ArrayList<>();
            int unsellable = 0;
            for (CompletableFuture<Line> lookup : lookups) {
                Line line = lookup.join();
                if (line.coins() > 0) {
                    sellable.add(line);
                } else {
                    unsellable++;
                }
            }
            sellable.sort(Comparator.comparingDouble(Line::coins).reversed());
            return new Summary(List.copyOf(sellable), unsellable, sellable.stream().mapToDouble(Line::coins).sum());
        });
    }

    public static CompletableFuture<Sold> sell(SkyBlockPlayer player, Source source) {
        return summarize(player, source).thenCompose(summary -> {
            CompletableFuture<Sold> chain = CompletableFuture.completedFuture(new Sold(0, 0, 0));
            for (Line line : summary.sellable()) {
                chain = chain.thenCompose(sold -> sellLine(player, source, line).thenApply(done -> done
                        ? new Sold(sold.products() + 1, sold.items() + line.amount(), sold.coins() + line.coins())
                        : sold));
            }
            return chain.thenApply(sold -> {
                player.getBazaarConnector().processAllPendingTransactions();
                return sold;
            });
        });
    }

    private static void refund(SkyBlockPlayer player, Source source, Line line, Runnable undo) {
        if (source == Source.SACKS) {
            player.getSackItems().increase(line.product().itemType(), line.amount());
        } else {
            undo.run();
        }
    }

    private static CompletableFuture<Boolean> sellLine(SkyBlockPlayer player, Source source, Line line) {
        int amount = line.amount();
        BazaarProduct product = line.product();
        Runnable undo = () -> {
        };
        if (source == Source.SACKS) {
            if (player.getSackItems().getAmount(product.itemType()) < amount) {
                return CompletableFuture.completedFuture(false);
            }
            player.getSackItems().decrease(product.itemType(), amount);
        } else {
            Runnable taken = product.take(player, amount);
            if (taken == null) {
                return CompletableFuture.completedFuture(false);
            }
            undo = taken;
        }
        Runnable restore = undo;
        double each = line.coins() / amount;
        return player.getBazaarConnector().createSellOrder(product, each, amount).thenApply(result -> {
            if (!result.success()) {
                refund(player, source, line, restore);
                return false;
            }
            BazaarHistory.record(player, BazaarHistory.Kind.INSTANT_SELL,
                    product, amount, line.coins(), player.getFullDisplayName());
            return true;
        });
    }
}
