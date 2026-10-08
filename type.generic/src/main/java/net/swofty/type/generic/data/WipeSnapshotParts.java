package net.swofty.type.generic.data;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class WipeSnapshotParts {
    static final int CHUNK_CHARS = 2_000_000;

    private final MongoCollection<Document> collection;
    private final String owner;

    WipeSnapshotParts(MongoCollection<Document> collection, UUID owner) {
        this.collection = collection;
        this.owner = owner.toString();
    }

    record Key(String kind, String key) {}

    static List<String> chunk(String value) {
        List<String> chunks = new ArrayList<>();
        for (int start = 0; start < value.length(); start += CHUNK_CHARS) {
            chunks.add(value.substring(start, Math.min(value.length(), start + CHUNK_CHARS)));
        }
        if (chunks.isEmpty()) chunks.add("");
        return chunks;
    }

    void clear() {
        collection.deleteMany(Filters.eq("owner", owner));
    }

    void putString(String kind, String key, String value) {
        if (value == null) return;
        List<String> chunks = chunk(value);
        for (int index = 0; index < chunks.size(); index++) {
            collection.insertOne(new Document("owner", owner)
                    .append("kind", kind)
                    .append("key", key)
                    .append("index", index)
                    .append("data", chunks.get(index)));
        }
    }

    void putRows(String kind, String key, List<Document> rows) {
        for (int index = 0; index < rows.size(); index++) {
            collection.insertOne(new Document("owner", owner)
                    .append("kind", kind)
                    .append("key", key)
                    .append("index", index)
                    .append("row", rows.get(index)));
        }
    }

    Map<Key, String> strings() {
        Map<Key, StringBuilder> joined = new LinkedHashMap<>();
        for (Document part : parts()) {
            String data = part.getString("data");
            if (data == null) continue;
            joined.computeIfAbsent(keyOf(part), _ -> new StringBuilder()).append(data);
        }
        Map<Key, String> strings = new LinkedHashMap<>();
        joined.forEach((key, builder) -> strings.put(key, builder.toString()));
        return strings;
    }

    Map<Key, List<Document>> rows() {
        Map<Key, List<Document>> rows = new LinkedHashMap<>();
        for (Document part : parts()) {
            Document row = part.get("row", Document.class);
            if (row != null) rows.computeIfAbsent(keyOf(part), _ -> new ArrayList<>()).add(row);
        }
        return rows;
    }

    private List<Document> parts() {
        List<Document> parts = new ArrayList<>();
        collection.find(Filters.eq("owner", owner))
                .sort(Sorts.ascending("kind", "key", "index"))
                .into(parts);
        return parts;
    }

    private static Key keyOf(Document part) {
        return new Key(part.getString("kind"), part.getString("key"));
    }
}
