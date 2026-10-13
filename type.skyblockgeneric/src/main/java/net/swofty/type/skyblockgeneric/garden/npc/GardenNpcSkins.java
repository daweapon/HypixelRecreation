package net.swofty.type.skyblockgeneric.garden.npc;

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

    public record Skin(String texture, String signature) {
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
