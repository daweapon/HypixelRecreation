package dev.daweapon.type.skyblockgeneric.redis.service;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.admin.AdminEventTriggerProtocol;
import dev.daweapon.commons.protocol.objects.admin.AdminEventTriggerProtocol.TriggerRequest;
import dev.daweapon.commons.protocol.objects.admin.AdminEventTriggerProtocol.TriggerResponse;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.skyblockgeneric.calendar.CalendarEvent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.elections.ElectionManager;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.miningevent.MiningEventScheduler;
import dev.daweapon.type.skyblockgeneric.miningevent.MiningEventType;
import dev.daweapon.type.skyblockgeneric.miningevent.RandomMiningEvents;
import net.minestom.server.MinecraftServer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

// Control-app counterpart of /adminbypass forcemayor, skiptospecialmayor, <event> and /force event.
// Election changes reach the other servers through the election service resync, and calendar
// skips through the shared epoch, so those only need to run on one server.
public class AdminEventTriggerHandler implements RedisMessageHandler<TriggerRequest, TriggerResponse> {
    private static final AdminEventTriggerProtocol PROTOCOL = new AdminEventTriggerProtocol();

    @Override
    public RedisProtocol<TriggerRequest, TriggerResponse> protocol() {
        return PROTOCOL;
    }

    @Override
    public TriggerResponse handle(TriggerRequest message, RedisMessageContext context) {
        try {
            return switch (message.kind()) {
                case AdminEventTriggerProtocol.OPTIONS -> options();
                case AdminEventTriggerProtocol.FORCE_MAYOR -> forceMayor(message.value());
                case AdminEventTriggerProtocol.SPECIAL_MAYOR -> specialMayor();
                case AdminEventTriggerProtocol.MINING_EVENT -> miningEvent(message.value());
                case AdminEventTriggerProtocol.CALENDAR_SKIP -> calendarSkip(message.value());
                default -> new TriggerResponse(false, "unknown trigger " + message.kind(), null);
            };
        } catch (Exception e) {
            Logger.error(e, "Admin trigger {} failed", message.kind());
            return new TriggerResponse(false, "failed: " + e.getMessage(), null);
        }
    }

    private static TriggerResponse options() {
        Map<String, List<String>> options = new LinkedHashMap<>();
        options.put("mayors", Arrays.stream(SkyBlockMayor.values()).map(Enum::name).toList());
        List<String> mining = new ArrayList<>(Arrays.stream(MiningEventType.values()).map(Enum::name).toList());
        mining.add("END");
        options.put("miningEvents", mining);
        options.put("calendarEvents", CalendarEvent.getAllEvents().stream().map(CalendarEvent::id).toList());
        return new TriggerResponse(true, "options", options);
    }

    private static TriggerResponse forceMayor(String name) {
        SkyBlockMayor mayor = SkyBlockMayor.valueOf(name == null ? "" : name.trim().toUpperCase());
        ElectionManager.forceElectMayor(mayor);
        return new TriggerResponse(true, "force-elected " + mayor.getDisplayName(), null);
    }

    private static TriggerResponse specialMayor() {
        SkyBlockMayor elected = ElectionManager.forceElectSpecialMayor();
        return elected == null
                ? new TriggerResponse(false, "no special mayors are registered", null)
                : new TriggerResponse(true, "force-elected special mayor " + elected.getDisplayName(), null);
    }

    private static TriggerResponse miningEvent(String name) throws Exception {
        if (MiningEventScheduler.zone() == null) return new TriggerResponse(false, "no mining events on this server", null);
        String wanted = name == null ? "" : name.trim().toUpperCase();
        if (wanted.equals("END")) {
            return onTick(() -> {
                MiningEventScheduler.stopCurrent();
                RandomMiningEvents.endFallenStar();
                return new TriggerResponse(true, "ended the running mining event", null);
            });
        }
        MiningEventType type = MiningEventType.valueOf(wanted);
        return onTick(() -> MiningEventScheduler.force(type, true)
                ? new TriggerResponse(true, "started " + type.name(), null)
                : new TriggerResponse(false, type.name() + " cannot run here", null));
    }

    private static TriggerResponse calendarSkip(String id) {
        CalendarEvent event = CalendarEvent.fromId(id == null ? "" : id.trim());
        if (event == null) return new TriggerResponse(false, "unknown calendar event " + id, null);
        long ticks = SkyBlockCalendar.ticksUntilEvent(event);
        if (ticks < 0) return new TriggerResponse(false, "that event has no scheduled occurrence", null);
        SkyBlockCalendar.setElapsed(SkyBlockCalendar.getElapsed() + ticks - 20);
        return new TriggerResponse(true, "skipped to " + event.id() + ", now " + SkyBlockCalendar.getMonthName()
                + " " + SkyBlockCalendar.getDay() + ", Year " + SkyBlockCalendar.getYear(), null);
    }

    private static TriggerResponse onTick(Supplier<TriggerResponse> work) throws Exception {
        CompletableFuture<TriggerResponse> result = new CompletableFuture<>();
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            try {
                result.complete(work.get());
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result.get(5, TimeUnit.SECONDS);
    }
}
