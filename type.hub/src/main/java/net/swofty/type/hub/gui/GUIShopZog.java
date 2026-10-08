package net.swofty.type.hub.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;

// Shop GUI for the Zog NPC in the Hub, selling skill XP boosts and pet item upgrades.
public class GUIShopZog extends ShopView {
    public GUIShopZog() {
        super("Zog", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        sell(ItemType.PET_ITEM_COMBAT_SKILL_BOOST_COMMON, 60_000);
        sell(ItemType.PET_ITEM_FORAGING_SKILL_BOOST_COMMON, 60_000);
        sell(ItemType.PET_ITEM_MINING_SKILL_BOOST_COMMON, 60_000);
        sell(ItemType.PET_ITEM_MINING_SKILL_BOOST_UNCOMMON, 250_000);
        sell(ItemType.PET_ITEM_FARMING_SKILL_BOOST_COMMON, 60_000);
        sell(ItemType.PET_ITEM_FARMING_SKILL_BOOST_RARE, 500_000);
        sell(ItemType.PET_ITEM_FISHING_SKILL_BOOST_COMMON, 60_000);
        sell(ItemType.PET_ITEM_ALL_SKILLS_BOOST_COMMON, 50_000);
        sell(ItemType.PET_ITEM_BIG_TEETH_COMMON, 750_000);
        sell(ItemType.PET_ITEM_IRON_CLAWS_COMMON, 750_000);
        sell(ItemType.PET_ITEM_HARDENED_SCALES_UNCOMMON, 1_000_000);
        sell(ItemType.PET_ITEM_SHARPENED_CLAWS_UNCOMMON, 1_000_000);
        sell(ItemType.PET_ITEM_BUBBLEGUM, 5_000_000);
    }

    private void sell(ItemType item, int coins) {
        attachItem(ShopItem.Single(new SkyBlockItem(item), 1, new CoinShopPrice(coins)));
    }
}
