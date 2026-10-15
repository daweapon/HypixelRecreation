package dev.daweapon.type.skyblockgeneric.shop.type;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/** A price in North Stars, the currency Einary trades in at Jerry's Workshop. */
public record NorthStarShopPrice(int amount) implements ShopPrice {

    @Override
    public List<Text> getGUIDisplay() {
        return List.of(Text.of("<d>{} North Star{}", StringUtility.commaify(amount), amount != 1 ? "s" : ""));
    }

    @Override
    public String getNamePlural() {
        return "North Stars";
    }

    @Override
    public boolean canAfford(SkyBlockPlayer player) {
        return stars(player).getValue() >= amount;
    }

    @Override
    public void processPurchase(SkyBlockPlayer player) {
        DatapointInteger stars = stars(player);
        stars.setValue(stars.getValue() - amount);
    }

    @Override
    public ShopPrice multiply(int multiplier) {
        return new NorthStarShopPrice(amount * multiplier);
    }

    @Override
    public ShopPrice divide(double divisor) {
        return new NorthStarShopPrice((int) Math.ceil(amount / divisor));
    }

    private static DatapointInteger stars(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.NORTH_STARS, DatapointInteger.class);
    }
}
