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
        return new ViewConfiguration<>("Mining Events Guide", InventoryType.CHEST_4_ROW);
    }

    private static String duration(long millis) {
        long seconds = Math.max(0, millis / 1000);
        return seconds >= 60 ? seconds / 60 + "m " + seconds % 60 + "s" : seconds + "s";
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        layout.slot(11, ItemStacks.item(Material.BOOK, """
                <a>General Information

                <b>Mining Events <7>occur in the
                <2>Dwarven Mines <7>every <e>20 <7>minutes.
                <8>(Around same time on all servers)

                <e>Active events <7>require all players
                <7>to group together, while <a>passive
                <a>events <7>offer optional bonuses.

                <7>You may also find <5>random events
                <7>happening around the island at
                <7>any time!"""));

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

        layout.slot(15, (_, _) -> {
            MiningEvent current = MiningEvents.current().orElse(null);
            if (current == null) {
                return ItemStacks.item(Material.GRAY_DYE, "<a>No Current Event\n\n<7>The next event will be announced in\n<7>about <a>"
                        + duration(MiningEventScheduler.millisUntilNextCycle()) + "<7>!");
            }
            return ItemStacks.item(Material.LIME_DYE, "<a>Current Event\n\n<7>Running: " + current.type().coloredName()
                    + "\n<7>Ends in <e>" + duration(current.millisLeft()));
        });
        Components.close(layout, 31);
    }
}
