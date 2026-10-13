package net.swofty.type.skyblockgeneric.gui.inventories.essence;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CombinedShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.EssenceShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.ItemShopPrice;

import java.util.List;

public class GUIShopGemma extends ShopView {
    public static final long PERFECTLY_CUT_DIAMOND_ESSENCE = 1_000;
    public static final int PERFECTLY_CUT_DIAMOND_REFINED = 2;
    public static final long MINING_OFF_CAMERA_ESSENCE = 2_500;

    public GUIShopGemma() {
        super("Gemma", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.PERFECTLY_CUT_DIAMOND), 1, new CombinedShopPrice(List.of(
                new EssenceShopPrice(EssenceType.DIAMOND, PERFECTLY_CUT_DIAMOND_ESSENCE),
                new ItemShopPrice(ItemType.REFINED_DIAMOND, PERFECTLY_CUT_DIAMOND_REFINED)))));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.MINING_OFF_CAMERA), 1,
                new EssenceShopPrice(EssenceType.DIAMOND, MINING_OFF_CAMERA_ESSENCE)));
    }
}
