package net.swofty.type.skyblockgeneric.auction;

import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.auctions.AuctionClaimProtocol;
import net.swofty.commons.protocol.objects.auctions.AuctionClaimProtocol.*;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.data.datapoints.DatapointDouble;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.skyblockgeneric.data.*;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointUUIDList;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAuctionStatistics;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;
import java.util.*;
import java.util.concurrent.*;

public final class AuctionClaims {
    private static final Set<UUID> submitting=ConcurrentHashMap.newKeySet();
    private AuctionClaims() {}

    public static void claim(SkyBlockPlayer player, UUID auction, UUID profile, boolean seller) {
        if (!submitting.add(player.getUuid())) return;
        Thread.startVirtualThread(() -> {
            try {
                boolean[] allowed={false};
                PlayerDataService.runLifecycleTransition(player.getUuid(), () -> {
                    var handler=SkyBlockDataHandler.getUser(player);
                    if (!player.isOnline() || handler==null || !profile.equals(handler.getCurrentProfileId())) return;
                    allowed[0]=list(handler,seller ? SkyBlockDataHandler.Data.AUCTION_ACTIVE_OWNED : SkyBlockDataHandler.Data.AUCTION_ACTIVE_BIDS).contains(auction)
                            && !list(handler,seller ? SkyBlockDataHandler.Data.AUCTION_INACTIVE_OWNED : SkyBlockDataHandler.Data.AUCTION_INACTIVE_BIDS).contains(auction);
                    if (allowed[0]) PlayerDataService.domain(SkyBlockDomain.KEY).save(player);
                });
                if (!allowed[0]) return;
                var response=new ProxyService(ServiceType.AUCTION_HOUSE).handleRequest(new AuctionClaimProtocol(),
                        new Request(auction,player.getUuid(),profile,(int) ElectionPerks.factor(SkyBlockMayor.Perk.QUAD_TAXES))).get(10,TimeUnit.SECONDS);
                if (!response.success()) { player.sendMessage("<c>"+response.error()); return; }
                Receipt receipt=response.receipt();
                if (receipt==null || !auction.equals(receipt.auction()) || !profile.equals(receipt.profile())
                        || !player.getUuid().equals(receipt.player()) || seller!=receipt.seller()) throw new IllegalStateException("Invalid claim receipt");
                var completed=new CompletableFuture<Void>();
                player.scheduler().scheduleNextTick(() -> {
                    try {
                        PlayerDataService.runLifecycleTransition(player.getUuid(), () -> deliver(player,receipt));
                        completed.complete(null);
                    } catch (Throwable failure) { completed.completeExceptionally(failure); }
                });
                completed.get(15,TimeUnit.SECONDS);
            } catch (Exception failure) {
                Logger.error(failure,"Auction claim could not complete for {}",player.getUuid());
                player.sendMessage("<e>Your claim could not be confirmed. Reopen the auction to retry.");
            } finally { submitting.remove(player.getUuid()); }
        });
    }

    private static void deliver(SkyBlockPlayer player, Receipt receipt) {
        var handler=SkyBlockDataHandler.getUser(player);
        if (!player.isOnline() || handler==null || !receipt.profile().equals(handler.getCurrentProfileId())) return;
        var delivered=list(handler,SkyBlockDataHandler.Data.AUCTION_DELIVERED_CLAIMS);
        var active=list(handler,receipt.seller() ? SkyBlockDataHandler.Data.AUCTION_ACTIVE_OWNED : SkyBlockDataHandler.Data.AUCTION_ACTIVE_BIDS);
        var inactive=list(handler,receipt.seller() ? SkyBlockDataHandler.Data.AUCTION_INACTIVE_OWNED : SkyBlockDataHandler.Data.AUCTION_INACTIVE_BIDS);
        if (delivered.contains(receipt.id()) || inactive.contains(receipt.auction()) || !active.contains(receipt.auction())) return;
        int slot=-1;
        net.minestom.server.item.ItemStack reward=null;
        if (receipt.item()!=null) {
            reward=PlayerItemUpdater.playerUpdate(player,new SkyBlockItem(UnderstandableSkyBlockItem.deserialize(receipt.item())).getItemStack(),true).build();
            if (!SkyBlockItem.isSkyBlockItem(reward)) throw new IllegalStateException("Claim item cannot be persisted");
            for (int i=0;i<36;i++) if (player.getInventory().getItemStack(i).isAir()) { slot=i; break; }
            if (slot<0) { player.sendMessage("<e>Free an inventory slot, then claim this auction again."); return; }
        }
        final int target=slot;
        final var item=reward;
        try {
            boolean committed = handler.commitAuctionClaim(receipt.id(), player, () -> settle(receipt,handler.get(SkyBlockDataHandler.Data.COINS,DatapointDouble.class),delivered,active,inactive,
                    handler.get(SkyBlockDataHandler.Data.AUCTION_STATISTICS, DatapointAuctionStatistics.class).getValue(),
                    () -> {
                        if (item==null) return;
                        try { player.getInventory().setItemStack(target,item); }
                        catch (RuntimeException failure) {
                            if (!player.getInventory().getItemStack(target).equals(item)) throw failure;
                            Logger.warn(failure,"Claim item was placed but an inventory listener failed");
                        }
                    },
                    () -> {}));
            if (!committed) {
                PlayerDataService.discardAll(net.swofty.type.generic.HypixelConst.getTypeLoader().getType(), player.getUuid());
                player.kick("<e>This claim was already collected. Reconnect to refresh your profile.");
                return;
            }
        } catch (Throwable failure) {
            player.kick("<c>Your auction claim could not be saved. Reconnect and reopen the auction to recover it.");
            throw failure;
        }
        player.closeInventory();
        player.sendMessage("<a>Auction claim collected!"+(receipt.coins()>0 ? " <6>+"+net.swofty.commons.StringUtility.commaify(receipt.coins())+" coins" : ""));
    }

    static boolean settle(Receipt receipt, DatapointDouble purse, List<UUID> delivered,
                          List<UUID> active, List<UUID> inactive, Runnable giveItem, Runnable save) {
        return settle(receipt, purse, delivered, active, inactive, null, giveItem, save);
    }

    static boolean settle(Receipt receipt, DatapointDouble purse, List<UUID> delivered,
                          List<UUID> active, List<UUID> inactive,
                          DatapointAuctionStatistics.AuctionStatistics statistics,
                          Runnable giveItem, Runnable save) {
        if (delivered.contains(receipt.id()) || inactive.contains(receipt.auction()) || !active.contains(receipt.auction())) return false;
        if (receipt.coins()<0) throw new IllegalArgumentException("Invalid claim coins");
        giveItem.run();
        synchronized (purse) { purse.setFrom(new DatapointDouble("coins",purse.getValue()+receipt.coins())); }
        updateStatistics(receipt, statistics);
        delivered.add(receipt.id());
        active.removeIf(receipt.auction()::equals);
        if (!inactive.contains(receipt.auction())) inactive.add(receipt.auction());
        save.run();
        return true;
    }

    private static void updateStatistics(Receipt receipt, DatapointAuctionStatistics.AuctionStatistics statistics) {
        if (statistics == null) return;
        if (receipt.seller()) {
            statistics.increment(receipt.sold()
                    ? DatapointAuctionStatistics.AuctionStatistics.AuctionStat.AUCTIONS_COMPLETED_WITH_BIDS
                    : DatapointAuctionStatistics.AuctionStatistics.AuctionStat.AUCTIONS_COMPLETED_WITHOUT_BIDS);
            if (receipt.sold()) {
                statistics.maximum(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.HIGHEST_AUCTION_HELD, receipt.salePrice());
                statistics.add(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.TOTAL_COINS_EARNED, receipt.coins());
                statistics.sold(rarity(receipt));
            }
        } else if (receipt.item() != null && receipt.sold()) {
            statistics.increment(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.AUCTIONS_WON);
            statistics.add(DatapointAuctionStatistics.AuctionStatistics.AuctionStat.TOTAL_COINS_SPENT, receipt.salePrice());
            statistics.bought(rarity(receipt));
        }
    }

    private static net.swofty.commons.skyblock.item.Rarity rarity(Receipt receipt) {
        if (receipt.auctionItem() == null) return null;
        return new SkyBlockItem(UnderstandableSkyBlockItem.deserialize(receipt.auctionItem())).getAttributeHandler().getRarity();
    }
    private static List<UUID> list(SkyBlockDataHandler handler,SkyBlockDataHandler.Data field) {
        return handler.get(field,DatapointUUIDList.class).getValue();
    }
}
