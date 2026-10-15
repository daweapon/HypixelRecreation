package dev.daweapon.type.skyblockgeneric.gui.inventories.auction;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.auctions.AuctionItem;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.domain.PlayerDataService;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.RefreshingGUI;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.auction.AuctionItemLoreHandler;
import dev.daweapon.type.skyblockgeneric.auction.AuctionPages;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDomain;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

abstract class AuctionPagedMenu extends HypixelInventoryGUI implements RefreshingGUI {
    private final AuctionPages<AuctionItem> pages;
    private final int[] slots;
    private final int previousSlot, nextSlot, indicatorSlot;
    private UUID profile;
    private AuctionPages.Request inFlight;
    private boolean loading;
    private boolean failed;
    private volatile boolean needsOpen = true;

    protected AuctionPagedMenu(Text title, InventoryType size, int[] slots, int previousSlot, int nextSlot, int indicatorSlot) {
        super(title, size);
        this.slots = slots.clone();
        this.pages = new AuctionPages<>(slots.length);
        this.previousSlot = previousSlot;
        this.nextSlot = nextSlot;
        this.indicatorSlot = indicatorSlot;
    }

    protected abstract CompletableFuture<List<AuctionItem>> fetch(SkyBlockPlayer player);
    protected abstract void controls();
    protected Material background() { return Material.BLACK_STAINED_GLASS_PANE; }

    @Override public void onOpen(InventoryGUIOpenEvent event) {
        needsOpen = true;
        refreshItems(getPlayer());
    }

    private void initialize(UUID current) {
        if (!current.equals(profile)) pages.reset();
        else pages.invalidate();
        profile = current;
        inFlight = null;
        loading = true;
        failed = false;
        needsOpen = false;
        render();
    }

    protected final void changeSelection(Runnable change) {
        onPlayer(() -> {
            change.run();
            pages.reset();
            inFlight = null;
            loading = true;
            failed = false;
            render();
            refresh();
        });
    }

    private void refresh() {
        if (inFlight != null) return;
        AuctionPages.Request request = pages.request();
        inFlight = request;
        loading = true;
        CompletableFuture<List<AuctionItem>> future;
        try { future = fetch((SkyBlockPlayer) getPlayer()); }
        catch (Exception error) { future = CompletableFuture.failedFuture(error); }
        future.orTimeout(10, TimeUnit.SECONDS).whenComplete((result, error) -> onPlayer(() -> {
            if (!request.equals(inFlight)) return;
            inFlight = null;
            loading = false;
            failed = error != null;
            if (error == null) pages.accept(request, result);
            else Logger.warn(error, "Unable to refresh auction menu {}", getClass().getSimpleName());
            render();
        }));
    }

    private void render() {
        synchronized (items) {
            fill(ItemStacks.named(background(), ""));
            controls();
            var page = pages.current();
            for (int i = 0; i < slots.length; i++) {
                int slot = slots[i];
                if (i >= page.items().size()) {
                    set(slot, ItemStack.builder(Material.AIR));
                    continue;
                }
                AuctionItem item = page.items().get(i);
                set(new GUIClickableItem(slot) {
                    @Override public void run(InventoryPreClickEvent event, HypixelPlayer player) {
                        onPlayer(() -> new GUIAuctionViewItem(item.getUuid(), AuctionPagedMenu.this).open(player));
                    }
                    @Override public ItemStack.Builder getItem(HypixelPlayer player) {
                        ItemStack.Builder updated = new NonPlayerItemUpdater(item.getItem()).getUpdatedItem();
                        return ItemStacks.text(updated, ItemStacks.nameText(updated),
                                new AuctionItemLoreHandler(item).getLoreTexts((SkyBlockPlayer) player));
                    }
                });
            }
            if (page.total() == 0) set(13, ItemStacks.item(failed ? Material.BARRIER : loading ? Material.CLOCK : Material.GRAY_DYE,
                    1, failed ? "<c>Could not load auctions" : loading ? "<e>Loading auctions..." : "<7>No auctions found",
                    failed ? "<7>Retrying automatically." : "<7>Use the menu controls to continue."));
            navigation(previousSlot, -1, page.hasPrevious());
            navigation(nextSlot, 1, page.hasNext());
            set(indicatorSlot, ItemStacks.item(Material.PAPER, 1, "<e>Page " + page.number() + " / " + page.count(),
                    "<7>" + page.total() + " auction" + (page.total() == 1 ? "" : "s")
                            + (failed ? "\n<c>Refresh failed; retrying automatically." : "")));
            updateItemStacks(getInventory(), getPlayer());
        }
    }

    private void navigation(int slot, int direction, boolean enabled) {
        String label = direction < 0 ? "Previous Page" : "Next Page";
        if (!enabled) {
            set(slot, ItemStacks.item(Material.ARROW, 1, "<8>" + label));
            return;
        }
        set(new GUIClickableItem(slot) {
            @Override public void run(InventoryPreClickEvent event, HypixelPlayer player) {
                onPlayer(() -> { pages.move(direction); render(); });
            }
            @Override public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ARROW, 1, "<a>" + label,
                        "<e>Page " + (pages.current().number() + direction));
            }
        });
    }

    private boolean isCurrent(SkyBlockPlayer player) {
        return player.isOnline() && GUI_MAP.get(player.getUuid()) == this && player.getOpenInventory() == getInventory();
    }

    private void onPlayer(Runnable action) {
        SkyBlockPlayer player = (SkyBlockPlayer) getPlayer();
        UUID expectedProfile = profile;
        player.scheduler().scheduleNextTick(() -> PlayerDataService.runLifecycleTransition(player.getUuid(), () -> {
            if (!isCurrent(player) || needsOpen) return;
            var handler = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
            if (handler != null && handler.getCurrentProfileId().equals(expectedProfile)) action.run();
        }));
    }

    @Override public void refreshItems(HypixelPlayer genericPlayer) {
        SkyBlockPlayer player = (SkyBlockPlayer) genericPlayer;
        player.scheduler().scheduleNextTick(() -> PlayerDataService.runLifecycleTransition(player.getUuid(), () -> {
            if (!isCurrent(player)) return;
            var handler = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
            if (handler == null) return;
            if (player.isIronman()) {
                player.sendMessage(Text.key("gui_auction.house.ironman_blocked"));
                player.closeInventory();
                return;
            }
            if (needsOpen || !handler.getCurrentProfileId().equals(profile)) initialize(handler.getCurrentProfileId());
            refresh();
        }));
    }
    @Override public int refreshRate() { return 40; }
    @Override public boolean allowHotkeying() { return false; }
    @Override public void onBottomClick(InventoryPreClickEvent event) { event.setCancelled(true); }
    @Override public void onClose(InventoryCloseEvent event, CloseReason reason) { needsOpen = true; pages.invalidate(); }
    @Override public void suddenlyQuit(Inventory inventory, HypixelPlayer player) { needsOpen = true; pages.invalidate(); }
}
