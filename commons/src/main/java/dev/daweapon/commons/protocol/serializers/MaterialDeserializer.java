package dev.daweapon.commons.protocol.serializers;

import com.google.gson.*;
import net.minestom.server.item.Material;

import java.lang.reflect.Type;

// Gson deserializer converting a material key string back into a Minestom Material.
public class MaterialDeserializer implements JsonDeserializer<Material> {
    @Override
    public Material deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Material.fromKey(jsonElement.getAsString());
    }
}