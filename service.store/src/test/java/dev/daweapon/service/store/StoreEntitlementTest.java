package dev.daweapon.service.store;

import dev.daweapon.commons.protocol.objects.proxy.to.StorePurchaseFulfillmentProtocol;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoreEntitlementTest {
    @Test
    void readsAllFieldsFromDocument() {
        Document document = new Document("type", "RANK").append("key", "MVP_PLUS")
                .append("amount", 3).append("durationDays", 30);

        StoreEntitlement entitlement = StoreEntitlement.fromDocument(document);

        assertEquals(new StoreEntitlement("RANK", "MVP_PLUS", 3L, 30L), entitlement);
    }

    @Test
    void amountDefaultsToOneAndDurationToNull() {
        StoreEntitlement entitlement = StoreEntitlement.fromDocument(new Document("type", "ITEM").append("key", "SWORD"));

        assertEquals(1L, entitlement.amount());
        assertNull(entitlement.durationDays());
    }

    @Test
    void acceptsAnyNumericTypeForAmountAndDuration() {
        Document document = new Document("type", "COINS").append("key", "COINS")
                .append("amount", 2_500_000_000L).append("durationDays", 7.0);

        StoreEntitlement entitlement = StoreEntitlement.fromDocument(document);

        assertEquals(2_500_000_000L, entitlement.amount());
        assertEquals(7L, entitlement.durationDays());
    }

    @Test
    void toProtocolCopiesEveryField() {
        StorePurchaseFulfillmentProtocol.Entitlement protocol = new StoreEntitlement("RANK", "VIP", 1, 14L).toProtocol();

        assertEquals("RANK", protocol.type());
        assertEquals("VIP", protocol.key());
        assertEquals(1, protocol.amount());
        assertEquals(14L, protocol.durationDays());
    }
}
