package dev.daweapon.type.skyblockgeneric.shop.type;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemShopPrice implements ShopPrice {
    ItemType type;
    int amount;

    @Override
    public List<Text> getGUIDisplay() {
        return List.of(Text.of("<6>" + Text.colorTag(type.rarity.getColor()) + type.getDisplayName() + " <8>x" + amount));
    }

    @Override
    public String getNamePlural() {
        return "configuration/items";
    }

    @Override
    public boolean canAfford(SkyBlockPlayer player) {
        return player.getAmountAnywhere(type) >= amount;
    }

    @Override
    public void processPurchase(SkyBlockPlayer player) {
        player.removeItemFromPlayer(type, amount);
    }

    @Override
    public ShopPrice multiply(int amount) {
        return new ItemShopPrice(type, this.amount * amount);
    }

    @Override
    public ShopPrice divide(double amount) {
        return new ItemShopPrice(type, Math.max((int) (this.amount / amount), 1));
    }

}
