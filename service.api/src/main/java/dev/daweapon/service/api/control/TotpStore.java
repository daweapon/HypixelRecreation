package dev.daweapon.service.api.control;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import dev.daweapon.commons.config.ConfigProvider;
import org.bson.Document;

import java.time.Instant;

/**
 * The install's single TOTP enrolment, in Mongo {@code api-control-totp}.
 *
 * One secret guards every OWNER action, whichever owner device makes the request: the second
 * factor is "something the owner holds" (an authenticator app), not something per device. The
 * secret must be stored in a form the server can use, which is inherent to TOTP; anyone with
 * Mongo access on the NAS can read it, the same boundary the audit log documents.
 *
 * Connects lazily so APIService needs no change to load it. Break-glass if the authenticator is
 * lost: delete the document {@code {_id: "install"}} from the collection with a shell on the NAS.
 */
public final class TotpStore {
    private static final String ID = "install";
    private static MongoCollection<Document> collection;

    public record State(String secret, boolean active, String enrolledBy, Instant enrolledAt,
                        String pendingSecret, String pendingBy, Instant pendingAt) {
        public static final State NONE = new State(null, false, null, null, null, null, null);
    }

    private TotpStore() {
    }

    private static synchronized MongoCollection<Document> collection() {
        if (collection == null) {
            ConnectionString cs = new ConnectionString(ConfigProvider.settings().getMongodb());
            collection = MongoClients.create(MongoClientSettings.builder().applyConnectionString(cs).build())
                    .getDatabase("Minestom").getCollection("api-control-totp");
        }
        return collection;
    }

    public static State load() {
        Document document = collection().find(new Document("_id", ID)).first();
        if (document == null) {
            return State.NONE;
        }
        return new State(
                document.getString("secret"),
                document.getBoolean("active", false),
                document.getString("enrolledBy"),
                parse(document.getString("enrolledAt")),
                document.getString("pendingSecret"),
                document.getString("pendingBy"),
                parse(document.getString("pendingAt")));
    }

    public static void save(State state) {
        Document document = new Document("_id", ID)
                .append("secret", state.secret())
                .append("active", state.active())
                .append("enrolledBy", state.enrolledBy())
                .append("enrolledAt", state.enrolledAt() == null ? null : state.enrolledAt().toString())
                .append("pendingSecret", state.pendingSecret())
                .append("pendingBy", state.pendingBy())
                .append("pendingAt", state.pendingAt() == null ? null : state.pendingAt().toString());
        collection().replaceOne(new Document("_id", ID), document, new ReplaceOptions().upsert(true));
    }

    public static void clear() {
        collection().deleteOne(new Document("_id", ID));
    }

    private static Instant parse(String value) {
        return value == null ? null : Instant.parse(value);
    }
}
