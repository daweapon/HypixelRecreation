package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CombinedShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GUIVargulShop extends ShopView {
    private static final int EMBLEM_SLOT = 50;
    private static final int BAR_LENGTH = 20;
    private static final int EXPIRED_PUMPKIN_AMOUNT = 30;
    private static final long DRAIN_COINS = 10_000_000;

    public GUIVargulShop() {
        super("Vargul", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Stackable(item("SCARE_FRAGMENT"), 1, new VargulPrice(10)));
        attachItem(ShopItem.Single(item("EXPIRED_PUMPKIN"), EXPIRED_PUMPKIN_AMOUNT, new VargulPrice(1)));
        attachItem(ShopItem.Single(item("BLACK_WOOLEN_YARN"), 1, new VargulPrice(100)));
        attachItem(ShopItem.Single(item("SLEEPY_HOLLOW"), 1, new VargulPrice(150)));
        attachItem(ShopItem.Stackable(item("SPOOKY_TREE"), 1, new VargulPrice(170)));
        attachItem(ShopItem.Single(drainBook(), 1, new CombinedShopPrice(List.of(
                new VargulPrice(200), new CoinShopPrice(DRAIN_COINS)))).withConfirmation());
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        super.layoutCustom(layout, state, ctx);
        layout.slot(EMBLEM_SLOT, (_, c) -> emblem((SkyBlockPlayer) c.player()), (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            if (VargulProgress.hasEmblem(player)) player.sendMessage("<a>You already have the Great Spook Emblem!");
            else player.sendMessage("<c>You haven't spent enough Dark Candies!");
        });
    }

    private static SkyBlockItem item(String id) {
        return new SkyBlockItem(ItemType.get(id));
    }

    private static SkyBlockItem drainBook() {
        SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
        book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.DRAIN, 5));
        return book;
    }

    private static ItemStack.Builder emblem(SkyBlockPlayer player) {
        int spent = VargulProgress.spent(player);
        int capped = Math.min(spent, VargulProgress.EMBLEM_CANDY);
        int filled = capped * BAR_LENGTH / VargulProgress.EMBLEM_CANDY;
        ItemStack.Builder stack = new SkyBlockItem(VargulPrice.candyType()).getItemStackBuilder();
        ItemStacks.name(stack, Text.of("<a>Great Spook Emblem <5><l>࿇"));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Obtain this <o>spooky <7>Emblem by"));
        lore.add(Text.of("<7>spending <5>{} Dark Candies <7>in this", VargulProgress.EMBLEM_CANDY));
        lore.add(Text.of("<7>shop!"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Progress: <d>{}%", capped * 100 / VargulProgress.EMBLEM_CANDY));
        lore.add(Text.of("<d><l>" + "-".repeat(filled) + "<f><l>" + "-".repeat(BAR_LENGTH - filled)
                + " <d>" + capped + "<5>/<d>" + VargulProgress.EMBLEM_CANDY));
        return ItemStacks.lore(stack, lore);
    }
}
