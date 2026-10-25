package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.ItemShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class VargulPrice implements ShopPrice {
    public static final String CANDY_ID = "DARK_CANDY";

    private final int amount;
    private final ItemShopPrice candy;

    public VargulPrice(int amount) {
        this.amount = amount;
        this.candy = new ItemShopPrice(candyType(), amount);
    }

    public static ItemType candyType() {
        return ItemType.get(CANDY_ID);
    }

    @Override
    public List<Text> getGUIDisplay() {
        return candy.getGUIDisplay();
    }

    @Override
    public String getNamePlural() {
        return "Dark Candies";
    }

    @Override
    public boolean canAfford(SkyBlockPlayer player) {
        return candy.canAfford(player);
    }

    @Override
    public void processPurchase(SkyBlockPlayer player) {
        candy.processPurchase(player);
        VargulProgress.recordSpent(player, amount);
    }

    @Override
    public ShopPrice multiply(int times) {
        return new VargulPrice(amount * times);
    }

    @Override
    public ShopPrice divide(double by) {
        return new VargulPrice(Math.max((int) (amount / by), 1));
    }
}
