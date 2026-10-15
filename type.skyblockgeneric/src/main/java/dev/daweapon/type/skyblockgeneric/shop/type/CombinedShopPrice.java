package dev.daweapon.type.skyblockgeneric.shop.type;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CombinedShopPrice implements ShopPrice {

    List<ShopPrice> priceElements;

    @Override
    public List<Text> getGUIDisplay() {
        List<Text> result = new ArrayList<>();
        priceElements.forEach(price -> result.addAll(price.getGUIDisplay()));
        return result;
    }

    @Override
    public String getNamePlural() {
        return String.join(" or ", priceElements.stream()
                .map(ShopPrice::getNamePlural)
                .toList());
    }

    @Override
    public boolean canAfford(SkyBlockPlayer player) {
        return priceElements.stream()
                .allMatch(price -> price.canAfford(player));
    }

    @Override
    public void processPurchase(SkyBlockPlayer player) {
        priceElements.forEach(price -> price.processPurchase(player));
    }

    @Override
    public ShopPrice multiply(int amount) {
        return new CombinedShopPrice(priceElements.stream()
                        .map(price -> price.multiply(amount))
                        .toList());
    }

    @Override
    public ShopPrice divide(double amount) {
        return new CombinedShopPrice(priceElements.stream()
                        .map(price -> price.divide(amount))
                        .toList());
    }

}
