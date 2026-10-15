package dev.daweapon.type.deepcaverns.gui;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;

// Shop GUI for Walter, selling Deep Caverns mining supplies like Superboom TNT.
public class GUIShopWalter extends ShopView {
	public GUIShopWalter() {
		super("Walter", SINGLE_SLOT);
	}

	@Override
	public void initializeShopItems() {
		attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.SUPERBOOM_TNT), 1, new CoinShopPrice(2500)));
	}
}
