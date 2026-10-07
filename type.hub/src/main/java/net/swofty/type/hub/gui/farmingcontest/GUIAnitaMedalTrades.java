package net.swofty.type.hub.gui.farmingcontest;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.farmingcontest.ContestBracket;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContestProfile;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContests;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// Anita's medal conversions and medal sales, from the wiki's Anita/UI "Medal Trades" tab.
public class GUIAnitaMedalTrades extends StatelessView {

    private record Conversion(int slot, Material icon, ContestBracket gain, int gained, ContestBracket cost, int costed) {}

    private record Sale(int slot, ContestBracket medal, int tickets) {}

    private static final List<Conversion> CONVERSIONS = List.of(
            new Conversion(10, Material.BRICK, ContestBracket.BRONZE, 2, ContestBracket.SILVER, 1),
            new Conversion(12, Material.IRON_INGOT, ContestBracket.SILVER, 1, ContestBracket.BRONZE, 2),
            new Conversion(14, Material.IRON_INGOT, ContestBracket.SILVER, 4, ContestBracket.GOLD, 1),
            new Conversion(16, Material.GOLD_INGOT, ContestBracket.GOLD, 1, ContestBracket.SILVER, 4));

    private static final List<Sale> SALES = List.of(
            new Sale(30, ContestBracket.BRONZE, 2),
            new Sale(32, ContestBracket.SILVER, 4),
            new Sale(34, ContestBracket.GOLD, 16));

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Medal Trades", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        if (!Components.back(layout, 49, ctx)) Components.close(layout, 49);

        for (Conversion conversion : CONVERSIONS) {
            layout.slot(conversion.slot(), (_, c) -> {
                boolean affordable = FarmingContests.profile((SkyBlockPlayer) c.player()).medals(conversion.cost()) >= conversion.costed();
                return ItemStacks.item(conversion.icon(), String.join("\n",
                        conversion.gain().getColor() + "Medal Conversion",
                        "<7>Gain " + conversion.gain().getColor() + conversion.gained() + " " + name(conversion.gain()),
                        "<7>Costs " + conversion.cost().getColor() + conversion.costed() + " " + name(conversion.cost()),
                        "",
                        affordable ? "<e>Click to trade!" : "<c>Cannot afford trade!"));
            }, (_, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                FarmingContestProfile profile = FarmingContests.profile(player);
                if (profile.medals(conversion.cost()) < conversion.costed()) return;
                profile.addMedals(conversion.cost(), -conversion.costed());
                profile.addMedals(conversion.gain(), conversion.gained());
                FarmingContests.save(player, profile);
                c.session(DefaultState.class).refresh();
            });
        }

        for (Sale sale : SALES) {
            layout.slot(sale.slot(), (_, c) -> {
                boolean affordable = FarmingContests.profile((SkyBlockPlayer) c.player()).medals(sale.medal()) >= 1;
                return ItemStacks.item(Material.NAME_TAG, String.join("\n",
                        sale.medal().getColor() + "Medal to Tickets",
                        "<7>Sell " + sale.medal().getColor() + "1 " + name(sale.medal()) + " medal",
                        "<7>for <a>" + sale.tickets() + " Jacob's Ticket",
                        "",
                        affordable ? "<e>Click to sell!" : "<c>Not enough medals!"));
            }, (_, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                FarmingContestProfile profile = FarmingContests.profile(player);
                if (profile.medals(sale.medal()) < 1) return;
                profile.addMedals(sale.medal(), -1);
                FarmingContests.save(player, profile);
                player.addAndUpdateItem(new SkyBlockItem(ItemType.JACOBS_TICKET, sale.tickets()));
                c.session(DefaultState.class).refresh();
            });
        }
    }

    private static String name(ContestBracket medal) {
        String name = medal.getDisplayName();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}
