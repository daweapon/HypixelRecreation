package dev.daweapon.service.control.integrity;

import com.google.gson.JsonObject;
import dev.daweapon.service.control.docker.DockerApi;
import dev.daweapon.service.control.docker.DockerException;
import dev.daweapon.service.control.fleet.ContainerInfo;
import dev.daweapon.service.control.fleet.FleetGroup;
import dev.daweapon.service.control.fleet.FleetService;
import org.tinylog.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Probes the resource-pack server and compares it with what the game servers advertise.
 *
 * Both sides print their hash once, at boot, so the boot window of each container's log is
 * read rather than its tail. The public URL and the GitHub fail-safe are probed too, but only
 * reported: from inside the NAS the public hostname may not loop back, so it cannot decide DEAD.
 */
public final class PackHealthService {
    private static final Duration CACHE_TTL = Duration.ofSeconds(30);
    private static final Duration BOOT_WINDOW = Duration.ofMinutes(15);
    private static final Duration PROBE_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofSeconds(60);

    public record Report(Instant takenAt, PackVerdict.State state, String headline, List<String> problems,
                         String container, boolean running, String internalUrl, Set<String> served,
                         Map<String, String> advertised, List<HttpProbe.Outcome> probes,
                         List<HttpProbe.Outcome> external, Map<String, Boolean> verified) {
    }

    private final DockerApi docker;
    private final FleetService fleetService;
    private final String container;
    private final int port;
    private final String packName;
    private final String publicUrl;
    private final String fallbackUrl;
    private final AtomicReference<Report> cached = new AtomicReference<>();

    public PackHealthService(DockerApi docker, FleetService fleetService) {
        this.docker = docker;
        this.fleetService = fleetService;
        this.container = System.getenv().getOrDefault("PACK_SERVER_CONTAINER", "hypixel_pack_server");
        this.port = Integer.parseInt(System.getenv().getOrDefault("PACK_SERVER_PORT", "7270"));
        this.packName = System.getenv().getOrDefault("PACK_NAME", "skyblockpack");
        this.publicUrl = trimSlash(System.getenv().getOrDefault("PACK_PUBLIC_URL", "http://hypixel.glazier.app:7270"));
        this.fallbackUrl = trimSlash(System.getenv().getOrDefault("PACK_FALLBACK_URL",
                "https://github.com/daweapon/skyblock-resource-pack/releases/download/pack"));
    }

    public Report report(boolean refresh, boolean verify) {
        Report current = cached.get();
        if (!refresh && !verify && current != null
                && Duration.between(current.takenAt(), Instant.now()).compareTo(CACHE_TTL) < 0) {
            return current;
        }
        Report fresh = build(verify);
        cached.set(fresh);
        return fresh;
    }

    private Report build(boolean verify) {
        ContainerFacts pack = facts(container);
        boolean running = pack != null && pack.running();
        String base = running && pack.ipAddress() != null ? "http://" + pack.ipAddress() + ":" + port : null;

        Set<String> served = running ? PackVerdict.servedHashes(bootLog(pack)) : new TreeSet<>();
        Map<String, String> advertised = advertisedHashes();

        List<HttpProbe.Outcome> probes = new ArrayList<>();
        Map<String, HttpProbe.Outcome> byHash = new LinkedHashMap<>();
        boolean answering = false;
        if (base != null) {
            HttpProbe.Outcome root = HttpProbe.head(base + "/", PROBE_TIMEOUT);
            answering = root.answered();
            probes.add(root);
            for (String hash : new TreeSet<>(advertised.values())) {
                HttpProbe.Outcome probe = HttpProbe.head(base + "/" + hash + ".zip", PROBE_TIMEOUT);
                byHash.put(hash, probe);
                probes.add(probe);
                answering |= probe.answered();
            }
        }

        Map<String, Boolean> verified = new LinkedHashMap<>();
        if (verify && base != null) {
            for (String hash : byHash.keySet()) {
                String digest = HttpProbe.digestOf(base + "/" + hash + ".zip", "SHA-1", DOWNLOAD_TIMEOUT);
                if (digest != null) {
                    verified.put(hash, digest.equalsIgnoreCase(hash));
                }
            }
        }

        List<HttpProbe.Outcome> external = new ArrayList<>();
        for (String hash : new TreeSet<>(advertised.values())) {
            if (!publicUrl.isEmpty()) {
                external.add(HttpProbe.head(publicUrl + "/" + hash + ".zip", PROBE_TIMEOUT));
            }
            if (!fallbackUrl.isEmpty()) {
                external.add(HttpProbe.head(fallbackUrl + "/" + hash + ".zip", PROBE_TIMEOUT));
            }
        }

        PackVerdict.Result verdict = PackVerdict.evaluate(running, answering, advertised, byHash, verified);
        return new Report(Instant.now(), verdict.state(), verdict.headline(), verdict.problems(), container,
                running, base, served, advertised, probes, external, verified);
    }

    private Map<String, String> advertisedHashes() {
        Map<String, String> advertised = new LinkedHashMap<>();
        for (ContainerInfo info : fleetService.snapshot().containers()) {
            if (info.group() != FleetGroup.GAME_SERVER) {
                continue;
            }
            ContainerFacts facts = facts(info.name());
            if (facts == null || !facts.running()) {
                continue;
            }
            String hash = PackVerdict.advertisedHash(bootLog(facts), packName);
            if (hash != null) {
                advertised.put(info.name(), hash);
            }
        }
        return advertised;
    }

    private ContainerFacts facts(String name) {
        try {
            JsonObject inspect = docker.inspectRaw(name);
            return inspect == null ? null : ContainerFacts.from(name, inspect);
        } catch (DockerException e) {
            Logger.warn("Pack check could not inspect {}: {}", name, e.getMessage());
            return null;
        }
    }

    private String bootLog(ContainerFacts facts) {
        if (facts.startedAt() == null) {
            return "";
        }
        try {
            return docker.logsBetween(facts.name(), facts.startedAt().minusSeconds(5),
                    facts.startedAt().plus(BOOT_WINDOW));
        } catch (DockerException e) {
            Logger.warn("Pack check could not read boot log of {}: {}", facts.name(), e.getMessage());
            return "";
        }
    }

    private static String trimSlash(String url) {
        String trimmed = url == null ? "" : url.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}
