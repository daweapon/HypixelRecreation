package dev.daweapon.type.galatea.gui;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

public class GUIHoney extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Honey", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);

        layout.slot(19, ItemStacks.item(Material.BARRIER, """
                <a>Step 1: Find a Protected Tree
                <7>You can spot if a <a>Tree <7>has an active
                <c>Tree Protection Order <7>if there is a
                <7>hologram saying “<c><l>TREE
                <c><l>PROTECTION ORDER<7>” next to it.
                <7>How nifty!"""));

        layout.slot(21, ItemStacks.item(Material.HONEYCOMB, """
                <a>Step 2: Smear it with Honeycomb
                <7>Craft a <a>Fun Sized Pot of Honeycomb
                <7>from the <a>Honeycomb Collection<7>, go up
                <7>to a <c>Protected Tree<7>, and smear it!
                <7>Have no mercy!

                <7>You can progress through the
                <a>Honeycomb Collection <7>by looting the
                <7>various <6>Honeyhives <7>found around
                <6>Torrhus Canyon <7>that refill
                <7>periodically."""));

        layout.slot(23, ItemStacks.item(Material.CLOCK, """
                <a>Step 3: Wait a While…
                <7>These things take time. Go cut some
                <a>Trees<7>, hunt some <a>Critters<7>, catch
                <7>some <6>Torrhus Sea Creatures<7>, I don't
                <7>know, I'm not your babysitter!
                <7>Entertain yourself!"""));

        layout.slot(25, ItemStacks.item(Material.ZOMBIE_HEAD, """
                <a>Step 4: Happy Huntin'!
                <7>Keep an eye on your chat and you'll
                <7>be notified when a <a>Critter <7>takes
                <7>the bait, then go and <e>Hunt <7>it!

                <7>The type of <a>Critter <7>that shows up
                <7>depends upon the type of <c>Protected
                <c>Tree <7>you smeared:

                <a>Fig Tree<7>: <f>Honeyhog<7>, <a>Honeymite<7>, or <9>Hewver
                <2>Mangrove Tree<7>: <f>Stag Beetle<7>, <a>Woodlouse<7>, or <9>Murkbat
                <6>Helix Tree<7>: <f>Pollendart<7>, <a>Parched<7>, or <9>Hivethief

                <7>Once you've caught the <a>Critter
                <7>that showed up, repeat the process
                <7>from <a>Step 1<7>!"""));
    }
}
