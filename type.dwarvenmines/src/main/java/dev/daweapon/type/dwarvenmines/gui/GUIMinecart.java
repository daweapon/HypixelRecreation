package dev.daweapon.type.dwarvenmines.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.dwarvenmines.rail.RailStop;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.glacite.RailroadPass;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GUIMinecart extends StatelessView {
    private static final Map<RailStop, Integer> SLOTS = Map.of(
            RailStop.DWARVEN_VILLAGE, 20,
            RailStop.HANGING_COURT, 21,
            RailStop.GREAT_ICE_WALL, 22,
            RailStop.FAR_RESERVE, 23,
            RailStop.UPPER_MINES, 24,
            RailStop.FORGE_BASIN, 29,
            RailStop.ROYAL_MINES, 30,
            RailStop.ABANDONED_QUARRY, 32,
            RailStop.GLACITE_TUNNELS, 33);
    private static final Map<RailStop, String> LABELS = Map.of(
            RailStop.ROYAL_MINES, "Lower Royal Mines",
            RailStop.GLACITE_TUNNELS, "Glacite Tunnels - Dwarven Base Camp");
    private static final Map<RailStop, String> DESCRIPTIONS = Map.of(
            RailStop.DWARVEN_VILLAGE, "The hustling and bustling community\nhub of the Dwarven Mines!",
            RailStop.HANGING_COURT, "The most majestic and well maintained\ngarden in the whole of the Dwarven\nMines neighbouring the Royal Palace.",
            RailStop.GREAT_ICE_WALL, "The great barrier between the\nDwarven Mines and that which is\nFrozen.",
            RailStop.FAR_RESERVE, "Deep rich reserves of Mithril and\nGold bordering enemy territory.",
            RailStop.UPPER_MINES, "Rich deposits of ore precariously\npositioned near the top of the\nDwarven Mines.",
            RailStop.FORGE_BASIN, "The fiery depths of the Dwarven\nMines playing host to the magnificent\nforge!",
            RailStop.ROYAL_MINES, "To the depths of the mighty Dwarven\nMines!",
            RailStop.ABANDONED_QUARRY, "A quarry once bustling with riches\nand promise. Now left to the depths.",
            RailStop.GLACITE_TUNNELS, "Find what lies beyond the Great Ice\nWall.");

    private static final String[] BOARDING = {
            "Hop on in and I'll get you going!",
            "Hop in and we'll be off!",
            "See that empty, weak metal box? Good! That's your cart, hop in!",
            "Hold tight, it's going to be a bump ride!",
            "Thanks for choosing Dwarven Mine Co. - we operate 24/7 across the whole mine!",
            "Now, please climb aboard before your cart leaves without you.",
            "Let's a go!",
            "Last guy who was in this cart didn't feel too well so watch out he might have left a surprise in there."
    };
    private static final Map<RailStop, String> REMARKS = Map.of(
            RailStop.HANGING_COURT, "Off to the court? I would tell you to talk to the King but I don't know who he is anymore...",
            RailStop.FORGE_BASIN, "Did you know that the Forge is so big it has to have multiple forging stations.",
            RailStop.ABANDONED_QUARRY, "Fell into a state of disrepair following an accident."
    );

    private final RailStop origin;

    public GUIMinecart(RailStop origin) {
        this.origin = origin;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Dwarven Mines Rail System", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        layout.slot(4, ItemStacks.item(Material.MINECART, "<6>Dwarven Mines Rail System\n<7>Your one stop shop for cheap, easy and fast\n<7>travel in the deepest reaches of the Dwarven\n<7>Mines.\n\n<3>Stations:\n"
                + "<7> > <3>Dwarven Village\n<7> > <3>Hanging Court\n<7> > <3>Great Ice Wall\n<7> > <3>Far Reserve\n<7> > <3>Upper Mines\n<7> > <3>Forge Basin\n"
                + "<7> > <3>Lower Royal Mines\n<7> > <2>Abandoned Quarry\n<7> > <3>Glacite Tunnels - Dwarven Base Camp"));
        for (RailStop stop : RailStop.values()) {
            String color = stop == RailStop.ABANDONED_QUARRY ? "<2>" : "<3>";
            String label = LABELS.getOrDefault(stop, stop.getDisplayName());
            long distance = Math.round(origin.getPosition().distance(stop.getPosition()));
            layout.slot(SLOTS.get(stop), (_, _) -> ItemStacks.item(stop.getIcon(), color + label + " <8>(<6>" + distance + "m<8>)\n<7>"
                    + DESCRIPTIONS.get(stop) + "\n\n" + describe(player, stop)), (_, c) -> travel(player, stop));
        }
        layout.slot(49, ItemStacks.item(Material.BARRIER, "<c>Actually, I'll walk..."), (_, c) -> c.player().closeInventory());
    }

    private String describe(SkyBlockPlayer player, RailStop stop) {
        if (stop == origin) return "<a>You are here!";
        if (stop.isRequiresPass() && !RailroadPass.isUnlocked(player)) {
            return "<7>Requires a <c>Secret Railroad Pass<7>.\n\n<e>Click to show your pass!";
        }
        return "<7>Cost: <6>" + String.format(Locale.US, "%,d", stop.fareFrom(origin)) + " Coins\n\n<e>Click to travel!";
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
        String speaker = origin == RailStop.DWARVEN_VILLAGE ? "Station Master" : "Ticket Master";
        player.sendMessage("<e>[NPC] <b>" + speaker + "<f>: " + BOARDING[ThreadLocalRandom.current().nextInt(BOARDING.length)]);
        String remark = REMARKS.get(stop);
        if (remark != null) player.sendMessage("<e>[NPC] <b>" + speaker + "<f>: " + remark);
        player.teleport(stop.getPosition());
        player.sendMessage("<e>[NPC] <b>" + speaker + "<f>: Welcome to the " + (stop == RailStop.GLACITE_TUNNELS ? "Dwarven Base Camp" : stop.getDisplayName()) + "!");
    }
}
