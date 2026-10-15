package dev.daweapon.type.generic.data.mongodb;

import com.mongodb.client.MongoClient;
import dev.daweapon.PlayerField;
import dev.daweapon.commons.data.ProfileIndexes;
import dev.daweapon.commons.data.SharedData;
import dev.daweapon.commons.skyblock.SkyBlockPlayerProfiles;

import java.util.UUID;
import java.util.stream.Collectors;

public class UserDatabase {
    private static final PlayerField<String> PROFILES_INDEX = ProfileIndexes.SKYBLOCK;

    public UUID id;

    public UserDatabase(UUID id) {
        this.id = id;
    }

    public UserDatabase(String id) {
        this.id = UUID.fromString(id);
    }

    public static void connect(MongoClient client) {
    }

    public SkyBlockPlayerProfiles getProfiles() {
        String stored = SharedData.account().get(id, PROFILES_INDEX);
        if (stored == null || stored.isEmpty()) {
            return new SkyBlockPlayerProfiles(id);
        }
        SkyBlockPlayerProfiles profiles = new SkyBlockPlayerProfiles(id);
        String[] parts = stored.split(";", 2);
        if (!parts[0].isEmpty()) profiles.setCurrentlySelected(UUID.fromString(parts[0]));
        if (parts.length > 1 && !parts[1].isEmpty()) {
            for (String profile : parts[1].split(",")) profiles.addProfile(UUID.fromString(profile));
        }
        return profiles;
    }

    public void deleteSelf() {
        SharedData.account().set(id, PROFILES_INDEX, null);
    }

    public void saveProfiles(SkyBlockPlayerProfiles profiles) {
        String selected = profiles.getCurrentlySelected() == null ? "" : profiles.getCurrentlySelected().toString();
        String list = profiles.getProfiles().stream().map(UUID::toString).collect(Collectors.joining(","));
        SharedData.account().set(id, PROFILES_INDEX, selected + ";" + list);
    }
}
