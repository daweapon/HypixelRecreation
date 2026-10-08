package net.swofty.type.crimsonisle.gui.faction;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeMinionData;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.faction.ReputationTier;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.CombinedShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.ItemShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.MinionShopPrice;

import java.util.List;

/**
 * Hilda's and Marthos's Tier XII minions (wiki Hilda/UI, Marthos/UI): each costs the Tier XI minion,
 * a material and 2,000,000 coins. The faction's own minion also needs Hero (12,000) reputation.
 */
public class GUIMinionMerchant extends ShopView {
    private static final int TIER = 12;
    private static final int COINS = 2_000_000;
    private final CrimsonFaction faction;

    public GUIMinionMerchant(String title, CrimsonFaction faction) {
        super(title, DEFAULT);
        this.faction = faction;
    }

    @Override
    public void initializeShopItems() {
        sell(ItemType.GHAST_MINION, ItemType.SILVER_FANG, 1024);
        sell(ItemType.QUARTZ_MINION, ItemType.ENCHANTED_QUARTZ_BLOCK, 16);
        sell(ItemType.GLOWSTONE_MINION, ItemType.ENCHANTED_GLOWSTONE, 32);
        sell(ItemType.BLAZE_MINION, ItemType.ENCHANTED_BLAZE_ROD, 16);
        sell(ItemType.MAGMA_CUBE_MINION, ItemType.ENCHANTED_MAGMA_CREAM, 1024);
        ShopItem factionMinion = faction == CrimsonFaction.MAGE
                ? sell(ItemType.MYCELIUM_MINION, ItemType.ENCHANTED_MYCELIUM_CUBE, 32)
                : sell(ItemType.RED_SAND_MINION, ItemType.ENCHANTED_RED_SAND_CUBE, 32);
        int required = ReputationTier.HERO.getRequiredReputation();
        factionMinion.lockedWhen(player -> CrimsonReputationService.get(player, faction) >= required ? null
                : "<4>❣ <c>Requires <c>12,000 " + faction.getDisplayName() + " Reputation.");
    }

    private ShopItem sell(ItemType minion, ItemType material, int amount) {
        SkyBlockItem item = new SkyBlockItem(minion);
        item.getAttributeHandler().setMinionData(new ItemAttributeMinionData.MinionData(TIER, 0));
        ShopItem shopItem = new ShopItem(item, 1, new CombinedShopPrice(List.of(
                new MinionShopPrice(minion, TIER - 1), new ItemShopPrice(material, amount), new CoinShopPrice(COINS))),
                false, false).withConfirmation();
        attachItem(shopItem);
        return shopItem;
    }
}
