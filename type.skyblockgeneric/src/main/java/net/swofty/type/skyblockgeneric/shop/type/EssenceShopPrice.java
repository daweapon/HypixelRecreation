package net.swofty.type.skyblockgeneric.shop.type;

import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.essence.Essences;
import net.swofty.type.skyblockgeneric.shop.ShopPrice;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public record EssenceShopPrice(EssenceType type, long amount) implements ShopPrice {

    @Override
    public List<Text> getGUIDisplay() {
        return List.of(Text.of("<d>{} Essence <8>x{}", type.getDisplayName(), StringUtility.commaify(amount)));
    }

    @Override
    public String getNamePlural() {
        return type.getFullName();
    }

    @Override
    public boolean canAfford(SkyBlockPlayer player) {
        return Essences.get(player, type) >= amount;
    }

    @Override
    public void processPurchase(SkyBlockPlayer player) {
        Essences.take(player, type, amount);
    }

    @Override
    public ShopPrice multiply(int multiplier) {
        return new EssenceShopPrice(type, amount * multiplier);
    }

    @Override
    public ShopPrice divide(double divisor) {
        return new EssenceShopPrice(type, (long) Math.ceil(amount / divisor));
    }
}
