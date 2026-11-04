package dev.daweapon.service.api.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.commons.protocol.objects.proxy.to.RequestServersProtocol;
import dev.daweapon.commons.redis.RedisClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Asks the proxy, over Redis, which game servers it currently has registered.
 *
 * service.api is the one control-plane process with Redis; the Docker agent deliberately has
 * none, so this list is fetched here and merged with the agent's container list.
 */
public final class ProxyRegistrationProbe {
    private static final RequestServersProtocol PROTOCOL = new RequestServersProtocol();
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private ProxyRegistrationProbe() {
    }

    public static List<ProxyRegistrations.Registration> fetch() throws Exception {
        RequestServersProtocol.Response response = RedisClient.requestProxy(PROTOCOL,
                        new RequestServersProtocol.Request("ALL", null, null))
                .orTimeout(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
                .get();
        if (!response.success()) {
            throw new IllegalStateException(response.error() == null ? "proxy refused the query" : response.error());
        }
        return parse(response.serversList());
    }

    public static List<ProxyRegistrations.Registration> parse(String serversList) {
        List<ProxyRegistrations.Registration> registrations = new ArrayList<>();
        JsonObject root = JsonParser.parseString(serversList).getAsJsonObject();
        JsonArray servers = root.has("servers") ? root.getAsJsonArray("servers") : new JsonArray();
        for (JsonElement element : servers) {
            JsonObject server = element.getAsJsonObject();
            registrations.add(new ProxyRegistrations.Registration(
                    string(server, "name"),
                    string(server, "uuid"),
                    string(server, "type"),
                    server.has("port") ? server.get("port").getAsInt() : 0,
                    server.has("players") && server.get("players").isJsonArray()
                            ? server.getAsJsonArray("players").size() : 0,
                    server.has("host") && !server.get("host").isJsonNull() ? server.get("host").getAsString() : null));
        }
        return registrations;
    }

    private static String string(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : "";
    }
}
