package net.swofty.type.hub.gui.carnival;

import net.minestom.server.inventory.InventoryType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.carnival.CarnivalPerkEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Doug's Carnival Perks menu: one entry per event, from the wiki's Doug/UI subpage.
public class GUICarnivalPerks extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 14, 15, 16};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Carnival Perks", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        Components.back(layout, 30, ctx);
        for (CarnivalPerkEvent event : CarnivalPerkEvent.values()) {
            layout.slot(SLOTS[event.ordinal()], (_, _) -> ItemStacks.item(event.getIcon(),
                    event.colored() + "\n"
                            + "<7>Exchange your <e>Carnival Tokens <7>for\n"
                            + event.colored() + " <7>perks!\n\n"
                            + "<e>Click to view!"), (_, c) -> c.push(new GUICarnivalEventPerks(event)));
        }
        layout.slot(32, (_, c) -> CarnivalTokensDisplay.of((SkyBlockPlayer) c.player()));
    }
}
