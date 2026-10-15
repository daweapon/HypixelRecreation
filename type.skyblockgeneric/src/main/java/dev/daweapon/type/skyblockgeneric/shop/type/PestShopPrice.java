package dev.daweapon.type.skyblockgeneric.shop.type;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public record PestShopPrice(int amount) implements ShopPrice {

    @Override
    public List<Text> getGUIDisplay() {
        return List.of(Text.of("<2>{} Pests", StringUtility.commaify(amount)));
    }

    @Override
    public String getNamePlural() {
        return "Pests";
    }

    @Override
    public boolean canAfford(SkyBlockPlayer player) {
        return Gardens.profile(player).pests().getStored() >= amount;
    }

    @Override
    public void processPurchase(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        profile.pests().setStored(profile.pests().getStored() - amount);
        Gardens.save(player, profile);
    }

    @Override
    public ShopPrice multiply(int multiplier) {
        return new PestShopPrice(amount * multiplier);
    }

    @Override
    public ShopPrice divide(double divisor) {
        return new PestShopPrice((int) Math.ceil(amount / divisor));
    }
}
