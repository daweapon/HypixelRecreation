package dev.daweapon.type.skyblockgeneric.shop.type;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.trapper.Pelts;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public record PeltShopPrice(int amount) implements ShopPrice {

    @Override
    public List<Text> getGUIDisplay() {
        return List.of(Text.of("<5>{} Pelt{}", StringUtility.commaify(amount), amount == 1 ? "" : "s"));
    }

    @Override
    public String getNamePlural() {
        return "Pelts";
    }

    @Override
    public boolean canAfford(SkyBlockPlayer player) {
        return Pelts.balance(player) >= amount;
    }

    @Override
    public void processPurchase(SkyBlockPlayer player) {
        Pelts.spend(player, amount);
    }

    @Override
    public ShopPrice multiply(int multiplier) {
        return new PeltShopPrice(amount * multiplier);
    }

    @Override
    public ShopPrice divide(double divisor) {
        return new PeltShopPrice((int) Math.ceil(amount / divisor));
    }
}
