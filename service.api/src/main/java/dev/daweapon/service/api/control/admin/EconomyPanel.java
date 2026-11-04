package dev.daweapon.service.api.control.admin;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import dev.daweapon.commons.protocol.objects.darkauction.GetAuctionStateProtocol;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Read-only economy health: the same Mongo collections the bazaar and auction services own, plus
// the purse total from the cached Redis profile scan and the Dark Auction service's live state.
public final class EconomyPanel {
    public record PendingTransaction(String id, String playerUuid, String profileUuid, String type, String createdAt) {}

    public record Snapshot(long pendingTransactions, Map<String, Long> pendingByType,
                           List<PendingTransaction> oldestPending, long bazaarOrders, long activeAuctions,
                           long endedAuctions, double totalPurseCoins, int profilesScanned, String coinsScannedAt,
                           @Nullable GetAuctionStateProtocol.GetAuctionStateResponse darkAuction) {}

    private static final Bson UNPROCESSED = Filters.eq("processed", false);

    private EconomyPanel() {}

    public static Snapshot snapshot(boolean refresh) {
        MongoDatabase database = AdminBackends.database();
        MongoCollection<Document> pending = database.getCollection("pendingTransactions");

        Map<String, Long> byType = new LinkedHashMap<>();
        for (String type : pending.distinct("transactionType", UNPROCESSED, String.class)) {
            byType.put(type, pending.countDocuments(Filters.and(UNPROCESSED, Filters.eq("transactionType", type))));
        }

        List<PendingTransaction> oldest = new ArrayList<>();
        for (Document document : pending.find(UNPROCESSED).sort(Sorts.ascending("createdAt")).limit(20)) {
            oldest.add(new PendingTransaction(document.getString("_id"), document.getString("playerUuid"),
                    document.getString("profileUuid"), document.getString("transactionType"),
                    document.getString("createdAt")));
        }

        OrphanScan.Result scan = OrphanScan.scan(refresh);
        return new Snapshot(pending.countDocuments(UNPROCESSED), byType, oldest,
                database.getCollection("bazaarOrders").estimatedDocumentCount(),
                database.getCollection("active-auctions").estimatedDocumentCount(),
                database.getCollection("inactive-auctions").estimatedDocumentCount(),
                scan.totalCoins(), scan.profiles(), scan.scannedAt().toString(), EventControl.darkAuctionState());
    }
}
