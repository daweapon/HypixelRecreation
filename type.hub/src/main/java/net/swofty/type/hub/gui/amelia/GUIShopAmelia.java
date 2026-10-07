package net.swofty.type.hub.gui.amelia;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIShopAmelia extends ShopView {
    public GUIShopAmelia() {
        super(Text.of("Amelia"), DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        for (SocialShopEntry entry : SocialShopEntry.values()) {
            attachItem(new ShopItem(new SkyBlockItem(entry.getItem()), entry.getAmount(),
                    new CoinShopPrice(entry.getCoinCost()), false, false));
        }
    }

    @Override
    protected ItemStack.Builder renderItem(ShopItem item, int index, HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        SocialShopEntry entry = entry(item);
        if (entry.isUnlocked(player)) return super.renderItem(item, index, p);

        ItemStack.Builder stack = PlayerItemUpdater.playerUpdate(player, item.getItem().getItemStackBuilder().build())
                .amount(item.getAmount());
        return ItemStacks.appendLore(stack, List.of(
                Text.empty(),
                Text.of("<7>Cost"),
                Text.of("<6>{:,} Coins", (long) entry.getCoinCost()),
                Text.empty(),
                Text.of("<c>Not unlocked!")));
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, ShopItem item, int index) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SocialShopEntry entry = entry(item);
        if (entry.isUnlocked(player)) {
            super.onItemClick(click, ctx, item, index);
            return;
        }
        player.sendMessage("<c>You need to reach Social Skill {} to unlock this item!", entry.getRequiredSocialLevel());
    }

    private static SocialShopEntry entry(ShopItem item) {
        return SocialShopEntry.of(item.getItem().getAttributeHandler().getPotentialType()).orElseThrow();
    }
}
