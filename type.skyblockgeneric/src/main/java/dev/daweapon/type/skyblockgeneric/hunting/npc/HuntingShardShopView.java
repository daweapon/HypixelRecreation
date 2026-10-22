package dev.daweapon.type.skyblockgeneric.hunting.npc;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.ViewSession;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneContactXP;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import dev.daweapon.type.skyblockgeneric.shop.PurchaseLimit;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class HuntingShardShopView extends ShopView {
    private final HuntingShardShops.Shop shop;
    private final @Nullable String contactId;
    private final @Nullable String purchaseTrigger;
    private final @Nullable Consumer<SkyBlockPlayer> onLeave;
    private final Map<ShopItem, HuntingShardShops.Offer> offers = new ConcurrentHashMap<>();

    public HuntingShardShopView(String shopId, @Nullable String contactId, @Nullable String purchaseTrigger,
                                @Nullable Consumer<SkyBlockPlayer> onLeave) {
        this(HuntingShardShops.get(shopId), contactId, purchaseTrigger, onLeave);
    }

    public HuntingShardShopView(HuntingShardShops.Shop shop, @Nullable String contactId, @Nullable String purchaseTrigger,
                                @Nullable Consumer<SkyBlockPlayer> onLeave) {
        super(Text.of(shop.title()), shop.slots());
        this.shop = shop;
        this.contactId = contactId;
        this.purchaseTrigger = purchaseTrigger;
        this.onLeave = onLeave;
    }

    public static HuntingShardShopView kiara() {
        return new HuntingShardShopView("kiara", "kiara", "kiara.buy", null);
    }

    public static PurchaseLimit limit(String shopId, HuntingShardShops.Offer offer, int stock) {
        return new PurchaseLimit(shopId + ":" + offer.shardId(), stock, PurchaseLimit.Period.DAY);
    }

    @Override
    public void initializeShopItems() {
        for (HuntingShardShops.Offer offer : shop.offers()) {
            AttributeDefinition definition = AttributeRegistry.get(offer.shardId());
            if (definition == null) {
                Logger.error("Shard shop {} lists unknown shard {}", shop.id(), offer.shardId());
                continue;
            }
            ShopItem item = new ShopItem(AttributeShardComponent.create(definition, 1), 1, new CoinShopPrice(offer.coins()), false, false)
                    .lockedWhen(player -> soldOut(player, offer) ? "<c>You have reached the maximum amount of items you can buy!" : null)
                    .onPurchase(player -> {
                        limitFor(player, offer).record(player, 1);
                        if (purchaseTrigger != null) ChapterTriggers.fire(player, purchaseTrigger);
                    });
            offers.put(item, offer);
            attachItem(item);
        }
    }

    private boolean hasContact(SkyBlockPlayer player) {
        return contactId != null && AbiphoneContactXP.unlocked(player).contains(contactId);
    }

    private PurchaseLimit limitFor(SkyBlockPlayer player, HuntingShardShops.Offer offer) {
        return limit(shop.id(), offer, shop.stockFor(offer, hasContact(player)));
    }

    private boolean soldOut(SkyBlockPlayer player, HuntingShardShops.Offer offer) {
        return limitFor(player, offer).reached(player);
    }

    @Override
    protected ItemStack.Builder renderItem(ShopItem item, int index, HypixelPlayer viewer) {
        ItemStack.Builder builder = super.renderItem(item, index, viewer);
        HuntingShardShops.Offer offer = offers.get(item);
        if (offer == null) return builder;
        SkyBlockPlayer player = (SkyBlockPlayer) viewer;
        List<Text> lore = ItemStacks.loreText(builder);
        lore.addAll(Math.max(0, lore.size() - 1), List.of(
                Text.of("<7>Stock"),
                Text.of("<6>{} <7>remaining", limitFor(player, offer).remaining(player)),
                Text.empty()));
        return ItemStacks.lore(builder, lore);
    }

    @Override
    public void onClose(State state, ViewContext ctx, ViewSession.CloseReason reason) {
        super.onClose(state, ctx, reason);
        if (onLeave != null && reason == ViewSession.CloseReason.PLAYER_EXITED) onLeave.accept((SkyBlockPlayer) ctx.player());
    }
}
