package dev.daweapon.type.generic.redis.service;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.admin.AdminServerStatusProtocol;
import dev.daweapon.commons.protocol.objects.admin.AdminServerStatusProtocol.CalendarState;
import dev.daweapon.commons.protocol.objects.admin.AdminServerStatusProtocol.OnlinePlayer;
import dev.daweapon.commons.protocol.objects.admin.AdminServerStatusProtocol.StatusRequest;
import dev.daweapon.commons.protocol.objects.admin.AdminServerStatusProtocol.StatusResponse;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.Rank;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AdminServerStatusHandler implements RedisMessageHandler<StatusRequest, StatusResponse> {
    private static final AdminServerStatusProtocol PROTOCOL = new AdminServerStatusProtocol();
    private static volatile Supplier<CalendarState> calendarSource = () -> null;

    public static void setCalendarSource(Supplier<CalendarState> source) {
        calendarSource = source == null ? () -> null : source;
    }

    @Override
    public RedisProtocol<StatusRequest, StatusResponse> protocol() {
        return PROTOCOL;
    }

    @Override
    public StatusResponse handle(StatusRequest message, RedisMessageContext context) {
        List<OnlinePlayer> players = new ArrayList<>();
        for (HypixelPlayer player : HypixelGenericLoader.getLoadedPlayers()) {
            players.add(describe(player));
        }

        CalendarState calendar = null;
        try {
            calendar = calendarSource.get();
        } catch (Exception e) {
            Logger.warn(e, "Failed to read the SkyBlock calendar for a status report");
        }

        return new StatusResponse(HypixelConst.getTypeLoader().getType().name(), HypixelConst.getServerName(),
                players, calendar);
    }

    private static OnlinePlayer describe(HypixelPlayer player) {
        try {
            Rank rank = player.getRank();
            return new OnlinePlayer(player.getUuid(), player.getUsername(), rank.name(), rank.isStaff());
        } catch (Exception e) {
            return new OnlinePlayer(player.getUuid(), player.getUsername(), "UNKNOWN", false);
        }
    }
}
