package net.swofty.service.api.control;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Append-only store of control actions.
 *
 * Deliberately exposes no update or delete operation: entries can be written and read, and
 * nothing in this service can alter one after the fact.
 *
 * Honest limitation: this is append-only *by construction in the application*, not
 * cryptographically tamper-evident. Anyone with direct Mongo access on the NAS can still edit
 * the collection, and since Docker socket access already implies host root, that is not a
 * boundary this can enforce. It answers "who did what" for accountability, not for an
 * adversary who already owns the box.
 */
public final class AuditDatabase {
    private static MongoCollection<Document> collection;

    private AuditDatabase() {
    }

    public static void connect(String connectionString) {
        ConnectionString cs = new ConnectionString(connectionString);
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(cs).build();
        MongoClient client = MongoClients.create(settings);
        collection = client.getDatabase("Minestom").getCollection("api-control-audit");
    }

    /**
     * Records an entry. Never throws: an audit write failing must not also fail the action the
     * operator asked for, but it must be loudly visible in the service log.
     */
    public static void record(AuditEntry entry) {
        try {
            if (collection == null) {
                Logger.error("Audit store not connected - dropping entry: {}", entry);
                return;
            }
            collection.insertOne(entry.toDocument());
        } catch (Exception e) {
            Logger.error(e, "Failed to write audit entry: {}", entry);
        }
    }

    /** Most recent entries first, optionally filtered by action or actor. */
    public static List<AuditEntry> recent(int limit, String action, String actorName) {
        List<AuditEntry> entries = new ArrayList<>();
        if (collection == null) {
            return entries;
        }

        List<Bson> filters = new ArrayList<>();
        if (action != null && !action.isBlank()) {
            filters.add(Filters.eq("action", action));
        }
        if (actorName != null && !actorName.isBlank()) {
            filters.add(Filters.eq("actorName", actorName));
        }
        Bson filter = filters.isEmpty() ? new Document() : Filters.and(filters);

        for (Document document : collection.find(filter)
                .sort(Sorts.descending("at"))
                .limit(Math.min(Math.max(limit, 1), 500))) {
            entries.add(AuditEntry.fromDocument(document));
        }
        return entries;
    }

    public static long count() {
        return collection == null ? 0 : collection.countDocuments();
    }
}
