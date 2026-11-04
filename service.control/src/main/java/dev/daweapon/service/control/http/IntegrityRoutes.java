package dev.daweapon.service.control.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.daweapon.service.control.deploy.DeployService;
import dev.daweapon.service.control.deploy.DeployTargets;
import dev.daweapon.service.control.docker.DockerApi;
import dev.daweapon.service.control.docker.DockerException;
import dev.daweapon.service.control.fleet.ContainerInfo;
import dev.daweapon.service.control.fleet.FleetGroup;
import dev.daweapon.service.control.fleet.FleetService;
import dev.daweapon.service.control.fleet.OperationJob;
import dev.daweapon.service.control.integrity.BootVerifier;
import dev.daweapon.service.control.integrity.CapacityService;
import dev.daweapon.service.control.integrity.ConfigDriftService;
import dev.daweapon.service.control.integrity.ContainerFacts;
import dev.daweapon.service.control.integrity.JarFingerprints;
import dev.daweapon.service.control.integrity.JarSkew;
import dev.daweapon.service.control.integrity.PackHealthService;
import dev.daweapon.service.control.integrity.RegistrationReport;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fleet-integrity routes: version skew, config drift, capacity, pack health and the proxy
 * registration hand-off from service.api. Authentication is the shared before-filter that
 * {@link ControlRoutes} installs for every path.
 */
public final class IntegrityRoutes {
    private final DockerApi docker;
    private final FleetService fleetService;
    private final DeployService deployService;
    private final JarFingerprints fingerprints;
    private final ConfigDriftService configDrift;
    private final CapacityService capacity;
    private final PackHealthService packHealth;
    private final RegistrationReport registrations;
    private final BootVerifier bootVerifier;

    public IntegrityRoutes(DockerApi docker, FleetService fleetService, DeployService deployService,
                           JarFingerprints fingerprints, ConfigDriftService configDrift,
                           CapacityService capacity, PackHealthService packHealth,
                           RegistrationReport registrations, BootVerifier bootVerifier) {
        this.docker = docker;
        this.fleetService = fleetService;
        this.deployService = deployService;
        this.fingerprints = fingerprints;
        this.configDrift = configDrift;
        this.capacity = capacity;
        this.packHealth = packHealth;
        this.registrations = registrations;
        this.bootVerifier = bootVerifier;
    }

    public void register() {
        Spark.before("/control/containers/restart", (req, res) -> {
            if (readNames(parseObject(req.body())).contains(DeployTargets.SELF_CONTAINER)) {
                bootVerifier.markPending("restart", actor(req));
            }
        });

        Spark.get("/control/integrity/skew", (req, res) -> Json.write(fingerprints.view(refresh(req))));
        Spark.post("/control/integrity/reconcile", this::reconcile);

        Spark.get("/control/integrity/config-drift", (req, res) -> Json.write(configDrift.view(refresh(req))));
        Spark.post("/control/integrity/config-sync", this::configSync);

        Spark.get("/control/integrity/capacity", (req, res) -> {
            String raw = req.queryParams("candidates");
            Set<String> candidates = raw == null ? Set.of() : Arrays.stream(raw.split(","))
                    .map(String::trim).filter(name -> !name.isEmpty()).collect(Collectors.toSet());
            return Json.write(capacity.evaluate(candidates));
        });

        Spark.get("/control/integrity/pack", (req, res) -> Json.write(packHealth.report(
                refresh(req), "true".equalsIgnoreCase(req.queryParams("verify")))));

        Spark.get("/control/integrity/servers", this::servers);
        Spark.post("/control/integrity/registrations", (req, res) -> {
            JsonObject body = parseObject(req.body());
            boolean reachable = body.has("proxyReachable") && body.get("proxyReachable").getAsBoolean();
            List<String> delisted = readArray(body, "delisted");
            registrations.update(reachable, delisted);
            return Json.write(Map.of("ok", true, "delisted", delisted.size()));
        });
    }

    private String reconcile(Request req, Response res) {
        JsonObject body = parseObject(req.body());
        String jar = body.has("jar") && !body.get("jar").isJsonNull() ? body.get("jar").getAsString() : "";
        if (jar.isBlank() || jar.contains("/") || jar.contains("\\") || jar.contains("..")) {
            res.status(400);
            return Json.write(Map.of("error", "jar must be a bare filename"));
        }
        List<String> names = readArray(body, "names");
        if (names.isEmpty()) {
            names = JarSkew.skewedContainers(fingerprints.latest().entries(), jar);
        }
        try {
            OperationJob job = deployService.reconcile(jar, names, optInt(body, "stagger"), actor(req));
            fingerprints.invalidate();
            return Json.write(ControlRoutes.describeJob(job));
        } catch (IllegalArgumentException e) {
            res.status(400);
            return Json.write(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    private String configSync(Request req, Response res) {
        JsonObject body = parseObject(req.body());
        boolean restart = !body.has("restart") || body.get("restart").getAsBoolean();
        try {
            OperationJob job = configDrift.sync(readArray(body, "names"), restart, optInt(body, "stagger"), actor(req));
            return Json.write(ControlRoutes.describeJob(job));
        } catch (IllegalArgumentException e) {
            res.status(400);
            return Json.write(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    /** Game servers with what service.api needs to match them to proxy registrations. */
    private String servers(Request req, Response res) {
        List<Map<String, Object>> servers = new ArrayList<>();
        for (ContainerInfo container : fleetService.snapshot().containers()) {
            if (container.group() != FleetGroup.GAME_SERVER) {
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", container.name());
            entry.put("state", container.state().name());
            entry.put("startedAt", container.startedAt());
            try {
                JsonObject inspect = docker.inspectRaw(container.name());
                ContainerFacts facts = inspect == null ? null : ContainerFacts.from(container.name(), inspect);
                entry.put("id", facts == null ? container.id() : facts.id());
                entry.put("hostname", facts == null ? null : facts.hostname());
                entry.put("serverType", facts == null || facts.command() == null ? null : facts.command().serverType());
            } catch (DockerException e) {
                entry.put("id", container.id());
                entry.put("hostname", null);
                entry.put("serverType", null);
            }
            servers.add(entry);
        }
        return Json.write(servers);
    }

    private static boolean refresh(Request req) {
        return "true".equalsIgnoreCase(req.queryParams("refresh"));
    }

    private static String actor(Request req) {
        String actor = req.headers("X-Actor");
        return actor == null || actor.isBlank() ? "unknown" : actor;
    }

    private static List<String> readNames(JsonObject body) {
        return readArray(body, "names");
    }

    private static JsonObject parseObject(String body) {
        if (body == null || body.isBlank()) {
            return new JsonObject();
        }
        try {
            JsonObject parsed = Json.gson().fromJson(body, JsonObject.class);
            return parsed == null ? new JsonObject() : parsed;
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    private static List<String> readArray(JsonObject body, String key) {
        if (!body.has(key) || !body.get(key).isJsonArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonElement element : body.getAsJsonArray(key)) {
            if (!element.isJsonNull() && !element.getAsString().isBlank()) {
                values.add(element.getAsString());
            }
        }
        return values;
    }

    private static int optInt(JsonObject body, String key) {
        try {
            return body.has(key) && !body.get(key).isJsonNull() ? body.get(key).getAsInt() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
