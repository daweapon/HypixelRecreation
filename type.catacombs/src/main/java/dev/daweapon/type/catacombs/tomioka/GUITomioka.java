package dev.daweapon.type.catacombs.tomioka;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;

/** Tomioka's shop as captured in M7 run 1: higher-tier enchanted books for coins; any purchase earns Rough Deal. */
public final class GUITomioka extends ShopView {
    static final String ROUGH_DEAL_TRIGGER = "skyblock.rough_deal_trigger";

    private final TomiokaConfig config;

    public GUITomioka(TomiokaConfig config) {
        super("Tomioka", DEFAULT);
        this.config = config;
    }

    @Override
    public void initializeShopItems() {
        for (TomiokaConfig.Book book : config.stock()) {
            SkyBlockItem item = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
            item.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(book.enchantment(), book.level()));
            attachItem(ShopItem.Single(item, 1, new CoinShopPrice(book.coins()))
                    .onPurchase(player -> player.getAchievementHandler().addProgressByTrigger(ROUGH_DEAL_TRIGGER, 1)));
        }
    }
}
