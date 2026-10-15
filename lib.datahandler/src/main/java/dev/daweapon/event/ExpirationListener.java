package dev.daweapon.event;

import dev.daweapon.ExpiringField;

import java.util.UUID;

@FunctionalInterface
public interface ExpirationListener<T> {
    void onExpired(UUID playerId, ExpiringField<T> field, T expiredValue);
}
