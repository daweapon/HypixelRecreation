package dev.daweapon.service.api.control;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Mongo-backed store of enrolled control devices. */
public final class DeviceDatabase {
    private static MongoCollection<Document> collection;

    private DeviceDatabase() {
    }

    public static void connect(String connectionString) {
        ConnectionString cs = new ConnectionString(connectionString);
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(cs).build();
        MongoClient client = MongoClients.create(settings);
        collection = client.getDatabase("Minestom").getCollection("api-control-devices");
    }

    public static void save(DeviceRecord record) {
        Document document = record.toDocument();
        if (collection.find(new Document("_id", record.tokenHash())).first() != null) {
            collection.replaceOne(new Document("_id", record.tokenHash()), document);
        } else {
            collection.insertOne(document);
        }
    }

    public static @Nullable DeviceRecord findByTokenHash(String tokenHash) {
        Document document = collection.find(new Document("_id", tokenHash)).first();
        return document == null ? null : DeviceRecord.fromDocument(document);
    }

    public static @Nullable DeviceRecord findByDeviceId(String deviceId) {
        Document document = collection.find(new Document("deviceId", deviceId)).first();
        return document == null ? null : DeviceRecord.fromDocument(document);
    }

    public static List<DeviceRecord> all() {
        List<DeviceRecord> records = new ArrayList<>();
        for (Document document : collection.find()) {
            records.add(DeviceRecord.fromDocument(document));
        }
        return records;
    }

    /** True when no device has ever been enrolled, used to bootstrap the first owner. */
    public static boolean isEmpty() {
        return collection.countDocuments() == 0;
    }

    public static void deleteByDeviceId(String deviceId) {
        collection.deleteMany(new Document("deviceId", deviceId));
    }
}
