package net.swofty.type.skyblockgeneric.bazaar;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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

    public record Line(ItemType type, int amount, double coins) {
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

    public static Map<ItemType, Integer> holdings(SkyBlockPlayer player, Source source) {
        Map<ItemType, Integer> held = new LinkedHashMap<>();
        if (source == Source.SACKS) {
            player.getSackItems().getItems().forEach((type, amount) -> {
                if (amount != null && amount > 0 && isListed(player, type)) {
                    held.put(type, amount);
                }
            });
            return held;
        }
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir()) {
                continue;
            }
            ItemType type = new SkyBlockItem(stack).getAttributeHandler().getPotentialType();
            if (type != null && !held.containsKey(type) && isListed(player, type)) {
                held.put(type, player.getAmountInInventory(type));
            }
        }
        return held;
    }

    private static boolean isListed(SkyBlockPlayer player, ItemType type) {
        return BazaarCategories.getFromItem(type) != null && !BazaarSettings.isIgnored(player.getUuid(), type);
    }

    public static CompletableFuture<Summary> summarize(SkyBlockPlayer player, Source source) {
        Map<ItemType, Integer> held = holdings(player, source);
        List<CompletableFuture<Line>> lookups = new ArrayList<>();
        for (Map.Entry<ItemType, Integer> entry : held.entrySet()) {
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

    private static void refund(SkyBlockPlayer player, Source source, Line line) {
        if (source == Source.SACKS) {
            player.getSackItems().increase(line.type(), line.amount());
        } else {
            player.addAndUpdateItem(line.type(), line.amount());
        }
    }

    private static CompletableFuture<Boolean> sellLine(SkyBlockPlayer player, Source source, Line line) {
        int amount = line.amount();
        if (source == Source.SACKS) {
            if (player.getSackItems().getAmount(line.type()) < amount) {
                return CompletableFuture.completedFuture(false);
            }
            player.getSackItems().decrease(line.type(), amount);
        } else if (player.takeItem(line.type(), amount) == null) {
            return CompletableFuture.completedFuture(false);
        }
        double each = line.coins() / amount;
        return player.getBazaarConnector().createSellOrder(line.type(), each, amount).thenApply(result -> {
            if (!result.success()) {
                refund(player, source, line);
                return false;
            }
            BazaarHistory.record(player.getProfiles().getCurrentlySelected(), BazaarHistory.Kind.INSTANT_SELL,
                    line.type(), amount, line.coins(), player.getFullDisplayName());
            return true;
        });
    }
}
