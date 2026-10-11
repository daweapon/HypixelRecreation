package net.swofty.type.dwarvenmines.gui;

import net.minestom.server.inventory.InventoryType;
import net.swofty.type.dwarvenmines.rail.RailStop;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.glacite.RailroadPass;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;

public class GUIMinecart extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 21, 23};

    private final RailStop origin;

    public GUIMinecart(RailStop origin) {
        this.origin = origin;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Minecart Rail Map", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        RailStop[] stops = RailStop.values();
        for (int i = 0; i < stops.length; i++) {
            RailStop stop = stops[i];
            layout.slot(SLOTS[i], (_, _) -> ItemStacks.item(stop.getIcon(), "<a>" + stop.getDisplayName() + "\n" + describe(player, stop)),
                    (_, c) -> travel(player, stop));
        }
        Components.close(layout, 31);
    }

    private String describe(SkyBlockPlayer player, RailStop stop) {
        if (stop == origin) return "<7>You are here.";
        if (stop.isRequiresPass() && !RailroadPass.isUnlocked(player)) {
            return "<7>Requires a <c>Secret Railroad Pass<7>.\n\n<e>Click to show your pass!";
        }
        return "<7>Fare: <6>" + String.format(Locale.US, "%,d", stop.fareFrom(origin)) + " coins\n\n<e>Click to ride!";
    }

    private void travel(SkyBlockPlayer player, RailStop stop) {
        if (stop == origin) return;
        if (stop.isRequiresPass() && !RailroadPass.showPass(player)) {
            player.sendMessage("<c>You need a <c>Secret Railroad Pass <c>to ride to the " + stop.getDisplayName() + "!");
            return;
        }
        long fare = stop.fareFrom(origin);
        if (player.getCoins() < fare) {
            player.sendMessage("<c>You don't have enough coins to pay the fare!");
            return;
        }
        player.removeCoins(fare);
        player.closeInventory();
        player.sendMessage("<e>Hop on in, hold tight and enjoy the ride to the <b>" + stop.getDisplayName() + "<e>!");
        player.teleport(stop.getPosition());
    }
}
