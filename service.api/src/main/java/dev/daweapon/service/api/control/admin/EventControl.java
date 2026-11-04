package dev.daweapon.service.api.control.admin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.model.Filters;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.protocol.objects.admin.AdminEventTriggerProtocol;
import dev.daweapon.commons.protocol.objects.admin.AdminEventTriggerProtocol.TriggerRequest;
import dev.daweapon.commons.protocol.objects.admin.AdminEventTriggerProtocol.TriggerResponse;
import dev.daweapon.commons.protocol.objects.darkauction.GetAuctionStateProtocol;
import dev.daweapon.commons.protocol.objects.darkauction.TriggerDarkAuctionProtocol;
import dev.daweapon.commons.redis.RedisClient;
import dev.daweapon.commons.skyblock.SharedCalendarClock;
import dev.daweapon.service.api.control.admin.CalendarDivergence.Assessment;
import dev.daweapon.service.api.control.admin.CalendarDivergence.SkyBlockDate;
import dev.daweapon.service.api.control.admin.FleetStatus.ServerReport;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// Event triggers reuse what the staff commands call: Dark Auction goes straight to its service, mayor
// and calendar triggers run on one SkyBlock server (the election service and the shared epoch carry
// them to the rest), and mining events go to every server because only Mines servers can start one.
public final class EventControl {
    public record ServerDate(String serverId, String serverType, @Nullable String serverName, SkyBlockDate date,
                             boolean shared, long offset) {}

    public record Calendar(@Nullable SkyBlockDate reference, boolean sharedClock, List<ServerDate> servers,
                           Assessment divergence) {}

    public record Mayor(@Nullable String currentMayor, @Nullable String currentMinister, boolean electionOpen) {}

    public record Overview(Calendar calendar, Mayor mayor, @Nullable GetAuctionStateProtocol.GetAuctionStateResponse darkAuction,
                           Map<String, List<String>> options) {}

    public record Outcome(boolean ok, String detail) {}

    private static final AdminEventTriggerProtocol TRIGGER = new AdminEventTriggerProtocol();
    private static final long OPTIONS_CACHE_MILLIS = 10 * 60 * 1000L;

    private static volatile Map<String, List<String>> options = Map.of();
    private static volatile long optionsAt;

    private EventControl() {}

    public static Overview overview() throws Exception {
        return new Overview(calendar(FleetStatus.snapshot(true)), mayor(), darkAuctionState(), options());
    }

    public static Calendar calendar(List<ServerReport> reports) {
        Long epoch = sharedEpoch();
        Long referenceElapsed = epoch == null ? null : SharedCalendarClock.elapsedAt(epoch, System.currentTimeMillis());
        List<ServerDate> servers = new ArrayList<>();
        List<Long> reported = new ArrayList<>();
        long baseline = referenceElapsed != null ? referenceElapsed
                : reports.stream().filter(ServerReport::isSkyBlock).mapToLong(r -> r.status().calendar().elapsed()).max().orElse(0L);
        for (ServerReport report : reports) {
            if (!report.isSkyBlock()) continue;
            long elapsed = report.status().calendar().elapsed();
            reported.add(elapsed);
            servers.add(new ServerDate(report.serverId().toString(), report.status().serverType(),
                    report.status().serverName(), CalendarDivergence.describe(elapsed),
                    report.status().calendar().shared(), elapsed - baseline));
        }
        return new Calendar(referenceElapsed == null ? null : CalendarDivergence.describe(referenceElapsed),
                epoch != null, servers, CalendarDivergence.assess(reported, referenceElapsed));
    }

    public static Outcome trigger(String kind, @Nullable String value) throws Exception {
        return switch (kind) {
            case "dark-auction" -> darkAuction();
            case "force-mayor" -> onOneSkyBlockServer(AdminEventTriggerProtocol.FORCE_MAYOR, value);
            case "special-mayor" -> onOneSkyBlockServer(AdminEventTriggerProtocol.SPECIAL_MAYOR, null);
            case "calendar-skip" -> onOneSkyBlockServer(AdminEventTriggerProtocol.CALENDAR_SKIP, value);
            case "mining-event" -> onEveryServer(AdminEventTriggerProtocol.MINING_EVENT, value);
            default -> throw new IllegalArgumentException("unknown trigger '" + kind
                    + "', expected dark-auction, force-mayor, special-mayor, calendar-skip or mining-event");
        };
    }

    private static Outcome darkAuction() throws Exception {
        long elapsed = currentElapsed();
        TriggerDarkAuctionProtocol.TriggerResponse response = RedisClient.requestService(ServiceType.DARK_AUCTION,
                new TriggerDarkAuctionProtocol(), new TriggerDarkAuctionProtocol.TriggerMessage(elapsed, true))
                .get(6, TimeUnit.SECONDS);
        return new Outcome(response.success(), response.message());
    }

    private static Outcome onOneSkyBlockServer(String kind, @Nullable String value) throws Exception {
        ServerReport server = FleetStatus.pickSkyBlockServer()
                .orElseThrow(() -> new IllegalStateException("no SkyBlock server answered the status broadcast"));
        TriggerResponse response = FleetStatus.ask(server.serverId(), TRIGGER, new TriggerRequest(kind, value), 15_000L);
        return new Outcome(response.applied(), response.detail() + " (via " + server.status().serverName() + ")");
    }

    private static Outcome onEveryServer(String kind, @Nullable String value) throws Exception {
        Map<UUID, TriggerResponse> responses = FleetStatus.broadcast(TRIGGER, new TriggerRequest(kind, value), 6000);
        List<String> applied = new ArrayList<>();
        List<String> refused = new ArrayList<>();
        responses.forEach((server, response) -> {
            if (response == null) return;
            if (response.applied()) applied.add(response.detail());
            else if (!response.detail().startsWith("no mining events")) refused.add(response.detail());
        });
        if (!applied.isEmpty()) return new Outcome(true, applied.size() + " server(s): " + String.join("; ", applied));
        if (!refused.isEmpty()) return new Outcome(false, String.join("; ", refused));
        return new Outcome(false, "no running server hosts mining events");
    }

    private static Map<String, List<String>> options() {
        if (!options.isEmpty() && System.currentTimeMillis() - optionsAt < OPTIONS_CACHE_MILLIS) return options;
        try {
            ServerReport server = FleetStatus.pickSkyBlockServer().orElse(null);
            if (server == null) return options;
            TriggerResponse response = FleetStatus.ask(server.serverId(), TRIGGER,
                    new TriggerRequest(AdminEventTriggerProtocol.OPTIONS, null), 5_000L);
            if (response.options() != null) {
                options = response.options();
                optionsAt = System.currentTimeMillis();
            }
        } catch (Exception e) {
            Logger.warn(e, "Failed to read event trigger options");
        }
        return options;
    }

    private static Mayor mayor() {
        try {
            Document stored = AdminBackends.database().getCollection("elections")
                    .find(Filters.eq("_id", "election_data")).first();
            if (stored == null || stored.getString("data") == null) return new Mayor(null, null, false);
            JsonObject data = JsonParser.parseString(stored.getString("data")).getAsJsonObject();
            return new Mayor(text(data, "currentMayor"), text(data, "currentMinister"),
                    data.has("electionOpen") && data.get("electionOpen").getAsBoolean());
        } catch (Exception e) {
            Logger.warn(e, "Failed to read the election state");
            return new Mayor(null, null, false);
        }
    }

    public static @Nullable GetAuctionStateProtocol.GetAuctionStateResponse darkAuctionState() {
        try {
            return RedisClient.requestService(ServiceType.DARK_AUCTION, new GetAuctionStateProtocol(),
                    new GetAuctionStateProtocol.GetAuctionStateMessage()).get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }

    private static long currentElapsed() throws Exception {
        Long epoch = sharedEpoch();
        if (epoch != null) return SharedCalendarClock.elapsedAt(epoch, System.currentTimeMillis());
        return FleetStatus.snapshot(false).stream().filter(ServerReport::isSkyBlock)
                .mapToLong(report -> report.status().calendar().elapsed()).max().orElse(0L);
    }

    private static @Nullable Long sharedEpoch() {
        try {
            return SharedCalendarClock.read();
        } catch (Exception e) {
            return null;
        }
    }

    private static @Nullable String text(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : null;
    }
}
