package net.swofty.type.jerrysworkshop.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CombinedShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.ItemShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.NorthStarShopPrice;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

// Einary's Emporium, priced in North Stars. The Gift Talismans are only sold in real-life December,
// as on Hypixel. The Gold Gift Talisman (needs a Golden Gift), Reindeer Pet, Snow equipment,
// tier XII Ice and Snow Minions, Magic Top Hat, Winter Fragments and enchanted books he also sells
// are not on this server yet.
public class GUIEinaryEmporium extends ShopView {

    public GUIEinaryEmporium() {
        super("Einary's Emporium", DEFAULT);
    }

    public static boolean isDecember() {
        return LocalDate.now().getMonth() == Month.DECEMBER;
    }

    @Override
    public void initializeShopItems() {
        if (!isDecember()) return;
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.WHITE_GIFT_TALISMAN), 1, new NorthStarShopPrice(20)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.GREEN_GIFT_TALISMAN), 1, upgrade(ItemType.WHITE_GIFT_TALISMAN, 40)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.BLUE_GIFT_TALISMAN), 1, upgrade(ItemType.GREEN_GIFT_TALISMAN, 60)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.PURPLE_GIFT_TALISMAN), 1, upgrade(ItemType.BLUE_GIFT_TALISMAN, 100)));
    }

    private static CombinedShopPrice upgrade(ItemType previousTier, int northStars) {
        return new CombinedShopPrice(List.of(new ItemShopPrice(previousTier, 1), new NorthStarShopPrice(northStars)));
    }
}
