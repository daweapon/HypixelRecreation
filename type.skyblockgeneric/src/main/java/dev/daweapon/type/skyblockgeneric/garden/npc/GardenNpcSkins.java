package dev.daweapon.type.skyblockgeneric.garden.npc;

import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class GardenNpcSkins {
    private static final Path FILE = Path.of("./configuration/skyblock/garden/visitor_skins.json");
    private static Map<String, Skin> skins;
    private static Map<String, Entity> entities;

    public record Skin(String texture, String signature) {
    }

    public record Entity(String type, @Nullable String profession) {
    }

    private GardenNpcSkins() {
    }

    public static synchronized Skin of(String visitorId) {
        if (skins == null) {
            skins = load(FILE);
        }
        Skin skin = skins.get(visitorId);
        return skin != null ? skin : new Skin(GardenNpcSkin.TEXTURE, GardenNpcSkin.SIGNATURE);
    }

    public static synchronized @Nullable Skin known(String visitorId) {
        if (skins == null) {
            skins = load(FILE);
        }
        return skins.get(visitorId);
    }

    public static @Nullable PlayerSkin playerSkin(String visitorId) {
        Skin skin = known(visitorId);
        return skin == null ? null : new PlayerSkin(skin.texture(), skin.signature());
    }

    public static synchronized @Nullable Entity entity(String visitorId) {
        if (entities == null) {
            entities = loadEntities(FILE);
        }
        return entities.get(visitorId);
    }

    static Map<String, Entity> loadEntities(Path path) {
        Map<String, Entity> loaded = new HashMap<>();
        try {
            JSONObject root = new JSONObject(Files.readString(path)).optJSONObject("entities");
            if (root != null) {
                for (String id : root.keySet()) {
                    JSONObject entity = root.getJSONObject(id);
                    loaded.put(id, new Entity(entity.getString("type"), entity.optString("profession", null)));
                }
            }
        } catch (IOException | RuntimeException exception) {
            Logger.warn(exception, "Could not read visitor entities from {}", path);
        }
        return loaded;
    }

    static Map<String, Skin> load(Path path) {
        Map<String, Skin> loaded = new HashMap<>();
        try {
            JSONObject root = new JSONObject(Files.readString(path)).getJSONObject("skins");
            for (String id : root.keySet()) {
                JSONObject skin = root.getJSONObject(id);
                loaded.put(id, new Skin(skin.getString("texture"), skin.getString("signature")));
            }
        } catch (IOException | RuntimeException exception) {
            Logger.warn(exception, "Could not read visitor skins from {}", path);
        }
        return loaded;
    }
}
