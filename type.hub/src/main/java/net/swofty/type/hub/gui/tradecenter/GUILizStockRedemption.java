package net.swofty.type.hub.gui.tradecenter;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.ShopPrice;
import net.swofty.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.List;

/**
 * Liz's shop. Freshly-Minted Coins come out of a stock shared by the whole SkyBlock network,
 * 5,000 units per 4-hour window, counted in Redis so every server sells from the same pile.
 */
public final class GUILizStockRedemption extends CatalogueShopView {
    private static final ItemType SHARED_STOCK_ITEM = ItemType.FRESHLY_MINTED_COINS;
    private static final long SHARED_STOCK = 5_000;
    private static final Duration WINDOW = Duration.ofHours(4);
    private static volatile long cachedUsed;

    public GUILizStockRedemption() {
        super("liz");
        refreshSharedStock();
    }

    @Override
    protected ItemStack.Builder renderItem(ShopItem item, int index, HypixelPlayer player) {
        ItemStack.Builder builder = super.renderItem(item, index, player);
        if (!isSharedStockItem(item)) return builder;
        List<Text> lore = ItemStacks.loreText(builder);
        int insertAt = Math.max(0, lore.size() - 1);
        lore.addAll(insertAt, List.of(
                Text.of("<c>This item has a SkyBlock-wide limit of"),
                Text.of("<c>{} units every 4h 0m.", StringUtility.commaify(SHARED_STOCK)),
                Text.empty(),
                Text.of("<7>Stock: <a>{}", StringUtility.commaify(Math.max(0, SHARED_STOCK - cachedUsed))),
                Text.of("<7>Resets in: <a>{}", resetsIn()),
                Text.empty()));
        return ItemStacks.lore(builder, lore);
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, ShopItem item, int index) {
        if (!isSharedStockItem(item)) {
            super.onItemClick(click, ctx, item, index);
            return;
        }
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        ShopPrice price = item.getPrice();
        if (!price.canAfford(player)) {
            player.sendMessage("<c>You don't have enough {}!", price.getNamePlural());
            return;
        }
        String key = stockKey();
        int amount = item.getAmount();
        Thread.startVirtualThread(() -> {
            long used = LeaderboardService.consumeSharedStock(key, amount, SHARED_STOCK, WINDOW.toSeconds());
            MinecraftServer.getSchedulerManager().scheduleNextTick(() -> finishSharedPurchase(ctx, player, price, key, amount, used));
        });
    }

    private void finishSharedPurchase(ViewContext ctx, SkyBlockPlayer player, ShopPrice price, String key, int amount, long used) {
        if (used == -2) {
            player.sendMessage("<c>Liz can't check her stock right now, try again later!");
            return;
        }
        if (used == -1) {
            player.sendMessage("<c>This item is out of stock! It restocks in {}.", resetsIn());
            return;
        }
        if (!player.isOnline() || !price.canAfford(player)) {
            Thread.startVirtualThread(() -> LeaderboardService.releaseSharedStock(key, amount));
            if (player.isOnline()) player.sendMessage("<c>You don't have enough {}!", price.getNamePlural());
            return;
        }
        cachedUsed = used;
        price.processPurchase(player);
        SkyBlockItem bought = new SkyBlockItem(SHARED_STOCK_ITEM);
        bought.setAmount(amount);
        player.addAndUpdateItem(bought);
        player.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.PLAYER, 1.0f, 2.0f));
        ctx.session(Object.class).refresh();
    }

    private static boolean isSharedStockItem(ShopItem item) {
        return item.getItem().getItemType() == SHARED_STOCK_ITEM;
    }

    private static void refreshSharedStock() {
        String key = stockKey();
        Thread.startVirtualThread(() -> cachedUsed = LeaderboardService.sharedStockUsed(key));
    }

    private static long windowIndex() {
        return System.currentTimeMillis() / WINDOW.toMillis();
    }

    private static String stockKey() {
        return "liz_freshly_minted_coins:" + windowIndex();
    }

    private static String resetsIn() {
        long remaining = (windowIndex() + 1) * WINDOW.toMillis() - System.currentTimeMillis();
        long minutes = Duration.ofMillis(remaining).toMinutes();
        return String.format("%dh %02dm", minutes / 60, minutes % 60);
    }
}
