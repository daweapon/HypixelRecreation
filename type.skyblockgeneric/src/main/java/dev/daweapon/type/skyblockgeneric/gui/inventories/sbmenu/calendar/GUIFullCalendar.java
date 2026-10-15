package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.calendar;

import net.minestom.server.component.DataComponents;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.calendar.CalendarEvent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;

import java.util.ArrayList;
import java.util.List;

// The full day-by-day SkyBlock Calendar, reached from a button next to Chocolate Factory in
// GUICalendar - matches real Hypixel's split between a short "upcoming events" list (GUICalendar)
// and this month grid, which is also where Dark Auction occurrences actually live.
public class GUIFullCalendar implements StatefulView<GUIFullCalendar.FullCalendarState> {

    private static final int PREVIOUS_MONTH_SLOT = 45;
    private static final int BACK_SLOT = 48;
    private static final int CLOSE_SLOT = 49;
    private static final int NEXT_MONTH_SLOT = 53;

    @Override
    public FullCalendarState initialState() {
        return new FullCalendarState(0);
    }

    @Override
    public ViewConfiguration<FullCalendarState> configuration() {
        return ViewConfiguration.withText((state, _) -> {
            DisplayedMonth displayed = resolve(state);
            return Text.of("{}, Year {}", SkyBlockCalendar.getMonthName(displayed.month()), displayed.year());
        }, InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<FullCalendarState> layout, FullCalendarState state, ViewContext ctx) {
        Components.fill(layout);

        DisplayedMonth displayed = resolve(state);
        boolean hasVisitedDarkAuction = ctx.player().getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_VISITED_DARK_AUCTION);
        List<CalendarEvent> skip = hasVisitedDarkAuction
                ? List.of(CalendarEvent.PERKPOCALYPSE)
                : List.of(CalendarEvent.PERKPOCALYPSE, CalendarEvent.DARK_AUCTION);

        boolean isCurrentMonth = displayed.year() == SkyBlockCalendar.getYear() && displayed.month() == SkyBlockCalendar.getMonth();
        int currentDay = SkyBlockCalendar.getDay();

        for (int day = 1; day <= 31; day++) {
            int slot = day - 1;
            CalendarEvent event = SkyBlockCalendar.getEventForDay(displayed.month(), day, skip);
            boolean isToday = isCurrentMonth && day == currentDay;

            layout.slot(slot, dayItem(day, displayed, event, isToday));
        }

        if (displayed.absoluteMonth() > 0) {
            layout.slot(PREVIOUS_MONTH_SLOT, ItemStacks.named(Material.ARROW, "<a>Previous Month"),
                    (_, c) -> c.session(FullCalendarState.class).update(s -> new FullCalendarState(s.monthOffset() - 1)));
        }

        Components.back(layout, BACK_SLOT, ctx);
        Components.close(layout, CLOSE_SLOT);

        layout.slot(NEXT_MONTH_SLOT, ItemStacks.named(Material.ARROW, "<a>Next Month"),
                (_, c) -> c.session(FullCalendarState.class).update(s -> new FullCalendarState(s.monthOffset() + 1)));
    }

    private ItemStack.Builder dayItem(int day, DisplayedMonth displayed, CalendarEvent event, boolean isToday) {
        String monthName = SkyBlockCalendar.getMonthName(displayed.month());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>{} {}, Year {}", StringUtility.ntify(day), monthName, displayed.year()));
        lore.add(Text.literal(" "));
        if (isToday) {
            lore.add(Text.of("<a>★ Today!"));
        }
        if (event != null) {
            if (isToday) lore.add(Text.literal(" "));
            lore.addAll(event.description());
        } else if (!isToday) {
            lore.add(Text.of("<7>No events."));
        }

        if (isToday) {
            ItemStack.Builder todayItem = ItemStack.of(Material.LIME_DYE)
                    .with(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .builder();
            return ItemStacks.text(todayItem, Text.of("<a>Today - {} {}", StringUtility.ntify(day), monthName), lore);
        }

        if (event != null) {
            return ItemStacks.text(event.representation().builder(), event.getDisplayName(displayed.year()), lore);
        }

        return ItemStacks.text(ItemStack.of(Material.PAPER).builder(),
                Text.of("<7>{} {}", StringUtility.ntify(day), monthName), lore);
    }

    private DisplayedMonth resolve(FullCalendarState state) {
        int currentAbsoluteMonth = (SkyBlockCalendar.getYear() - 1) * 12 + (SkyBlockCalendar.getMonth() - 1);
        int absoluteMonth = Math.max(0, currentAbsoluteMonth + state.monthOffset());
        return new DisplayedMonth(absoluteMonth, absoluteMonth / 12 + 1, absoluteMonth % 12 + 1);
    }

    private record DisplayedMonth(int absoluteMonth, int year, int month) {}

    public record FullCalendarState(int monthOffset) {}
}
