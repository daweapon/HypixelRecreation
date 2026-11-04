package dev.daweapon.commons.protocol.objects.admin;

import dev.daweapon.commons.protocol.RedisProtocol;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

// Broadcast from the control API to every game server: who is online there and, on SkyBlock
// servers, which SkyBlock date that server currently believes it is.
public class AdminServerStatusProtocol extends RedisProtocol<
        AdminServerStatusProtocol.StatusRequest,
        AdminServerStatusProtocol.StatusResponse> {

    public AdminServerStatusProtocol() {
        super(StatusRequest.class, StatusResponse.class);
    }

    public record StatusRequest() {}

    public record OnlinePlayer(UUID uuid, String name, String rank, boolean staff) {}

    public record CalendarState(long elapsed, int year, int month, String monthName, int day, int hour,
                                boolean shared) {}

    public record StatusResponse(String serverType, @Nullable String serverName, List<OnlinePlayer> players,
                                 @Nullable CalendarState calendar) {}
}
