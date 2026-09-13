package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.calendar;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.calendar.CalendarEvent;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.gui.inventories.rabbits.GUIChocolateFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUICalendar extends StatelessView {

    private static final int[] EVENT_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 26};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Calendar and Events", InventoryType.CHEST_5_ROW);
    }

    private static final int HALVE_DARK_AUCTION_TOGGLE_SLOT = 41;
    private static final int CHOCOLATE_FACTORY_SLOT = 42;
    private static final String CHOCOLATE_TEXTURE = "9a815398e7da89b1bc08f646cafc8e7b813da0be0eec0cce6d3eff5207801026";

    private static Map<SkyBlockCalendar.EventInfo, CalendarEvent> queryEvents(ViewContext ctx) {
        boolean includeDarkAuction = ctx.player().getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_VISITED_DARK_AUCTION);
        if (!includeDarkAuction) {
            return SkyBlockCalendar.getEventsWithDurationUntilSkipSpecific(14,
                    List.of(CalendarEvent.DARK_AUCTION, CalendarEvent.PERKPOCALYPSE));
        }

        boolean halveDarkAuction = ctx.player().getToggles().get(DatapointToggles.Toggles.ToggleType.HALVE_DARK_AUCTION_CALENDAR_ENTRIES);
        return SkyBlockCalendar.getEventsWithDurationUntilSkipSpecific(14,
                List.of(CalendarEvent.PERKPOCALYPSE), halveDarkAuction);
    }

    private record SlotOccurrence(long eventAbsoluteTime, Duration duration, int year, CalendarEvent event) {}

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 40);
        Components.back(layout, 39, ctx);

        Map<SkyBlockCalendar.EventInfo, CalendarEvent> initialEvents = queryEvents(ctx);
        long baseElapsed = SkyBlockCalendar.getElapsed();

        // Anchor each slot to the absolute tick its occurrence begins at, rather than re-looking it
        // up by event type on every refresh - multiple slots often share the same CalendarEvent
        // (e.g. several distinct Dark Auction occurrences), and matching by type alone made every
        // one of those slots collapse onto whichever occurrence of that type came up first.
        List<SlotOccurrence> orderedOccurrences = initialEvents.entrySet().stream()
            .map(e -> new SlotOccurrence(
                baseElapsed + e.getKey().timeUntilBegin(),
                e.getKey().duration(),
                e.getKey().year(),
                e.getValue()))
            .toList();

        for (int i = 0; i < Math.min(orderedOccurrences.size(), EVENT_SLOTS.length); i++) {
            final SlotOccurrence occurrence = orderedOccurrences.get(i);
            final int slot = EVENT_SLOTS[i];

            layout.autoUpdating(slot, (_, _) -> {
                long timeUntilBegin = Math.max(0, occurrence.eventAbsoluteTime() - SkyBlockCalendar.getElapsed());

                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<7>Starts in: <e>{:time}", timeUntilBegin * 50L));
                if (!occurrence.duration().isZero()) {
                    lore.add(Text.of("<7>Event lasts for <e>{:time}<7>!", occurrence.duration().toMillis()));
                }
                lore.add(Text.literal(" "));
                lore.addAll(occurrence.event().description());

                return ItemStacks.text(occurrence.event().representation().builder(), occurrence.event().getDisplayName(occurrence.year()), lore);
            }, Duration.ofSeconds(1));
        }

        layout.autoUpdating(HALVE_DARK_AUCTION_TOGGLE_SLOT, (_, c) -> {
            boolean enabled = c.player().getToggles().get(DatapointToggles.Toggles.ToggleType.HALVE_DARK_AUCTION_CALENDAR_ENTRIES);
            String template = enabled
                ? "<e>Halve Dark Auction Entries\n<a>Status: ENABLED\n\n<7>At most half of the slots above\n<7>can be Dark Auction, leaving\n<7>room for other events."
                : "<e>Halve Dark Auction Entries\n<c>Status: DISABLED\n\n<7>Every upcoming Dark Auction\n<7>is shown here, which can\n<7>crowd out other events.";
            return ItemStacks.item(Material.HOPPER, template);
        }, (click, c) -> {
            c.player().getToggles().inverse(DatapointToggles.Toggles.ToggleType.HALVE_DARK_AUCTION_CALENDAR_ENTRIES);
            c.player().openView(new GUICalendar());
        }, Duration.ofSeconds(1));

        layout.slot(CHOCOLATE_FACTORY_SLOT, (_, _) -> ItemStacks.head(CHOCOLATE_TEXTURE, """
                <6>Chocolate Factory
                <7>Hop on over to your <6>Chocolate Factory
                <7>and see how your <a>Chocolate Rabbits
                <7>are doing!

                <e>Click to view!"""),
                (click, c) -> c.player().openView(new GUIChocolateFactory()));
    }
}