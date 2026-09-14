package net.swofty.type.skyblockgeneric.redis.service;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.calendar.ResetCalendarProtocol;
import net.swofty.commons.protocol.objects.calendar.ResetCalendarProtocol.ResetMessage;
import net.swofty.commons.protocol.objects.calendar.ResetCalendarProtocol.ResetResponse;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.server.attribute.SkyBlockServerAttributes;

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
