package net.swofty.type.kuudra.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.kuudra.fight.KuudraFight;
import net.swofty.type.kuudra.fight.KuudraPerks;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraPerkShop;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** "Are you sure?" before a route is locked in for the run (the Burning capture). */
public class GUIConfirmRoute extends StatelessView {
    private final KuudraFight fight;
    private final KuudraPerkShop.Route route;

    public GUIConfirmRoute(KuudraFight fight, KuudraPerkShop.Route route) {
        this.fight = fight;
        this.route = route;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Are you sure?", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(13, ItemStacks.item(Material.OAK_SIGN, """
                <c>Are you sure?
                <7>You cannot change routes once you
                <7>have selected it for this run!

                <7>Route: <a>{}
                <7>Cost: <5>{} Tokens""", route.name(), fight.perks().routeCost()));
        layout.slot(20, ItemStacks.item(Material.GREEN_TERRACOTTA, "<a>Confirm"), (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            KuudraPerks.Result result;
            synchronized (fight.run()) {
                result = fight.perks().chooseRoute(player.getUuid(), route.id());
            }
            switch (result) {
                case BOUGHT -> player.sendMessage("<a>You unlocked the <6>" + route.name() + " <a>route!");
                case NOT_ENOUGH_TOKENS -> player.sendMessage("<c>You don't have enough Tokens!");
                case ROUTE_ALREADY_CHOSEN -> player.sendMessage("<c>You have already chosen a route!");
                default -> {
                }
            }
            c.replace(new GUIKuudraPerkShop(fight));
        });
        layout.slot(24, ItemStacks.item(Material.RED_TERRACOTTA, "<c>Cancel"), (_, c) -> c.replace(new GUIKuudraPerkShop(fight)));
        Components.back(layout, 31, ctx);
    }
}
