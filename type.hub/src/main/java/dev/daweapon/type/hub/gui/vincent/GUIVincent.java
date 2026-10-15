package dev.daweapon.type.hub.gui.vincent;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.hub.util.UnreleasedContent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;

import java.util.List;

public class GUIVincent extends StatelessView {
    private static final int[] MASTERPIECE_SLOTS = {29, 31, 33};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Dyes", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);

        layout.slot(13, ItemStacks.item(Material.OAK_SIGN, """
                <6>Vincent's Masterpiece
                <7>Every <d>SkyBlock Year<7>, <b>Vincent<7> picks
                <c>d<6>y<e>e<a>s <7>that he will use to paint a
                <7>masterpiece!

                <7>The <a>3 <7>chosen dyes will have their
                <7>drop chances increased for the
                <7>duration of the year."""));

        int year = SkyBlockCalendar.getYear();
        List<VincentDye> chosen = VincentDyes.masterpiece(year);
        for (int i = 0; i < chosen.size(); i++) {
            VincentDye dye = chosen.get(i);
            Text boost = Text.of("<e>This dye is <a>{}x<e> as common during\n<d>SkyBlock Year <5>{}<e>!",
                    VincentDyes.MASTERPIECE_MULTIPLIERS[i], year);
            layout.slot(MASTERPIECE_SLOTS[i], dye.display(boost));
        }

        UnreleasedContent.DYES.slot(layout, 48, Material.WATER_BUCKET, """
                <6>Bucket of Dye
                <7>Found a <c>d<6>y<e>e <7>on your travels?
                <b>Vincent<7> will exchange it for an
                <d>accessory<7>, the color of which
                <7>depends upon the dye donated.""");
        layout.slot(50, ItemStacks.item(Material.ORANGE_DYE, """
                <6>Dye Guide
                <8>All Dyes

                <b>Vincent <7>knows all. Click to see a list
                <7>of all <c>d<6>y<e>e<a>s <7>currently in the game,
                <7>how they are obtained, and their
                <7>chances of dropping.

                <e>Click to view!"""), (click, c) -> c.push(new GUIDyeGuide(), GUIDyeGuide.initialState()));
    }
}
