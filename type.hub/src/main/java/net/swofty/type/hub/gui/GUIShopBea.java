package net.swofty.type.hub.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;

import java.util.List;

// Shop GUI for Bea in the Hub: a Common Bee pet and the Honey Dippers Kat uses to raise its rarity.
public class GUIShopBea extends ShopView {
    public GUIShopBea() {
        super("Bea", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachBeePet();
        sell(ItemType.SMALL_HONEY_DIPPER, 500);
        sell(ItemType.MEDIUM_HONEY_DIPPER, 5_000);
        sell(ItemType.LARGE_HONEY_DIPPER, 50_000);
        sell(ItemType.GIANT_HONEY_DIPPER, 500_000);
    }

    private void sell(ItemType item, int coins) {
        attachItem(ShopItem.Single(new SkyBlockItem(item), 1, new CoinShopPrice(coins)));
    }

    private void attachBeePet() {
        SkyBlockItem beeItem = new SkyBlockItem(ItemType.BEE_PET);
        beeItem.getAttributeHandler().setRarity(Rarity.COMMON);
        ShopItem bee = ShopItem.Single(beeItem, 1, new CoinShopPrice(50));

        SkyBlockItem beeDisplayItem = new SkyBlockItem(ItemType.BEE_PET);
        beeDisplayItem.getAttributeHandler().setRarity(Rarity.COMMON);
        PetLevels.setLevel(beeDisplayItem, PetLevels.maxLevel(beeDisplayItem));

        List<Text> lore = beeDisplayItem.getLoreText();
        lore.add(Text.literal(" "));
        lore.add(Text.of("<c>This is a preview of Lvl 100."));
        lore.add(Text.of("<c>New pets are lowest level!"));

        bee.setLore(lore);
        bee.setDisplayName(beeDisplayItem.getDisplayName());
        attachItem(bee);
    }
}
