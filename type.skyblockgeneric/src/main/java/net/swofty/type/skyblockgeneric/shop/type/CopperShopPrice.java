package net.swofty.type.skyblockgeneric.shop.type;

import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.shop.ShopPrice;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public record CopperShopPrice(int amount) implements ShopPrice {

    @Override
    public List<Text> getGUIDisplay() {
        return List.of(Text.of("<c>{} Copper", StringUtility.commaify(amount)));
    }

    @Override
    public String getNamePlural() {
        return "Copper";
    }

    @Override
    public boolean canAfford(SkyBlockPlayer player) {
        return Gardens.profile(player).getCopper() >= amount;
    }

    @Override
    public void processPurchase(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        profile.spendCopper(amount);
        Gardens.save(player, profile);
    }

    @Override
    public ShopPrice multiply(int multiplier) {
        return new CopperShopPrice(amount * multiplier);
    }

    @Override
    public ShopPrice divide(double divisor) {
        return new CopperShopPrice((int) Math.ceil(amount / divisor));
    }
}
