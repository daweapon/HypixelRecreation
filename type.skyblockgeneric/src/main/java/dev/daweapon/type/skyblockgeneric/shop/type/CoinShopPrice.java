package dev.daweapon.type.skyblockgeneric.shop.type;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.shop.ShopDiscount;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CoinShopPrice implements ShopPrice {

    double amount;

    @Override
    public List<Text> getGUIDisplay() {
        return List.of(Text.of("<6>{} Coin{}", StringUtility.coinAmount(amount), amount != 1 ? "s" : ""));
    }

    @Override
    public String getNamePlural() {
        return "coins";
    }

    @Override
    public boolean canAfford(SkyBlockPlayer player) {
        return player.getCoins() >= ShopDiscount.apply(player, amount);
    }

    @Override
    public void processPurchase(SkyBlockPlayer player) {
        player.removeCoins(ShopDiscount.apply(player, amount));
    }

    @Override
    public ShopPrice multiply(int amount) {
        return new CoinShopPrice(this.amount * amount);
    }

    @Override
    public ShopPrice divide(double amount) {
        return new CoinShopPrice(this.amount / amount);
    }

}
