package net.swofty.service.auction;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class AuctionArchiveService {
    private static final int BATCH_SIZE = 100;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "auction-archive");
        thread.setDaemon(true);
        return thread;
    });

    public void start() {
        scheduler.scheduleWithFixedDelay(this::archiveSafely, 1, 5, TimeUnit.SECONDS);
    }

    int archiveExpired(long now) {
        int archived = 0;
        var expired = AuctionActiveDatabase.collection.find(Filters.lte("end", now)).limit(BATCH_SIZE);
        for (Document document : expired) {
            if (archive(document)) archived++;
        }
        return archived;
    }

    boolean archive(Document document) {
        String id = document.getString("_id");
        if (id == null || !(document.get("end") instanceof Number)) return false;

        List<Bson> updates = new ArrayList<>();
        for (var entry : document.entrySet()) {
            if (entry.getKey().equals("_id") || entry.getKey().equals("claims")
                    || entry.getKey().equals("bid_profiles") || entry.getKey().equals("seller_profile")) continue;
            updates.add(Updates.setOnInsert(entry.getKey(), entry.getValue()));
        }
        copyEntries(updates, "claims", document.get("claims", Document.class));
        copyEntries(updates, "bid_profiles", document.get("bid_profiles", Document.class));
        if (document.getString("seller_profile") != null) {
            updates.add(Updates.set("seller_profile", document.getString("seller_profile")));
        }
        if (updates.isEmpty()) return false;

        AuctionInactiveDatabase.collection.updateOne(Filters.eq("_id", id),
                Updates.combine(updates), new UpdateOptions().upsert(true));
        List<Bson> unchanged = new ArrayList<>();
        unchanged.add(Filters.eq("_id", id));
        unchanged.add(Filters.eq("end", document.get("end")));
        unchanged.add(Filters.eq("bids", document.get("bids")));
        matchOptional(unchanged, document, "claims");
        matchOptional(unchanged, document, "bid_profiles");
        matchOptional(unchanged, document, "seller_profile");
        return AuctionActiveDatabase.collection.deleteOne(Filters.and(unchanged)).getDeletedCount() == 1;
    }

    private static void copyEntries(List<Bson> updates, String field, Document values) {
        if (values == null) return;
        values.forEach((key, value) -> updates.add(Updates.set(field + "." + key, value)));
    }

    private static void matchOptional(List<Bson> filters, Document document, String field) {
        filters.add(document.containsKey(field) ? Filters.eq(field, document.get(field)) : Filters.exists(field, false));
    }

    private void archiveSafely() {
        try {
            while (archiveExpired(System.currentTimeMillis()) == BATCH_SIZE) {}
        } catch (Throwable failure) {
            System.err.println("Could not archive ended auctions: " + failure.getMessage());
            failure.printStackTrace();
        }
    }
}
