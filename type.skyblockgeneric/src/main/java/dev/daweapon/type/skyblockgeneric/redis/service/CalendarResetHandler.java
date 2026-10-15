package dev.daweapon.type.skyblockgeneric.redis.service;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.calendar.ResetCalendarProtocol;
import dev.daweapon.commons.protocol.objects.calendar.ResetCalendarProtocol.ResetMessage;
import dev.daweapon.commons.protocol.objects.calendar.ResetCalendarProtocol.ResetResponse;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.server.attribute.SkyBlockServerAttributes;

// Receives the cross-server reset broadcast triggered by /adminbypass resetyear (see
// ResetCalendarProtocol) and applies + force-persists the reset on this server.
public class CalendarResetHandler implements RedisMessageHandler<ResetMessage, ResetResponse> {

    private static final ResetCalendarProtocol PROTOCOL = new ResetCalendarProtocol();

    @Override
    public RedisProtocol<ResetMessage, ResetResponse> protocol() {
        return PROTOCOL;
    }

    @Override
    public ResetResponse handle(ResetMessage message, RedisMessageContext context) {
        SkyBlockCalendar.resetToYearOne();
        SkyBlockServerAttributes.forceSave();
        return new ResetResponse(true);
    }
}
