package dev.daweapon.type.catacombs.engine.look;

import com.google.gson.JsonObject;
import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.Nullable;

/** A captured skin or head texture: Mojang's Base64 texture value and, where Hypixel sent a valid one, its signature. */
public record Skin(String texture, @Nullable String signature) {
    public static Skin parse(JsonObject json) {
        return new Skin(json.get("texture").getAsString(), json.has("signature") ? json.get("signature").getAsString() : null);
    }

    public PlayerSkin playerSkin() {
        return new PlayerSkin(texture, signature);
    }
}
