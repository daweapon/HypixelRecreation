package net.swofty.type.generic.data;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import net.swofty.commons.data.SwoftyData;
import net.swofty.commons.skyblock.CoopStorage;
import net.swofty.commons.skyblock.IslandStorage;
import net.swofty.type.generic.data.mongodb.MongoConnection;
import org.bson.Document;
import org.tinylog.Logger;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class WipeSnapshotService {
    private static final String COLLECTION = "wipe_snapshots";
    private static final String PARTS_COLLECTION = "wipe_snapshot_parts";
    private static final int FORMAT = 2;
    private static final String ACCOUNT = "account";
    private static final String ACCOUNT_VERSION = "accountVersion";
    private static final String PROFILE = "profile";
    private static final String PROFILE_VERSION = "profileVersion";
    private static final String ISLAND = "island";
    private static final String OWNED = "owned";
    private static final String ACCOUNT_KEY_PREFIX = "hsb:acct:players:";
    private static final String ACCOUNT_VERSION_PREFIX = "hsb:acct:version:players:";
    private static final String PROFILE_KEY_PREFIX = "hsb:prof:players:";
    private static final String PROFILE_VERSION_PREFIX = "hsb:prof:version:players:";
    public static final long RESTORE_WINDOW_MILLIS = TimeUnit.HOURS.toMillis(1);

    private WipeSnapshotService() {}

    public enum RestoreResult {
        SUCCESS,
        EXPIRED,
        NOT_FOUND
    }

    // Captures the raw Redis documents for a player's account/profiles/islands before a wipe deletes them,
    // rather than individual fields - the account and profile data model each live as one JSON blob per key,
    // so a whole-key copy is the only way to guarantee every field (not just a hand-picked subset) comes back.
    // Each blob is stored as its own chunked part document, because a staff account with several maxed profiles
    // and islands can exceed Mongo's 16 MB limit for a single document.
    public static void capture(UUID playerUuid, List<UUID> profileIds) {
        MongoCollection<Document> collection = MongoConnection.collection(COLLECTION);
        MongoCollection<Document> partCollection = MongoConnection.collection(PARTS_COLLECTION);
        if (collection == null || partCollection == null) return;

        WipeSnapshotParts parts = new WipeSnapshotParts(partCollection, playerUuid);
        try {
            collection.deleteOne(Filters.eq("_id", playerUuid.toString()));
            parts.clear();

            parts.putString(ACCOUNT, "", rawGet(ACCOUNT_KEY_PREFIX + playerUuid));
            parts.putString(ACCOUNT_VERSION, "", rawGet(ACCOUNT_VERSION_PREFIX + playerUuid));

            Set<String> islands = new HashSet<>();
            for (UUID profileId : profileIds) {
                parts.putString(PROFILE, profileId.toString(), rawGet(PROFILE_KEY_PREFIX + profileId));
                parts.putString(PROFILE_VERSION, profileId.toString(), rawGet(PROFILE_VERSION_PREFIX + profileId));

                UUID sharedCoopId = CoopStorage.coopIdOfProfile(profileId);
                UUID coopId = sharedCoopId == null ? profileId : sharedCoopId;
                for (UUID islandId : PlayerWipeService.islandIdsOf(profileId, coopId)) {
                    if (islands.add(islandId.toString())) parts.putString(ISLAND, islandId.toString(), readIslandRaw(islandId));
                }
            }

            List<String> profileIdStrings = profileIds.stream().map(UUID::toString).toList();
            for (PlayerWipeService.OwnedCollection ownedCollection : PlayerWipeService.OWNED_COLLECTIONS) {
                MongoCollection<Document> source = MongoConnection.collection(ownedCollection.name());
                if (source == null) continue;
                try {
                    List<Document> rows = new ArrayList<>();
                    source.find(PlayerWipeService.ownedFilter(ownedCollection, playerUuid, profileIdStrings)).into(rows);
                    parts.putRows(OWNED, ownedCollection.name(), rows);
                } catch (Exception e) {
                    Logger.error(e, "Failed to snapshot the rows of user {} in {}", playerUuid, ownedCollection.name());
                }
            }

            collection.replaceOne(Filters.eq("_id", playerUuid.toString()),
                    new Document("_id", playerUuid.toString())
                            .append("timestamp", System.currentTimeMillis())
                            .append("format", FORMAT),
                    new ReplaceOptions().upsert(true));
        } catch (Exception e) {
            Logger.error(e, "Failed to store the wipe snapshot of user {}", playerUuid);
        }
    }

    public static RestoreResult restore(UUID playerUuid) {
        MongoCollection<Document> collection = MongoConnection.collection(COLLECTION);
        if (collection == null) return RestoreResult.NOT_FOUND;

        Document snapshot = collection.find(Filters.eq("_id", playerUuid.toString())).first();
        if (snapshot == null) return RestoreResult.NOT_FOUND;

        MongoCollection<Document> partCollection = MongoConnection.collection(PARTS_COLLECTION);
        WipeSnapshotParts parts = partCollection == null ? null : new WipeSnapshotParts(partCollection, playerUuid);

        Long timestamp = snapshot.getLong("timestamp");
        if (timestamp == null || System.currentTimeMillis() - timestamp > RESTORE_WINDOW_MILLIS) {
            discard(collection, parts, playerUuid);
            return RestoreResult.EXPIRED;
        }

        if (snapshot.getInteger("format", 1) >= FORMAT && parts != null) {
            restoreParts(playerUuid, parts);
        } else {
            restoreInline(playerUuid, snapshot);
        }

        discard(collection, parts, playerUuid);
        return RestoreResult.SUCCESS;
    }

    private static void restoreParts(UUID playerUuid, WipeSnapshotParts parts) {
        parts.strings().forEach((key, value) -> {
            switch (key.kind()) {
                case ACCOUNT -> rawSet(ACCOUNT_KEY_PREFIX + playerUuid, value);
                case ACCOUNT_VERSION -> rawSet(ACCOUNT_VERSION_PREFIX + playerUuid, value);
                case PROFILE -> rawSet(PROFILE_KEY_PREFIX + key.key(), value);
                case PROFILE_VERSION -> rawSet(PROFILE_VERSION_PREFIX + key.key(), value);
                case ISLAND -> writeIslandRaw(key.key(), value);
                default -> Logger.warn("Unknown wipe snapshot part {} for user {}", key.kind(), playerUuid);
            }
        });
        parts.rows().forEach((key, rows) -> restoreRows(playerUuid, key.key(), rows));
    }

    private static void restoreInline(UUID playerUuid, Document snapshot) {
        rawSet(ACCOUNT_KEY_PREFIX + playerUuid, snapshot.getString("account"));
        rawSet(ACCOUNT_VERSION_PREFIX + playerUuid, snapshot.getString("accountVersion"));

        Document profiles = snapshot.get("profiles", Document.class);
        if (profiles != null) {
            for (String profileIdString : profiles.keySet()) {
                rawSet(PROFILE_KEY_PREFIX + profileIdString, profiles.getString(profileIdString));
            }
        }

        Document profileVersions = snapshot.get("profileVersions", Document.class);
        if (profileVersions != null) {
            for (String profileIdString : profileVersions.keySet()) {
                rawSet(PROFILE_VERSION_PREFIX + profileIdString, profileVersions.getString(profileIdString));
            }
        }

        Document islands = snapshot.get("islands", Document.class);
        if (islands != null) {
            for (String islandIdString : islands.keySet()) {
                writeIslandRaw(islandIdString, islands.getString(islandIdString));
            }
        }

        Document owned = snapshot.get("owned", Document.class);
        if (owned != null) {
            for (PlayerWipeService.OwnedCollection ownedCollection : PlayerWipeService.OWNED_COLLECTIONS) {
                List<Document> rows = owned.getList(ownedCollection.name(), Document.class);
                if (rows != null) restoreRows(playerUuid, ownedCollection.name(), rows);
            }
        }
    }

    private static void restoreRows(UUID playerUuid, String collectionName, List<Document> rows) {
        if (rows.isEmpty()) return;
        MongoCollection<Document> target = MongoConnection.collection(collectionName);
        if (target == null) return;
        try {
            target.insertMany(rows);
        } catch (Exception e) {
            Logger.error(e, "Failed to restore the rows of user {} into {}", playerUuid, collectionName);
        }
    }

    private static void discard(MongoCollection<Document> collection, WipeSnapshotParts parts, UUID playerUuid) {
        collection.deleteOne(Filters.eq("_id", playerUuid.toString()));
        if (parts != null) parts.clear();
    }

    private static String rawGet(String key) {
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            Logger.error(e, "Failed to read the raw key {} for a wipe snapshot", key);
            return null;
        }
    }

    private static void rawSet(String key, String value) {
        if (value == null) return;
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            jedis.set(key, value);
        } catch (Exception e) {
            Logger.error(e, "Failed to restore the raw key {} from a wipe snapshot", key);
        }
    }

    private static String readIslandRaw(UUID islandId) {
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            byte[] raw = jedis.get(IslandStorage.key(islandId.toString()));
            return raw == null ? null : Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            Logger.error(e, "Failed to read the island {} for a wipe snapshot", islandId);
            return null;
        }
    }

    private static void writeIslandRaw(String islandId, String base64) {
        if (base64 == null) return;
        try (Jedis jedis = SwoftyData.jedisPool().getResource()) {
            jedis.set(IslandStorage.key(islandId), Base64.getDecoder().decode(base64));
        } catch (Exception e) {
            Logger.error(e, "Failed to restore the island {} from a wipe snapshot", islandId);
        }
    }
}
