package dev.daweapon.type.hub.gui.rosetta;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;

public class GUIMercenaryArmor extends ShopView {
    public GUIMercenaryArmor() {
        super("Mercenary Armor", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.MERCENARY_AXE), 1, new CoinShopPrice(30000)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.MERCENARY_HELMET), 1, new CoinShopPrice(35000)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.MERCENARY_CHESTPLATE), 1, new CoinShopPrice(70000)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.MERCENARY_LEGGINGS), 1, new CoinShopPrice(45000)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.MERCENARY_BOOTS), 1, new CoinShopPrice(30000)));
    }
}
