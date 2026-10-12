package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.swofty.type.skyblockgeneric.bazaar.BazaarSales;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

final class BazaarMenuState {
    private volatile BazaarSales.Summary inventory;
    private volatile BazaarSales.Summary sacks;
    private volatile int orders;

    BazaarSales.Summary inventory() {
        return inventory;
    }

    BazaarSales.Summary sacks() {
        return sacks;
    }

    int orders() {
        return orders;
    }

    void refresh(SkyBlockPlayer player) {
        BazaarSales.summarize(player, BazaarSales.Source.INVENTORY).thenAccept(summary -> inventory = summary);
        BazaarSales.summarize(player, BazaarSales.Source.SACKS).thenAccept(summary -> sacks = summary);
        player.getBazaarConnector().getPendingOrders().thenAccept(pending -> orders = pending.size());
    }
}
