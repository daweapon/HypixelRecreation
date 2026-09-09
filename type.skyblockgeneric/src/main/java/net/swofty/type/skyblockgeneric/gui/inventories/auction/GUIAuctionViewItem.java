package net.swofty.type.skyblockgeneric.gui.inventories.auction;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.auctions.AuctionFetchItemProtocol;
import net.swofty.commons.skyblock.auctions.AuctionItem;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.skyblockgeneric.data.SkyBlockDomain;
import org.tinylog.Logger;
import java.util.concurrent.atomic.AtomicLong;
import net.swofty.type.skyblockgeneric.auction.AuctionItemLoreHandler;
import net.swofty.type.skyblockgeneric.gui.inventories.auction.view.AuctionViewSelfBIN;
import net.swofty.type.skyblockgeneric.gui.inventories.auction.view.AuctionViewSelfNormal;
import net.swofty.type.skyblockgeneric.gui.inventories.auction.view.AuctionViewThirdBin;
import net.swofty.type.skyblockgeneric.gui.inventories.auction.view.AuctionViewThirdNormal;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

// GUI for viewing a single auction listing, delegating role-specific slots (self/bidder, normal/BIN) to an AuctionView
public class GUIAuctionViewItem extends HypixelInventoryGUI implements RefreshingGUI {
    public final UUID auctionID;
    public final HypixelInventoryGUI previousGUI;
    public long bidAmount = 0;
    public long minimumBidAmount = 0;
    private UUID profile;
    private final AtomicLong requests = new AtomicLong();
    private long appliedRequest;

    public GUIAuctionViewItem(UUID auctionID, HypixelInventoryGUI previousGUI) {
        super(Text.key("gui_auction.view.title"), InventoryType.CHEST_6_ROW);

        this.auctionID = auctionID;
        this.previousGUI = previousGUI;


    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        profile = ((SkyBlockPlayer) getPlayer()).getSkyblockDataHandler().getCurrentProfileId();
        if (((SkyBlockPlayer) getPlayer()).isIronman()) {
            getPlayer().sendMessage(Text.key("gui_auction.house.ironman_blocked"));
            getPlayer().closeInventory();
            return;
        }
        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getGoBackItem(49, previousGUI));
        updateItems();
    }

    public void updateItems() {
        SkyBlockPlayer player = (SkyBlockPlayer) getPlayer();
        UUID requestedProfile = profile;
        long request = requests.incrementAndGet();
        new ProxyService(ServiceType.AUCTION_HOUSE).handleRequest(new AuctionFetchItemProtocol(),
                new AuctionFetchItemProtocol.AuctionFetchItemMessage(auctionID)).thenAccept(result ->
                player.scheduler().scheduleNextTick(() -> PlayerDataService.runLifecycleTransition(player.getUuid(), () -> {
                    var handler = PlayerDataService.find(SkyBlockDomain.KEY, player.getUuid()).orElse(null);
                    if (!player.isOnline() || GUI_MAP.get(player.getUuid()) != this || player.getOpenInventory() != getInventory() || handler == null
                            || !requestedProfile.equals(handler.getCurrentProfileId()) || request < appliedRequest) return;
                    appliedRequest = request;
                    synchronized (items) {
                        render(result, player);
                        updateItemStacks(getInventory(), player);
                    }
                }))).exceptionally(error -> {
                    Logger.warn(error, "Auction view could not refresh {}", auctionID);
                    return null;
                });
    }

    private void render(AuctionFetchItemProtocol.AuctionFetchItemResponse result, SkyBlockPlayer player) {
        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getGoBackItem(49, previousGUI));
        if (!result.success() || result.item() == null) {
            set(13, ItemStacks.item(Material.BARRIER, 1, "<c>Auction not found"));
            return;
        }
        AuctionItem item = result.item();
        set(new GUIItem(13) {
            @Override public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.lore(PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(item.getItem()).getItemStack()),
                        new AuctionItemLoreHandler(item).getLoreTexts(player));
            }
        });
        if (item.getOriginator().equals(player.getUuid())) {
            if (item.getSellerProfile() != null && !profile.equals(item.getSellerProfile())) {
                set(31, ItemStacks.item(Material.BARRIER, 1, "<c>Different Profile",
                        "<7>Return to the profile that created\n<7>this auction to collect it."));
            } else if (item.isBin()) new AuctionViewSelfBIN().open(this, item, player);
            else new AuctionViewSelfNormal().open(this, item, player);
        } else if (item.isBin()) new AuctionViewThirdBin().open(this, item, player);
        else new AuctionViewThirdNormal().open(this, item, player);
    }

    @Override
    public void refreshItems(HypixelPlayer player) { updateItems(); }

    @Override
    public int refreshRate() {
        return 10;
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {

    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {

    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
