package dev.daweapon.service.api.control.admin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.daweapon.PlayerField;
import dev.daweapon.commons.data.ProfileIndexes;
import dev.daweapon.commons.data.SharedData;
import dev.daweapon.commons.skyblock.CoopStorage;
import dev.daweapon.commons.skyblock.IslandStorage;
import dev.daweapon.commons.skyblock.SkyBlockProfileFields;
import dev.daweapon.service.api.control.admin.OrphanClassifier.CoopFacts;
import dev.daweapon.service.api.control.admin.OrphanClassifier.Inputs;
import dev.daweapon.service.api.control.admin.OrphanClassifier.Orphan;
import dev.daweapon.service.api.control.admin.OrphanClassifier.ProfileFacts;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Profiles, islands and co-ops all live in Redis now (the Mongo profile collection is gone), so the
// scan walks the hsb:* keyspace with SCAN rather than querying Mongo.
public final class OrphanScan {
    public record Result(Instant scannedAt, int profiles, int accounts, int islands, int coops, double totalCoins,
                         List<Orphan> orphans) {}

    static final String ACCOUNT_PREFIX = "hsb:acct:players:";
    static final String PROFILE_PREFIX = "hsb:prof:players:";
    private static final String COOP_LINK_PREFIX = "hsb:prof:linked/coop:";
    private static final String COOP_ISLAND_FIELD = "coop:" + SkyBlockProfileFields.ISLAND_UUID_KEY;
    private static final Duration CACHE = Duration.ofMinutes(2);
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private static volatile Result cached;

    private OrphanScan() {}

    public static synchronized Result scan(boolean refresh) {
        Result current = cached;
        if (!refresh && current != null && current.scannedAt().plus(CACHE).isAfter(Instant.now())) return current;

        Inputs inputs = collect();
        Result result = new Result(Instant.now(), inputs.profiles().size(), inputs.accounts().size(),
                inputs.islands().size(), inputs.coops().size(), OrphanClassifier.totalCoins(inputs),
                OrphanClassifier.classify(inputs));
        cached = result;
        return result;
    }

    public static synchronized String delete(String kind, String id) {
        Orphan target = scan(true).orphans().stream()
                .filter(orphan -> orphan.kind().equals(kind) && orphan.id().equals(id))
                .findFirst().orElse(null);
        if (target == null) throw new IllegalStateException("not an orphan on a fresh scan, nothing deleted");

        switch (kind) {
            case OrphanClassifier.ISLAND -> IslandStorage.delete(id);
            case OrphanClassifier.PROFILE -> SharedData.profile().deletePlayer(UUID.fromString(id));
            default -> throw new IllegalArgumentException("deleting " + kind + " orphans is not supported");
        }
        cached = null;
        return "deleted " + kind.toLowerCase() + " " + id + " (" + target.reason() + ")";
    }

    private static Inputs collect() {
        try (Jedis jedis = SharedData.jedisPool().getResource()) {
            Set<String> accounts = new HashSet<>();
            Map<String, String> ownerByIndexedProfile = new HashMap<>();
            for (String accountId : scanIds(jedis, ACCOUNT_PREFIX)) {
                accounts.add(accountId);
                Map<String, Object> account = readMap(jedis, ACCOUNT_PREFIX + accountId);
                for (PlayerField<String> index : ProfileIndexes.ALL) {
                    Object stored = account.get(index.fullKey());
                    if (stored == null) continue;
                    String value = String.valueOf(stored);
                    ProfileIndexes.decodeProfiles(value).forEach(profile -> ownerByIndexedProfile.put(profile.toString(), accountId));
                    UUID selected = ProfileIndexes.decodeSelected(value);
                    if (selected != null) ownerByIndexedProfile.put(selected.toString(), accountId);
                }
            }

            Map<String, ProfileFacts> profiles = new LinkedHashMap<>();
            for (String profileId : scanIds(jedis, PROFILE_PREFIX)) {
                profiles.put(profileId, profileFacts(profileId, readMap(jedis, PROFILE_PREFIX + profileId)));
            }

            Set<String> linkedIslandRefs = new HashSet<>();
            for (String coopKey : scanIds(jedis, COOP_LINK_PREFIX)) {
                String island = unquote(readMap(jedis, COOP_LINK_PREFIX + coopKey).get(COOP_ISLAND_FIELD));
                if (island != null) linkedIslandRefs.add(island);
            }

            Set<String> islands = new HashSet<>(scanIds(jedis, IslandStorage.ISLAND_PREFIX));

            List<CoopFacts> coops = new ArrayList<>();
            for (String coopId : scanIds(jedis, CoopStorage.COOP_PREFIX)) {
                if (coopId.equals("bymember") || coopId.equals("byprofile")) continue;
                String json = jedis.get(CoopStorage.COOP_PREFIX + coopId);
                if (json == null) continue;
                try {
                    coops.add(new CoopFacts(coopId, List.copyOf(CoopStorage.ids(Document.parse(json), CoopStorage.MEMBER_PROFILES))));
                } catch (Exception e) {
                    Logger.warn(e, "Unreadable co-op roster {}", coopId);
                }
            }

            return new Inputs(profiles, ownerByIndexedProfile, accounts, islands, linkedIslandRefs, coops);
        }
    }

    private static ProfileFacts profileFacts(String profileId, Map<String, Object> stored) {
        Object documentJson = stored.get(SkyBlockProfileFields.DOCUMENT.fullKey());
        if (documentJson == null) return new ProfileFacts(profileId, null, null, 0);
        try {
            Document document = Document.parse(String.valueOf(documentJson));
            return new ProfileFacts(profileId, document.getString("_owner"),
                    unquote(document.get(SkyBlockProfileFields.ISLAND_UUID_KEY)), number(document.get("coins")));
        } catch (Exception e) {
            Logger.warn(e, "Unreadable profile document {}", profileId);
            return new ProfileFacts(profileId, null, null, 0);
        }
    }

    private static List<String> scanIds(Jedis jedis, String prefix) {
        List<String> ids = new ArrayList<>();
        ScanParams params = new ScanParams().match(prefix + "*").count(500);
        String cursor = ScanParams.SCAN_POINTER_START;
        do {
            ScanResult<String> page = jedis.scan(cursor, params);
            for (String key : page.getResult()) {
                String id = key.substring(prefix.length());
                if (!id.contains(":")) ids.add(id);
            }
            cursor = page.getCursor();
        } while (!ScanParams.SCAN_POINTER_START.equals(cursor));
        return ids;
    }

    static Map<String, Object> readMap(Jedis jedis, String key) {
        String json = jedis.get(key);
        if (json == null || json.isBlank()) return Map.of();
        try {
            Map<String, Object> map = GSON.fromJson(json, MAP_TYPE);
            return map == null ? Map.of() : map;
        } catch (Exception e) {
            return Map.of();
        }
    }

    static @Nullable String unquote(@Nullable Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).replace("\"", "").trim();
        return text.isEmpty() || text.equals("null") ? null : text;
    }

    static double number(@Nullable Object value) {
        String text = unquote(value);
        if (text == null) return 0;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
