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

public class GUIFossilGuide extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Fossil Guide", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(19, ItemStacks.item(Material.RED_DYE, """
                <a>Step 1: Find a Glacite Mineshaft

                <7>When mining in the <b>Glacite Tunnels<7>,
                <7>you will sometimes come across a
                <b>Glacite Mineshaft<7>.

                <7>These mineshafts are the only place
                <7>in which <6>Fossils <7>are naturally found,
                <7>so jump right in!"""));
        layout.slot(20, ItemStacks.item(Material.DIAMOND_PICKAXE, """
                <a>Step 2: Get Mining!

                <7>When in a <b>Glacite Mineshaft<7>, you have
                <7>a small chance to come across a
                <9>Suspicious Scrap <7>whenever you mine
                <7>an ore."""));
        layout.slot(21, ItemStacks.item(Material.PAPER, """
                <a>Step 3: Find a Suspicious Scrap

                <9>Suspicious Scraps <7>can contain a
                <7>variety of treasures, including
                <6>Fossils<7>. However, you'll need to do
                <7>some excavating!

                <7>Once you've found a <9>Suspicious
                <9>Scrap<7>, bring it to the <6>Fossil
                <6>Research Center<7>!"""));
        layout.slot(23, ItemStacks.item(Material.ARMOR_STAND, """
                <a>Step 4: Start the Fossil Excavator

                <7>The <6>Fossil Excavator <7>can be found
                <7>in the <6>Fossil Research Center<7>, and
                <7>can be used to find out what's
                <7>hidden within your <9>Suspicious Scrap<7>.

                <7>Put your <9>Suspicious Scrap <7>and the
                <a>Chisel <7>you want to use in the
                <7>excavator, and then press start!"""));
        layout.slot(24, ItemStacks.item(Material.BONE, """
                <a>Step 5: Uncover Your Fossil!

                <7>When using the <6>Fossil Excavator<7>, you
                <7>will have a limited number of <a>clicks<7>.
                <7>Unearth as much <6>treasure <7>as
                <7>possible before running out!

                <7>Sometimes, you will find a piece of a
                <7>larger <6>Fossil <7>in the excavator.
                <7>Uncover the entire fossil before
                <7>running out of clicks and <d>you will
                <d>receive it as an item<7>!"""));
        layout.slot(25, ItemStacks.item(Material.WHITE_STAINED_GLASS_PANE, """
                <a>Step 6: Donate or Restore Fossil

                <7>After obtaining a <6>Fossil <7>from the
                <7>excavator, you can donate it to Dr.
                <7>Stone<7>, who will reward you with <5>HOTM
                <5>XP <7>and <3>Mining XP<7>.

                <7>After donating a Fossil, you will also
                <7>unlock a new <6>Dwarven Forge Recipe
                <7>allowing you to turn that fossil into a
                <7>new type of <d>Pet <7>next time you find it!"""));
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }
}
