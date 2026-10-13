package net.swofty.type.dwarvenmines.gui;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.fiesta.MiningFiesta;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.hunting.AttributeRegistry;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AttributeShardComponent;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIShopBrynmor extends ShopView {
    public static final String FIESTA_FORTUNE_SHARD = "U73";
    private static final String FIESTA_LOCK = "Only sold while Mayor Cole's Mining Fiesta is active.";

    public GUIShopBrynmor() {
        super("Brynmor", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        sell(ItemType.MINING_PUMPKIN, 50_000);
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.FILET_O_FORTUNE), 1, new CoinShopPrice(100_000))
                .lockedWhen(player -> hotmLock(player, 4)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.CHILLED_PRISTINE_POTATO), 1, new CoinShopPrice(250_000))
                .lockedWhen(player -> hotmLock(player, 7)));
        attachItem(ShopItem.Single(new SkyBlockItem(ItemType.FIESTA_FLASK), 1, new CoinShopPrice(2_500_000))
                .lockedWhen(GUIShopBrynmor::fiestaLock));
        attachItem(ShopItem.Single(AttributeShardComponent.create(AttributeRegistry.get(FIESTA_FORTUNE_SHARD), 1), 1,
                new CoinShopPrice(250_000)).lockedWhen(GUIShopBrynmor::fiestaLock));
    }

    private void sell(ItemType type, long coins) {
        attachItem(ShopItem.Single(new SkyBlockItem(type), 1, new CoinShopPrice(coins)));
    }

    private static String hotmLock(SkyBlockPlayer player, int tier) {
        return HotmService.data(player).getTier() >= tier ? null : "Requires Heart of the Mountain Tier " + tier + ".";
    }

    private static String fiestaLock(SkyBlockPlayer player) {
        return MiningFiesta.isActive() ? null : FIESTA_LOCK;
    }
}
