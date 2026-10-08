package net.swofty.type.hub.gui;

import net.swofty.type.skyblockgeneric.accessory.AccessoryPower;
import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointBoolean;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIErihann extends StatelessView {
    private static final double IMBUE_COST = 200_000;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Erihann", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        layout.slot(13,
                ItemStacks.text(
                        PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(ItemType.RIFT_PRISM).getItemStack()),
                        """
                                <9>Imbue Rift Prism
                                <7>Consume the <9>Rift Prism <7>to permanently enjoy
                                <7>its <a>effect <7>and gain <6>+{} Accessory Power<7>.

                                <7>Cost
                                <6>200,000 Coins

                                <e>Click to imbue!""", AccessoryPower.RIFT_PRISM_IMBUED),
                (click, context) -> imbue(player));
    }

    private static void imbue(SkyBlockPlayer player) {
        DatapointBoolean imbued = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.RIFT_PRISM_IMBUED, DatapointBoolean.class);
        if (imbued.getValue()) return;
        if (player.countItem(ItemType.RIFT_PRISM) < 1) {
            player.sendMessage("<c>You don't have a Rift Prism in your inventory!");
            return;
        }
        if (player.getCoins() < IMBUE_COST) {
            player.sendMessage("<c>You don't have enough coins!");
            return;
        }

        player.takeItem(ItemType.RIFT_PRISM, 1);
        player.removeCoins(IMBUE_COST);
        imbued.setValue(true);
        player.closeInventory();
        player.playSuccessSound();
        player.sendMessage("<9>Erihann <e>imbued your <9>Rift Prism <e>right into your spirit!");
    }
}
