package net.swofty.type.skyblockgeneric.data;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAuctionOperations;
import net.swofty.type.generic.data.mongodb.ProfilesDatabase;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data;
import org.bson.Document;

import java.util.Arrays;
import java.util.UUID;

final class SkyBlockProfileDocumentWriter {

    private SkyBlockProfileDocumentWriter() {
    }

    static void persist(net.swofty.DataAPI api, UUID profile, Document document) {
        validate(document);
        api.transaction(profile, transaction -> { write(transaction, document); });
    }

    static void validate(Document document) {
        for (Data field : new Data[]{Data.COINS, Data.INVENTORY, Data.AUCTION_DELIVERED_CLAIMS, Data.AUCTION_OPERATIONS,
                Data.AUCTION_ACTIVE_OWNED, Data.AUCTION_INACTIVE_OWNED, Data.AUCTION_ACTIVE_BIDS, Data.AUCTION_INACTIVE_BIDS}) {
            if (!document.containsKey(field.getKey())) throw new IllegalStateException("Incomplete profile save: " + field);
        }
    }

    static void write(net.swofty.transaction.Transaction transaction, Document document) {
        validate(document);
        String savedClaims = transaction.get(Data.AUCTION_DELIVERED_CLAIMS.profileField());
        if (savedClaims != null && !savedClaims.isEmpty()) {
            var currentClaims = Arrays.asList(document.getString(Data.AUCTION_DELIVERED_CLAIMS.getKey()).split(","));
            if (!currentClaims.containsAll(Arrays.asList(savedClaims.split(",")))) {
                throw new IllegalStateException("Refusing to overwrite newer auction claim receipts");
            }
        }
        var savedOperations = DatapointAuctionOperations.read(transaction.get(Data.AUCTION_OPERATIONS.profileField()));
        var operations = DatapointAuctionOperations.read(document.getString(Data.AUCTION_OPERATIONS.getKey()));
        if (operations.revision() < savedOperations.revision()
                || operations.revision() == savedOperations.revision() && !operations.equals(savedOperations)) {
            throw new IllegalStateException("Refusing to overwrite newer auction operations");
        }
        for (Data field : Data.values()) {
            if (field.coopField() == null && document.containsKey(field.getKey())) {
                transaction.set(field.profileField(), document.getString(field.getKey()));
            }
        }
        transaction.set(ProfilesDatabase.DOCUMENT, document.toJson());
    }
}
