package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.calendar.CalendarEvent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EventsWidget implements TabWidget {
    private static final List<CalendarEvent> TIMED = List.of(CalendarEvent.DARK_AUCTION, CalendarEvent.FARMING_CONTEST);

    @Override
    public String id() {
        return "events";
    }

    @Override
    public String displayName() {
        return "Event";
    }

    @Override
    public Material icon() {
        return Material.CLOCK;
    }

    @Override
    public int defaultPriority() {
        return 30;
    }

    @Override
    public boolean enabledByDefault(TabLocation location) {
        return location == TabLocation.HUB;
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<d><l>Event:"));
        int year = SkyBlockCalendar.getYear();
        for (CalendarEvent active : SkyBlockCalendar.getCurrentEvents()) {
            lines.add(Text.of(" {} <a>Active", active.getDisplayName(year)));
        }
        for (Map.Entry<SkyBlockCalendar.EventInfo, CalendarEvent> next
                : SkyBlockCalendar.getEventsWithDurationUntilSkipSpecific(1, TIMED).entrySet()) {
            lines.add(Text.of(" {} <e>{}", next.getValue().getDisplayName(next.getKey().year()),
                    TabTime.formatTicks(next.getKey().timeUntilBegin())));
        }
        for (CalendarEvent event : TIMED) {
            long ticks = SkyBlockCalendar.ticksUntilEvent(event);
            if (ticks < 0) continue;
            lines.add(Text.of(" {} <e>{}", event.getDisplayName(year), TabTime.formatTicks(ticks)));
        }
        return lines.size() == 1 ? List.of() : lines;
    }
}
