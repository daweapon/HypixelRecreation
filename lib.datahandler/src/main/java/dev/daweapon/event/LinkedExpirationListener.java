package dev.daweapon.event;

import dev.daweapon.ExpiringLinkedField;

import java.util.Set;
import java.util.UUID;

@FunctionalInterface
public interface LinkedExpirationListener<K, T> {
    void onExpired(K linkKey, ExpiringLinkedField<K, T> field, T expiredValue, Set<UUID> memberIds);
}
