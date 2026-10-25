package dev.daweapon.type.skyblockgeneric.gui.inventories.spidersden;

import net.minestom.server.inventory.InventoryType;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.spidersden.ArachneCrystalTrade;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIShaggy extends StatelessView {
    private static final String CRYSTAL_TEXTURE = "aa11ba288bd6aa7d49360b03bdc618189154c44e588516ea29caff22946cee00";

    private final Runnable onCrafted;

    public GUIShaggy(Runnable onCrafted) {
        this.onCrafted = onCrafted;
    }

    public GUIShaggy() {
        this(() -> {
        });
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Shaggy", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        boolean affordable = ArachneCrystalTrade.canAfford(player);
        StringBuilder cost = new StringBuilder("\n<7>Cost\n");
        ArachneCrystalTrade.COST.forEach((item, amount) -> cost.append("<a>")
                .append(new SkyBlockItem(item).getDisplayName()).append(" <8>x").append(amount).append('\n'));
        layout.slot(13, ItemStacks.head(CRYSTAL_TEXTURE, "<5>Arachne Crystal" + cost
                + "\n" + (affordable ? "<e>Click to trade!" : "<c>You don't have the items!")), (_, c) -> {
            if (!ArachneCrystalTrade.trade(player)) {
                player.sendMessage("<c>You don't have the items Shaggy needs!");
                return;
            }
            c.pop();
            onCrafted.run();
        });
    }
}
