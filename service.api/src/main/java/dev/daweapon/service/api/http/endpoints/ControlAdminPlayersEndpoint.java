package dev.daweapon.service.api.http.endpoints;

import com.google.gson.JsonObject;
import dev.daweapon.commons.protocol.objects.admin.AdminServerStatusProtocol.OnlinePlayer;
import dev.daweapon.service.api.control.ControlAuth;
import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.admin.FleetStatus;
import dev.daweapon.service.api.http.APIResponse;
import spark.Request;
import spark.Response;

import java.util.List;

/** Everyone online across the game servers, grouped by the server they are on. */
public class ControlAdminPlayersEndpoint extends ControlAdminEndpoint {
    private record Server(String serverId, String serverType, String serverName, List<OnlinePlayer> players) {}

    private record Payload(int totalPlayers, List<Server> servers) {}

    @Override
    public String getPath() {
        return "/control/admin/players";
    }

    @Override
    protected ControlRole requiredRole() {
        return ControlRole.READ_ONLY;
    }

    @Override
    protected APIResponse serve(ControlAuth.Caller caller, JsonObject body, Request req, Response res) throws Exception {
        List<Server> servers = FleetStatus.snapshot(flag(req, "refresh")).stream()
                .map(report -> new Server(report.serverId().toString(), report.status().serverType(),
                        report.status().serverName(), report.status().players()))
                .toList();
        int total = servers.stream().mapToInt(server -> server.players().size()).sum();
        return ok(new Payload(total, servers));
    }
}
