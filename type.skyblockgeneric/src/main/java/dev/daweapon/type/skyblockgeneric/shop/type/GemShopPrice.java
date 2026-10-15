package dev.daweapon.type.skyblockgeneric.shop.type;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public record GemShopPrice(int amount) implements ShopPrice {

    @Override
    public List<Text> getGUIDisplay() {
        return List.of(Text.of("<a>{} Gems", StringUtility.commaify(amount)));
    }

    @Override
    public String getNamePlural() {
        return "gems";
    }

    @Override
    public boolean canAfford(SkyBlockPlayer player) {
        return player.getGems() >= amount;
    }

    @Override
    public void processPurchase(SkyBlockPlayer player) {
        player.removeGems(amount);
    }

    @Override
    public ShopPrice multiply(int multiplier) {
        return new GemShopPrice(amount * multiplier);
    }

    @Override
    public ShopPrice divide(double divisor) {
        return new GemShopPrice((int) Math.ceil(amount / divisor));
    }
}
