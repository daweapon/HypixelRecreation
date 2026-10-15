package dev.daweapon.service.auction;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mongodb.client.model.Filters;
import dev.daweapon.commons.skyblock.auctions.AuctionsFilter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Short-lived in-memory cache of auction listings per category/filter, without mutating listing storage during reads.
public class AuctionsCacheService {
    private final Cache<String, Object> cache;

    public AuctionsCacheService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.SECONDS)
                .maximumSize(100)
                .build();
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    public List<Document> getAuctions(String categoryFilter, AuctionsFilter filter) {
        // Check the cache first
        List<Document> result = (List<Document>) cache.getIfPresent(categoryFilter + "-" + filter);
        if (result != null) {
            return unexpired(result);
        }

        // If not in cache, fetch from MongoDB and store in cache
        List<Document> documents = getDocuments(categoryFilter);

        // Do not move listings during reads: cached snapshots can race with accepted bids.
        result = switch (filter) {
            case SHOW_ALL -> documents;
            case AUCTIONS_ONLY -> documents.stream().filter(document -> document.getBoolean("bin").equals(false)).toList();
            case BIN_ONLY -> documents.stream().filter(document -> document.getBoolean("bin").equals(true)).toList();
        };

        cache.put(categoryFilter + "-" + filter, result);
        return unexpired(result);
    }

    private static List<Document> unexpired(List<Document> documents) {
        long now = System.currentTimeMillis();
        return documents.stream().filter(document -> document.get("end") instanceof Number end
                && end.longValue() > now).toList();
    }

    public List<Document> getDocuments(String category) {
        Object result = cache.getIfPresent(category);
        if (result != null) {
            return (List<Document>) result;
        }

        long now = System.currentTimeMillis();
        ArrayList<Document> documents = new ArrayList<>(
                AuctionActiveDatabase.collection.find(Filters.eq("category", category.toUpperCase())).into(new ArrayList<>())
                        .stream().filter(document -> document.getLong("end") > now).toList());
        cache.put(category, documents);

        return documents;
    }
}
