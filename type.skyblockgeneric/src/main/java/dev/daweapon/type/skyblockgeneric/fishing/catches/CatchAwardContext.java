package dev.daweapon.type.skyblockgeneric.fishing.catches;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Side-effect surface a {@link CatchPayload} sees when it's applied.
 * Keeps the payload variants free of FishingHook coupling while still
 * giving them everything they need (player, the rod that pulled them
 * up, and the bobber position for spawn placement).
 */
public record CatchAwardContext(SkyBlockPlayer player, SkyBlockItem rod, Pos hookPosition) {
}
