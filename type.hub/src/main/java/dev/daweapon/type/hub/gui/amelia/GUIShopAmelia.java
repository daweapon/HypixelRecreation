package dev.daweapon.type.hub.gui.amelia;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
