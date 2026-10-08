package net.swofty.type.skyblockgeneric.shop.type;

import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.shop.ShopPrice;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
