package dev.daweapon.type.generic.data.datapoints;

import net.minestom.server.entity.GameMode;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.type.generic.data.Datapoint;

// Datapoint wrapper for a Minestom GameMode value.
public class DatapointGamemode extends Datapoint<GameMode> {
    private static final JacksonSerializer<GameMode> serializer = new JacksonSerializer<>(GameMode.class);

    public DatapointGamemode(String key, GameMode value) {
        super(key, value, serializer);
    }

    public DatapointGamemode(String key) {
        super(key, null, serializer);
    }
}