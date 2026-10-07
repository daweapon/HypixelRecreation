package net.swofty.type.hub.gui.lucius;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUILucius extends StatelessView {
    private static final String LUCIUS_HEAD = "e4d8951cd209768db5715bdc443621a5452d25f4eb5b6a1c0e7286b1842941f9";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Lucius", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        LuciusTier tier = LuciusTier.next(player);

        if (tier == null) {
            layout.slot(13, ItemStacks.head(LUCIUS_HEAD, """
                    <e>Fully upgraded!
                    <7>You're basically part of the
                    <7>family at this point."""));
            return;
        }

        List<Text> lore = new ArrayList<>();
        lore.add(Text.empty());
        lore.add(Text.of("<7>Cost"));
        lore.add(Text.of("<6>{:,} Coins", (long) tier.getCoinCost()));
        if (tier.getTradedIn() != null) lore.add(itemName(tier.getTradedIn()));
        lore.add(Text.empty());
        if (LuciusTier.darkAuctionPurchases(player) < tier.getRequiredPurchases()) {
            lore.add(Text.of("<c>Requires {} Dark Auction purchases!", tier.getRequiredPurchases()));
        } else {
            lore.add(Text.of("<e>Click to trade!"));
        }

        layout.slot(13,
                ItemStacks.appendLore(PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(tier.getReward()).getItemStack()), lore),
                (click, context) -> purchase(player, tier, context));
    }

    private static Text itemName(ItemType type) {
        return Text.of("<color:{}>{}", type.rarity.getColor(), new SkyBlockItem(type).getDisplayName());
    }

    private static void purchase(SkyBlockPlayer player, LuciusTier tier, ViewContext ctx) {
        if (LuciusTier.next(player) != tier) return;
        if (LuciusTier.darkAuctionPurchases(player) < tier.getRequiredPurchases()) {
            player.sendMessage("<c>You need to purchase {} items from the Dark Auction first!", tier.getRequiredPurchases());
            return;
        }
        if (tier.getTradedIn() != null && player.countItem(tier.getTradedIn()) < 1) {
            player.sendMessage(Text.of("<c>You need a {} <c>in your inventory to upgrade it!", itemName(tier.getTradedIn())));
            return;
        }
        if (player.getCoins() < tier.getCoinCost()) {
            player.sendMessage("<c>You don't have enough coins!");
            return;
        }

        if (tier.getTradedIn() != null) player.takeItem(tier.getTradedIn(), 1);
        player.removeCoins(tier.getCoinCost());
        player.addAndUpdateItem(tier.getReward());
        DatapointInteger purchased = LuciusTier.purchasedTiers(player);
        purchased.setValue(purchased.getValue() + 1);
        player.playSuccessSound();
        ctx.session(DefaultState.class).refresh();
    }
}
