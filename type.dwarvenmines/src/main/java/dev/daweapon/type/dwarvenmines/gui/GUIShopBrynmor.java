package dev.daweapon.type.dwarvenmines.gui;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.fiesta.MiningFiesta;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import dev.daweapon.type.skyblockgeneric.shop.PurchaseLimit;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIShopBrynmor extends ShopView {
    public static final String FIESTA_FORTUNE_SHARD = "U73";
    private static final PurchaseLimit FLASK_LIMIT = new PurchaseLimit("FIESTA_FLASK", 5, PurchaseLimit.Period.MAYORSHIP);
    private static final PurchaseLimit SHARD_LIMIT = new PurchaseLimit("PACKRAT_SHARD", 64, PurchaseLimit.Period.YEAR);
    private static final String FIESTA_LOCK = "Only sold while Mayor Cole's Mining Fiesta is active.";

    public GUIShopBrynmor() {
        super("Brynmor's Shop", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.MINING_PUMPKIN), 1, new CoinShopPrice(50_000))
                .lockedWhen(player -> hotmLock(player, 1)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.FILET_O_FORTUNE), 1, new CoinShopPrice(100_000))
                .lockedWhen(player -> hotmLock(player, 4)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.CHILLED_PRISTINE_POTATO), 1, new CoinShopPrice(250_000))
                .lockedWhen(player -> hotmLock(player, 7)));
        attachItem(limited(new SkyBlockItem(ItemType.FIESTA_FLASK), 2_500_000, FLASK_LIMIT));
        attachItem(limited(AttributeShardComponent.create(AttributeRegistry.get(FIESTA_FORTUNE_SHARD), 1), 250_000, SHARD_LIMIT));
    }

    private static ShopItem limited(SkyBlockItem item, long coins, PurchaseLimit limit) {
        return ShopItem.Single(item, 1, new CoinShopPrice(coins))
                .lockedWhen(player -> fiestaLock(player, limit))
                .onPurchase(player -> limit.record(player, 1));
    }

    private static String hotmLock(SkyBlockPlayer player, int tier) {
        return HotmService.data(player).getTier() >= tier ? null : "Requires Heart of the Mountain Tier " + tier + ".";
    }

    private static String fiestaLock(SkyBlockPlayer player, PurchaseLimit limit) {
        if (!MiningFiesta.isActive()) return FIESTA_LOCK;
        return limit.reached(player) ? "<c>You have reached the maximum amount of items you can buy!" : null;
    }
}
