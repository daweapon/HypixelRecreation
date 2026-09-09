package net.swofty.commons.protocol.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minestom.server.item.Material;
import java.lang.reflect.Type;

// Gson serializer converting a Minestom Material into its key string.
public class MaterialSerializer implements JsonSerializer<Material> {
    @Override
    public JsonElement serialize(Material src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.key().asString());
    }
}
