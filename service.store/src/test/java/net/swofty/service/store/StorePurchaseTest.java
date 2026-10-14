package net.swofty.service.store;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StorePurchaseTest {
    @Test
    void mapsDocumentFieldsAndNestedEntitlements() {
        Instant paidAt = Instant.parse("2026-01-02T03:04:05Z");
        Document document = new Document("_id", "pi_123")
                .append("playerUuid", "uuid-1")
                .append("playerName", "Steve")
                .append("productId", "prod")
                .append("productName", "Product")
                .append("paidAt", Date.from(paidAt))
                .append("attempts", 4)
                .append("entitlements", List.of(
                        new Document("type", "RANK").append("key", "VIP").append("durationDays", 30),
                        new Document("type", "ITEM").append("key", "CAKE").append("amount", 2)));

        StorePurchase purchase = StorePurchase.fromDocument(document);

        assertEquals("pi_123", purchase.id());
        assertEquals("uuid-1", purchase.playerUuid());
        assertEquals("Steve", purchase.playerName());
        assertEquals("prod", purchase.productId());
        assertEquals("Product", purchase.productName());
        assertEquals(paidAt, purchase.paidAt());
        assertEquals(4, purchase.attempts());
        assertEquals(List.of(
                new StoreEntitlement("RANK", "VIP", 1L, 30L),
                new StoreEntitlement("ITEM", "CAKE", 2L, null)), purchase.entitlements());
    }

    @Test
    void missingOptionalFieldsFallBackToDefaults() {
        Instant before = Instant.now();

        StorePurchase purchase = StorePurchase.fromDocument(new Document("_id", "x"));

        assertEquals(0, purchase.attempts());
        assertTrue(purchase.entitlements().isEmpty());
        assertFalse(purchase.paidAt().isBefore(before));
    }
}
