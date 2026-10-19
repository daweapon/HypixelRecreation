package dev.daweapon.type.dwarvenmines.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.miningevent.MiningEvent;
import dev.daweapon.type.skyblockgeneric.miningevent.MiningEventScheduler;
import dev.daweapon.type.skyblockgeneric.miningevent.MiningEvents;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIOldManGarry extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Old Man Garry", InventoryType.CHEST_3_ROW);
    }

    private static String duration(long millis) {
        long seconds = Math.max(0, millis / 1000);
        return seconds >= 60 ? seconds / 60 + "m " + seconds % 60 + "s" : seconds + "s";
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        layout.slot(11, (_, _) -> {
            MiningEvent current = MiningEvents.current().orElse(null);
            String running = current == null ? "<7>No event is running." : "<7>Running: " + current.type().coloredName()
                    + "\n<7>Ends in <e>" + duration(current.millisLeft());
            return ItemStacks.item(Material.CLOCK, "<a>Event Schedule\n<7>Events happen around the Dwarven Mines\n<7>every <e>20 minutes<7>.\n\n"
                    + running + "\n\n<7>Next event in <e>" + duration(MiningEventScheduler.millisUntilNextCycle()));
        });

        layout.slot(13, (_, _) -> {
            MiningEvent current = MiningEvents.current().orElse(null);
            boolean warp = current != null && current.warpLocation() != null;
            return ItemStacks.item(warp ? Material.ENDER_PEARL : Material.BARRIER, warp
                    ? "<a>Warp to the Event\n<7>Take me to the " + current.type().coloredName() + "<7>!\n\n<e>Click to warp!"
                    : "<c>No Event to Warp To\n<7>Come back when an active event\n<7>is running.");
        }, (_, c) -> {
            MiningEvent current = MiningEvents.current().orElse(null);
            if (current == null || current.warpLocation() == null) {
                player.sendMessage("<c>There is no event to warp to right now!");
                return;
            }
            c.player().closeInventory();
            player.teleport(current.warpLocation());
        });

        layout.slot(15, ItemStacks.item(Material.BOOK, """
                <a>Mining Events
                <7>Passive: <b>2x Powder<7>, <d>Better Together
                <7>Active: <c>Goblin Raid<7>, <6>Raffle<7>,
                <b>Mithril Gourmand
                <7>Random: <5>Fallen Stars<7>, <6>Powder Ghasts"""));
        Components.close(layout, 22);
    }
}
