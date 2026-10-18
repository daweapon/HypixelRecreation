package dev.daweapon.type.hub.gui;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeMinionData;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CombinedShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.ItemShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.MinionShopPrice;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRequirement;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;

import java.util.List;

// Shop GUI for the Bartender NPC in the Hub, selling drinks and mob-mixin ingredients.
public class GUIShopBartender extends ShopView {
    private static final int MINION_TIER = 12;
    private static final int MINION_COINS = 2_000_000;
    private static final int MINION_SLAYER_LEVEL = 9;

    public GUIShopBartender() {
        super("Bartender", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.CHEAP_COFFEE), 1, new CoinShopPrice(1000)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.TEPID_GREEN_TEA), 1, new CoinShopPrice(1000)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.PULPOUS_ORANGE_JUICE), 1, new CoinShopPrice(1000)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.BITTER_ICE_TEA), 1, new CoinShopPrice(1200)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.KNOCKOFF_COLA), 1, new CoinShopPrice(1500)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.DECENT_COFFEE), 1, new CoinShopPrice(5000)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.ZOMBIE_BRAIN_MIXIN), 1, new CoinShopPrice(150000)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.SPIDER_EGG_MIXIN), 1, new CoinShopPrice(150000)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.WOLF_FUR_MIXIN), 1, new CoinShopPrice(150000)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.END_PORTAL_FUMES), 1, new CoinShopPrice(150000)));
        attachItem(ShopItem.Stackable(new SkyBlockItem(ItemType.GABAGOEY_MIXIN), 1, new CoinShopPrice(150000)));
        sellMinion(ItemType.REVENANT_MINION, ItemType.REVENANT_VISCERA, SlayerType.REVENANT_HORROR);
        sellMinion(ItemType.TARANTULA_MINION, ItemType.TARANTULA_SILK, SlayerType.TARANTULA_BROODFATHER);
    }

    private void sellMinion(ItemType minion, ItemType material, SlayerType slayer) {
        SkyBlockItem item = new SkyBlockItem(minion);
        item.getAttributeHandler().setMinionData(new ItemAttributeMinionData.MinionData(MINION_TIER, 0));
        attachItem(new ShopItem(item, 1, new CombinedShopPrice(List.of(
                new MinionShopPrice(minion, MINION_TIER - 1), new ItemShopPrice(material, 64), new CoinShopPrice(MINION_COINS))),
                false, false)
                .lockedWhen(player -> SlayerRequirement.level(player, slayer) >= MINION_SLAYER_LEVEL
                        ? null : SlayerRequirement.lockedLine(slayer, MINION_SLAYER_LEVEL))
                .withConfirmation());
    }
}
