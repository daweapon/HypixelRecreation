package dev.daweapon.type.skyblockgeneric.shop.type;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public record BitsShopPrice(int amount) implements ShopPrice {

    @Override
    public List<Text> getGUIDisplay() {
        return List.of(Text.of("<b>{} Bits", StringUtility.commaify(amount)));
    }

    @Override
    public String getNamePlural() {
        return "bits";
    }

    @Override
    public boolean canAfford(SkyBlockPlayer player) {
        return player.getBits() >= amount;
    }

    @Override
    public void processPurchase(SkyBlockPlayer player) {
        player.removeBits(amount);
    }

    @Override
    public ShopPrice multiply(int multiplier) {
        return new BitsShopPrice(amount * multiplier);
    }

    @Override
    public ShopPrice divide(double divisor) {
        return new BitsShopPrice((int) Math.ceil(amount / divisor));
    }
}
