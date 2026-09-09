package net.swofty.type.generic.collectibles;

import net.swofty.type.generic.user.HypixelPlayer;

// Custom logic for determining whether a player has unlocked a collectible, beyond the standard unlock methods.
@FunctionalInterface
public interface CollectibleUnlockResolver {
    boolean isUnlocked(HypixelPlayer player, CollectibleDefinition definition);
}
