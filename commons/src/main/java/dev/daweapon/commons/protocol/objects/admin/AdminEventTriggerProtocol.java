package dev.daweapon.commons.protocol.objects.admin;

import dev.daweapon.commons.protocol.RedisProtocol;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

// Sent from the control API to SkyBlock game servers to run the same triggers the staff /force and
// /adminbypass commands use. OPTIONS asks a server for the values each trigger accepts.
public class AdminEventTriggerProtocol extends RedisProtocol<
        AdminEventTriggerProtocol.TriggerRequest,
        AdminEventTriggerProtocol.TriggerResponse> {

    public static final String OPTIONS = "OPTIONS";
    public static final String FORCE_MAYOR = "FORCE_MAYOR";
    public static final String SPECIAL_MAYOR = "SPECIAL_MAYOR";
    public static final String MINING_EVENT = "MINING_EVENT";
    public static final String CALENDAR_SKIP = "CALENDAR_SKIP";

    public AdminEventTriggerProtocol() {
        super(TriggerRequest.class, TriggerResponse.class);
    }

    public record TriggerRequest(String kind, @Nullable String value) {}

    public record TriggerResponse(boolean applied, String detail, @Nullable Map<String, List<String>> options) {}
}
