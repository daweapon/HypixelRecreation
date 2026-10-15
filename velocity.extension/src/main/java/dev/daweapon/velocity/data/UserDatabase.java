package dev.daweapon.velocity.data;

import dev.daweapon.PlayerField;
import dev.daweapon.codec.Codecs;
import dev.daweapon.commons.data.SharedData;
import org.bson.Document;

import java.util.UUID;

// Looks up a player's currently selected profile index via the shared SharedData account store.
public class UserDatabase {
    private static final PlayerField<String> PROFILES_INDEX =
            PlayerField.create("hypixel", "_profiles_index", Codecs.STRING, null);

    public UUID id;

    public UserDatabase(UUID id) {
        this.id = id;
    }

    public UserDatabase(String id) {
        this.id = UUID.fromString(id);
    }

    public static void connect(String connectionString) {
    }

    public Document getDocument() {
        String stored = read(id);
        if (stored == null || stored.isEmpty()) return null;
        String[] parts = stored.split(";", 2);
        if (parts[0].isEmpty()) return null;
        return new Document("_id", id.toString()).append("selected", parts[0]);
    }

    private static String read(UUID uuid) {
        try {
            return SharedData.account().get(uuid, PROFILES_INDEX);
        } finally {
            SharedData.account().unload(uuid);
        }
    }
}
