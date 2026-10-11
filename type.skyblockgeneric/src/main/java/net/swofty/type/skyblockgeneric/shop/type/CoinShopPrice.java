package net.swofty.type.skyblockgeneric.shop.type;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.shop.ShopDiscount;
import net.swofty.type.skyblockgeneric.shop.ShopPrice;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
