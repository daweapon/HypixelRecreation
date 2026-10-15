package dev.daweapon.type.skyblockgeneric.shop.type;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.feast.FeastService;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public record KernelShopPrice(int amount) implements ShopPrice {

    @Override
    public List<Text> getGUIDisplay() {
        return List.of(Text.of("<e>{} Kernels", StringUtility.commaify(amount)));
    }

    @Override
    public String getNamePlural() {
        return "Kernels";
    }

    @Override
    public boolean canAfford(SkyBlockPlayer player) {
        return FeastService.data(Gardens.profile(player)).getKernels() >= amount;
    }

    @Override
    public void processPurchase(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        var data = FeastService.data(profile);
        data.setKernels(data.getKernels() - amount);
        Gardens.save(player, profile);
    }

    @Override
    public ShopPrice multiply(int multiplier) {
        return new KernelShopPrice(amount * multiplier);
    }

    @Override
    public ShopPrice divide(double divisor) {
        return new KernelShopPrice((int) Math.ceil(amount / divisor));
    }
}
