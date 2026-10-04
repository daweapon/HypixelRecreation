package net.swofty.service.control.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/** Shared Gson instance; Instants and Durations are serialised in forms a client can use directly. */
public final class Json {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new TypeAdapter<Instant>() {
                @Override
                public void write(JsonWriter out, Instant value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        out.value(value.toString());
                    }
                }

                @Override
                public Instant read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                        return null;
                    }
                    return Instant.parse(in.nextString());
                }
            })
            .registerTypeAdapter(Duration.class, new TypeAdapter<Duration>() {
                @Override
                public void write(JsonWriter out, Duration value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        out.value(value.toSeconds());
                    }
                }

                @Override
                public Duration read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                        return null;
                    }
                    return Duration.ofSeconds(in.nextLong());
                }
            })
            .serializeNulls()
            .create();

    private Json() {
    }

    public static Gson gson() {
        return GSON;
    }

    public static String write(Object value) {
        return GSON.toJson(value);
    }

    public static <T> T read(String json, Class<T> type) {
        return GSON.fromJson(json, type);
    }
}
